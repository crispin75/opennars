package org.projog.core.udp.compiler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.parseTermsFromFile;
import static org.projog.core.KnowledgeBaseUtils.IMPLICATION_PREDICATE_NAME;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.api.Projog;
import org.projog.core.KB;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.ProjogSystemProperties;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.udp.ClauseModel;
import org.projog.core.udp.MultipleRulesWithMultipleImmutableArgumentsPredicate;
import org.projog.core.udp.MultipleRulesWithSingleImmutableArgumentPredicate;
import org.projog.core.udp.SingleRuleWithMultipleImmutableArgumentsPredicate;
import org.projog.core.udp.SingleRuleWithSingleImmutableArgumentPredicate;
import org.projog.core.udp.StaticUserDefinedPredicateFactory;

/**
 * Tests java code generated by {@link CompiledPredicateSourceGenerator}.
 * <p>
 * Compares the Java source generated as a result of processing CompiledPredicateSourceGeneratorTest.pl against the
 * expected java source contained in the scripts directory.
 * <p>
 * NOTE: For these tests to work you need to have "projogGeneratedClasses" in the classpath as that will be the output
 * directory for bytecode generated at runtime.
 * 
 * @see org.projog.TestUtils#COMPILATION_ENABLED_PROPERTIES
 */
public class CompiledPredicateSourceGeneratorTest {
   /**
    * Directory containing expected values.
    */
   private final File RESOURCE_DIR = new File("src/prolog/CompiledPredicateSourceGeneratorTest");
   /**
    * File containing Prolog syntax that is used to generate the Java source examined by this test case.
    */
   private final File PROLOG_SOURCE = new File(RESOURCE_DIR, "CompiledPredicateSourceGeneratorTest.pl");
   private static final String FILE_SUFFIX = ".txt";
   private static final String DUBUG_SUFFIX = "_debug";

   private final File outputDir = new File("build", getClass().getName());
   private final KB debugEnabledKB;
   private final KB debugDisabledKB;

   public CompiledPredicateSourceGeneratorTest() {
      debugEnabledKB = TestUtils.createKnowledgeBase();
      debugDisabledKB = TestUtils.createKnowledgeBase(new ProjogSystemProperties() {
         @Override
         public boolean isSpyPointsEnabled() {
            return false;
         }
      });
      addSingletonCompiledPredicate("singleton_compiled_Predicate/3");
      addRetryableCompiledPredicate("retryable_compiled_Predicate/3");
      addMultipleRulesWithSingleImmutableArgumentPredicate("multiple_rules_with_single_immutable_argument/1", new String[] {"s", "d", "a"});
      addMultipleRulesWithMulipleImmutableArgumentPredicate("multiple_rules_with_multiple_immutable_arguments/3", new String[][] {
                  {"q", "w", "e"},
                  {"a", "s", "d"},
                  {"z", "x", "z"}});
      addSingleRulesWithSingleImmutableArgumentPredicate("single_rule_with_single_immutable_argument/1", "z");
      addSingleRulesWithMulipleImmutableArgumentPredicate("single_rule_with_multiple_immutable_arguments/3", new String[] {"a", "s", "d"});
      addUserDefinedPredicate("testCalculatables/3");
   }

   /**
    * Confirm assertContentsMatch works as expected.
    * <p>
    * (If assertContentsMatch didn't work then it would make the other tests invalid.)
    */
   @Test
   public void testTestUtilsAssertContentMatches() {
      File files[] = getAllNonDebugTestFiles();
      assertTrue(files.length > 1);
      for (int i1 = 0; i1 < files.length; i1++) {
         for (int i2 = 0; i2 < files.length; i2++) {
            if (i1 != i2) {
               assertContentDoesNotMatch(files[i1], files[i2]);
            }
         }
      }
   }

   private void assertContentDoesNotMatch(File f1, File f2) {
      try {
         assertContentsMatch(f1, f2);
         fail();
      } catch (RuntimeException e) {
         // expected
      }
   }

   /**
    * Test consulting {@link #PROLOG_SOURCE}.
    * <p>
    * Check syntax in test script can be parsed correctly and compiles to bytecode without any exceptions.
    */
   @Test
   public void testScriptCompiles() {
      Projog p = new Projog(TestUtils.COMPILATION_ENABLED_PROPERTIES);
      p.consultFile(PROLOG_SOURCE);
   }

   /**
    * Test the Java source files generated as a result of consulting {@link #PROLOG_SOURCE} match the expected values.
    */
   @Test
   public void testScriptGeneration() {
      File expectedSourceContent[] = getAllNonDebugTestFiles();
      Map<String, List<PTerm>> testFunctions = getTestFunctionNames();
      for (File f : expectedSourceContent) {
         // get filename
         String filename = getFunctionName(f);
         String functionName = "test" + filename;
         // remove from map
         List<PTerm> terms = testFunctions.remove(functionName);
         assertNotNull("no terms for: " + functionName, terms);
         assertContentMatches(filename, terms.toArray(new PTerm[terms.size()]));
      }
      assertTrue("no source files to compare for: " + testFunctions.keySet(), testFunctions.isEmpty());
   }

