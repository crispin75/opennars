package nars.guifx;

import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import nars.NAR;
import nars.guifx.space.WebMap;
import nars.guifx.util.CodeInput;
import nars.guifx.util.TabXLazy;
import nars.guifx.wikipedia.NARWikiBrowser;
import nars.io.in.LibraryInput;
import nars.io.nlp.Twenglish;
import nars.nal.nal2.Similarity;
import nars.nal.nal7.Sequence;
import nars.term.Atom;
import nars.term.Term;

import java.util.Collection;
import java.util.Map;

import static javafx.application.Platform.runLater;
import static nars.guifx.NARfx.scrolled;


/**
 * Created by me on 8/11/15.
 */
public class InputPane extends TabPane {

    private final NAR nar;

    public InputPane(NAR n) {
        super();

        this.nar = n;

        setSide(Side.BOTTOM);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        getTabs().add(new TabXLazy("Narsese", () -> new NarseseInput(n)));

        getTabs().add(new TabXLazy("Library", LibraryInputPane::new));

        getTabs().add(new TabXLazy("Wiki", () -> {

            //"Navigate wikipedia to collect tags to use as input terms"
            return new NARWikiBrowser("Happiness");
        }));
        getTabs().add(new TabXLazy("Space", WebMap::new) /*"Space", "Navigate a 2D map to input (map region-as-shape analysis, and lists of features and their locations)")*/);


        getTabs().add(new TabXLazy("Natural", () ->
            //"Natural language input in any of the major languages, using optional strategies (ex: CoreNLP)"
            new NaturalLanguagePane(n)
        ));
        {
            /*getTabs().add(new Tab("En"));
            getTabs().add(new Tab("Es"));
            getTabs().add(new Tab("Fr"));
            getTabs().add(new Tab("De"));*/
        }
        getTabs().add(new ComingSoonTab("Sensors", "List of live signals and data sources which can be enabled, disabled, and reprioritized"));
        getTabs().add(new ComingSoonTab("Data", "Spreadsheet view for entering tabular data"));
        getTabs().add(new ComingSoonTab("Draw", "Drawing/composing an image that can be input"));
        getTabs().add(new ComingSoonTab("Webcam", "Webcam/video stream record; audio optional"));
        getTabs().add(new ComingSoonTab("Audio", "Microphone/audio stream record, w/ freq and noise analyzers, and optional speech recognition via multiple strategies"));


        getTabs().add(new ComingSoonTab("Time", "Navigate a timeline to view and edit significants in any time region"));
        getTabs().add(new ComingSoonTab("Patterns", "Frequently-used inputs and templates selectable via speed-dial button grid"));
        getTabs().add(new ComingSoonTab("URL", "Bookmarks that will create a new browser tab for web pages. Also includes URL navigation textfield"));

    }


    /**
     * Apps, APIs, Interfaces, and Examples (ex: from .NAL files) that can be input
     */
    private class LibraryInputPane extends SplitPane implements Runnable {


        final ListView<String> index = new ListView<>();
        final TextArea source = new TextArea();
        final Map<String, String> absPath;

        public LibraryInputPane() {
            super();

            GridPane buttons = new GridPane();
            buttons.setMaxHeight(Double.MAX_VALUE);
            buttons.addColumn(0,
                    new Button("Input"), //input immediately
                    new Button("Edit"), //copy the source to narsese input panel and switch to it, after verifying with user before needing to remove any existing code there
                    new Button("Append"), //append the source to current narsese input buffer
                    new Button("Fork") //fork a new NAR which will have this executed in it, while the current NAR remains the same
            );

            BorderPane bp = new BorderPane();
            bp.setCenter(scrolled(source));
            bp.setRight(buttons);

            getItems().setAll(scrolled(index), bp);

            absPath = nars.io.in.LibraryInput.getAllExamples();

            index.getItems().addAll(absPath.keySet());

            index.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            index.getSelectionModel().selectedItemProperty().addListener((e) -> {
                runLater(LibraryInputPane.this);
            });

        }

        @Override
        public synchronized void run() {

            StringBuilder sb = new StringBuilder();
            for (final String file : index.getSelectionModel().getSelectedItems()) {

                try {
                    LibraryInput x = LibraryInput.get(nar, absPath.get(file));
                    if (x != null)
                        sb.append(x.getSource()).append("\n\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            source.setText(sb.toString());

        }
    }

    static class NaturalLanguagePane extends CodeInput {

        final Twenglish te = new Twenglish();
        private final NAR nar;

        public NaturalLanguagePane(NAR n) {
            super();
            this.nar = n;
        }

        /** return false to indicate input was not accepted, leaving it as-is.
         * otherwise, return true that it was accepted and the buffer will be cleared. */
        public boolean onInput(String s) {

            te.parse(nar, s).forEach( nar::input );

            Collection<Term> tokens = Twenglish.tokenize(s);

            if (tokens == null)
                return false;
            else {
                if (!tokens.isEmpty())
                    nar.believe(
                        Similarity.make(
                            Atom.quote(s),
                            Sequence.makeSequence(
                                    tokens.toArray(new Term[tokens.size()])
                            )
                        )
                    );
                return true;
            }
        }
    }
}
