package nars.util.utf8;

/**
 * Has and can change its byte representation
 *
 */
public interface Byted {

    /**
     * ordinary array equals comparison with some conditions removed
     * instance equality between A and B will most likely already performed prior to calling this, so it is not done in this method
     */
    static boolean equals(final Byted A, final Byted B) {
        final byte[] a = A.bytes();
        final byte[] b = B.bytes();

        if (a == b)
            return true;

        if (A.hashCode() != B.hashCode())
            return false;

        return isEqualContentAndMergeIfTrue(a,B,b);
    }

    static int compare(final Byted A, final Byted B) {
        /*int i = Integer.compare(A.hashCode(), B.hashCode());
        if (i != 0)
            return i;*/



        final byte[] a = A.bytes();
        final byte[] b = B.bytes();

        if (a == b)
            return 0;


        int minLength = Math.min(a.length, b.length);

        for(int i = 0; i < minLength; ++i) {
            int compareResult = a[i] - b[i];
            if(compareResult != 0) {
                return compareResult;
            }
        }

        final int r = a.length - b.length;
        if (r == 0) {
            //determined to be equal, share instances
            B.setBytes(a);
        }

        return r;
    }

    /** separate method so that the base equals() method can be more easily inlined */
    static boolean isEqualContentAndMergeIfTrue(final byte[] a, final Byted B, final byte[] b) {

        final int aLen = a.length;
        if (b.length != aLen)
            return false;

        //backwards
        for (int i = aLen - 1; i >= 0; i--)
            if (a[i] != b[i]) {
                //if this happens, it could indicate a BAD HASHING strategy
                return false;
            }

        //merge the two instances
        B.setBytes(a);

        return true;
    }

    public byte[] bytes();

    public void setBytes(byte[] b);


}