   private Map<String, List<PTerm>> getTestFunctionNames() {
      Map<String, List<PTerm>> testFunctions = new HashMap<>();
      PTerm[] allTerms = parseTermsFromFile(PROLOG_SOURCE);
      for (PTerm t : allTerms) {
         String name = getFunctionName(t);
         if (name.startsWith("test")) {
            List<PTerm> functionTerms = testFunctions.get(name);
            if (functionTerms == null) {
               functionTerms = new ArrayList<>();
               testFunctions.put(name, functionTerms);
            }
            functionTerms.add(t);
         }
      }
      return testFunctions;
   }

   private String getFunctionName(PTerm t) {
      if (t.getName().equals(IMPLICATION_PREDICATE_NAME)) {
         return t.term(0).getName();
      } else {
         return t.getName();
      }
   }

   private String getFunctionName(File f) {
      String name = f.getName();
      return name.substring(0, name.indexOf('.'));
   }

   private File[] getAllNonDebugTestFiles() {
      File[] files = RESOURCE_DIR.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return name.indexOf(DUBUG_SUFFIX) == -1 && name.endsWith(FILE_SUFFIX);
         }
      });
      assertTrue(files.length > 0);
      // sort array as predicatable order makes investigation of any problems easier
      Arrays.sort(files);
      return files;
   }

   private void assertContentMatches(String filenamePrefix, PTerm[] terms) {
      assertContentMatches(debugDisabledKB, filenamePrefix + FILE_SUFFIX, terms);
      assertContentMatches(debugEnabledKB, filenamePrefix + DUBUG_SUFFIX + FILE_SUFFIX, terms);
   }

   private void assertContentMatches(KB kb, String exampleSourceFilename, PTerm[] terms) {
      File exampleSourceFile = new File(RESOURCE_DIR, exampleSourceFilename);
      File newlyGeneratedSourceFile = getGeneratedSourceFile(kb, terms);
      assertContentsMatch(exampleSourceFile, newlyGeneratedSourceFile);
   }

   private File getGeneratedSourceFile(KB kb, PTerm[] terms) {
      try {
         List<ClauseModel> implications = createClauseModels(terms);
         return getGeneratedSourceFile(kb, implications);
      } catch (Exception e) {
         throw new RuntimeException(e + " generating source file for terms: " + Arrays.toString(terms), e);
      }
   }

   private List<ClauseModel> createClauseModels(PTerm[] terms) {
      List<ClauseModel> implications = new ArrayList<>();
      for (PTerm t : terms) {
         ClauseModel ci = ClauseModel.createClauseModel(t);
         implications.add(ci);
      }
      return implications;
   }

   /**
    * Translate the specified clauses into Java syntax.
    * 
    * @param implications the Prolog clauses to convert
    * @return newly created file containing Java source code
    */
   private File getGeneratedSourceFile(KB kb, List<ClauseModel> implications) {
      CompiledPredicateWriter writer = new CompiledPredicateWriter(kb, implications);
      // Note: as we are only generating the java source code here
      // we are not testing that it would actually compile.
      // testScriptCompiles() tests the script compiles and
      // the system tests verify that it works as expected.
      new CompiledPredicateSourceGenerator(writer).generateSource();
      outputDir.mkdirs();
      return writer.save(outputDir);
   }

   private static void assertContentsMatch(File expected, File actual) {
      assertExists(expected);
      assertExists(actual);

      int lineCtr = 0;
      try (FileReader fr1 = new FileReader(expected);
                  FileReader fr2 = new FileReader(actual);
                  BufferedReader br1 = new BufferedReader(fr1);
                  BufferedReader br2 = new BufferedReader(fr2)) {
         String name1 = expected.getName();
         String name2 = actual.getName();

         while (true) {
            lineCtr++;
            String next1 = getNextNonEmptyLine(br1);
            String next2 = getNextNonEmptyLine(br2);
            if (next1 == null && next2 == null) {
               return; // reached end of both files
            }
            if (next1 == null) {
               throw new RuntimeException(name1 + " ends before " + name2);
            }
            if (next2 == null) {
               throw new RuntimeException(name2 + " ends before " + name1);
            }
            next1 = tokenFilterFilename(next1, actual);
            if (next1.equals(next2) == false) {
               throw new RuntimeException("[" + next1 + "] in " + name1 + " not equals to [" + next2 + "] in " + name2 + " line " + lineCtr);
            }
         }
      } catch (Exception e) {
         throw new RuntimeException("Comparing " + expected.getAbsolutePath() + " to " + actual.getAbsolutePath() + " caused " + e, e);
      }
   }

   private static void assertExists(File f) {
      if (f.exists() == false) {
         throw new RuntimeException("File does not exist: " + f.getAbsolutePath());
      }
   }

   private static String getNextNonEmptyLine(BufferedReader br) throws IOException {
      String next;
      do {
         next = br.readLine();
         if (next == null) {
            return null; // reached end of file
         }
         next = next.trim();
      } while (next.length() == 0);
      return next;
   }

   /**
    * As the class names of the dynamically created files will be different each time, token filter the class name
    * contained in the expected results so that differences in class names are ignored as part of the comparison.
    */
   private static String tokenFilterFilename(String line, File f) {
      String fileName = f.getName();
      String className = fileName.substring(0, fileName.indexOf('.'));
      return line.replace("%CLASS_NAME%", className);
   }

   private void addSingletonCompiledPredicate(String keySyntax) {
      addPredicateFactory(keySyntax, new DummySingletonCompiledPredicate());
   }

   private void addRetryableCompiledPredicate(String keySyntax) {
      addPredicateFactory(keySyntax, new DummyRetryableCompiledPredicate());
   }

   private void addMultipleRulesWithSingleImmutableArgumentPredicate(String keySyntax, String[] argumentSyntax) {
      PTerm[] args = new PTerm[argumentSyntax.length];
      for (int i = 0; i < argumentSyntax.length; i++) {
         args[i] = TestUtils.parseTerm(argumentSyntax[i]);
      }
      addPredicateFactory(keySyntax, new MultipleRulesWithSingleImmutableArgumentPredicate(args, null));
   }

   private void addMultipleRulesWithMulipleImmutableArgumentPredicate(String keySyntax, String[][] argumentSyntax) {
      PTerm[][] args = new PTerm[argumentSyntax.length][argumentSyntax[0].length];
      for (int i1 = 0; i1 < argumentSyntax.length; i1++) {
         for (int i2 = 0; i2 < argumentSyntax[0].length; i2++) {
            args[i1][i2] = TestUtils.parseTerm(argumentSyntax[i1][i2]);
         }
      }
      addPredicateFactory(keySyntax, new MultipleRulesWithMultipleImmutableArgumentsPredicate(args, null));
   }

   private void addSingleRulesWithSingleImmutableArgumentPredicate(String keySyntax, String argumentSyntax) {
      PTerm t = TestUtils.parseTerm(argumentSyntax);
      addPredicateFactory(keySyntax, new SingleRuleWithSingleImmutableArgumentPredicate(t, null));
   }

   private void addSingleRulesWithMulipleImmutableArgumentPredicate(String keySyntax, String[] argumentSyntax) {
      PTerm[] args = new PTerm[argumentSyntax.length];
      for (int i = 0; i < argumentSyntax.length; i++) {
         args[i] = TestUtils.parseTerm(argumentSyntax[i]);
      }
      addPredicateFactory(keySyntax, new SingleRuleWithMultipleImmutableArgumentsPredicate(args, null));
   }

   private void addUserDefinedPredicate(String keySyntax) {
      addPredicateFactory(keySyntax, new CompiledPredicate() {
         @Override
         public void setKB(KB kb) {
         };

         @Override
         public Predicate getPredicate(PTerm... args) {
            return this;
         }

         @Override
         public boolean evaluate(PTerm... args) {
            return true;
         }

         @Override
         public boolean isRetryable() {
            return true;
         }

         @Override
         public boolean couldReEvaluationSucceed() {
            return true;
         }
      });
   }

   private void addPredicateFactory(String keySyntax, PredicateFactory ef) {
      PTerm t = TestUtils.parseSentence(keySyntax + ".");
      PredicateKey key = PredicateKey.createFromNameAndArity(t);
      DummyUserDefinedPredicateFactory cp = new DummyUserDefinedPredicateFactory(key, ef);
      debugEnabledKB.addDefined(cp);
      debugDisabledKB.addDefined(cp);
   }

   private class DummyUserDefinedPredicateFactory extends StaticUserDefinedPredicateFactory {
      final PredicateFactory ef;

      DummyUserDefinedPredicateFactory(PredicateKey key, PredicateFactory ef) {
         super(key);
         this.ef = ef;
      }

      @Override
      public PredicateFactory getActualPredicateFactory() {
         return ef;
      }
   }

   private class DummySingletonCompiledPredicate extends AbstractSingletonPredicate implements CompiledPredicate {
      @Override
      public boolean evaluate(PTerm... args) {
         return false;
      }
   }

   private class DummyRetryableCompiledPredicate extends AbstractRetryablePredicate implements CompiledPredicate {
      @Override
      public boolean evaluate(PTerm... args) {
         return false;
      }

      @Override
      public Predicate getPredicate(PTerm... args) {
         return this;
      }
   }
}