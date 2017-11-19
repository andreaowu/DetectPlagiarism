public class Main {

    public static void main(String[] args) {
        int tupleSize = 3;
        String synonyms, fileToCheck, fileWithTuples;

        // Process the arguments
        try {
            synonyms = args[0];
            fileToCheck = args[1];
            fileWithTuples = args[2];
        } catch (Exception exception) {
            System.out.println("Usage: java [synonymFile] [file1] [file2] [tupleSize]");
            System.out.println("where: ");
            System.out.println("synonymFile         file path to file containing all synonyms");
            System.out.println("file1               file path to file containing text to check for plagarism");
            System.out.println("file2               file path to file containing text to check for plagarism");
            System.out.println("tupleSize           size of tuples to check with; optional argument defaulted to 3");
            System.out.println("Output: Percentage of file1's tuples found in file2");
            return;
        }
        try {
            // Since tuple size is an optional argument, put it in separate try/catch
            tupleSize = Integer.parseInt(args[3]);
        } catch (Exception e) {
            // Don't need to do anything here
        }

        DetectPlagiarismFromFiles detectPlagiarism = new DetectPlagiarismFromFiles(synonyms, fileToCheck, fileWithTuples, tupleSize);
        detectPlagiarism.plagiarismCheck();
    }

}