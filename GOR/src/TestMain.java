public class TestMain {
    public static void main(String[] args) {
        String ssSeq = "--------HEHHHHECCHCHEHEHEHCHEHCEHCEHCEHCHHHHHHHHHHCEEEEEEECEEECCCCCCCHHHHHHCCC--------";
        char[] ssSeqArray = ssSeq.toCharArray();
        String ssSeqPost = new String(ssSeqArray);
        System.out.println(ssSeqPost);

        // iterate only over ssSeq
        for (int i = 8; i < ssSeqArray.length - 8; i++) {
            if (ssSeqArray[i] == 'H') {
                int hCounter = 1;
                int j = i;
                // looking forward
                while (ssSeqArray[j] == 'H') {
                    hCounter++;
                    j++;
                }
                int forward = j; // save distance traveled → direction
                j = i;
                while (ssSeqArray[j] == 'H') {
                    hCounter++;
                    j--;
                }
                int backwards = j; // save distance traveled → direction

                // we do something
                if (hCounter < 5) {
                   // if (ssSeqArray[]) {

                    // }
                }
            }

        }
    }
}
