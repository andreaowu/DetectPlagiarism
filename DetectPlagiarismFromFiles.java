import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;

/**
 * This class extends the DetectPlagiarism base class and implements calls to get the text in the synonyms
 * and two files to compare for plagiarism.
 */
public class DetectPlagiarismFromFiles extends DetectPlagiarism {

    private String synonyms;
    private String fileToCheck;
    private String fileWithTuples;

    DetectPlagiarismFromFiles(String synonyms, String fileToCheck, String fileWithTuples, int tupleSize) {
        super(tupleSize);
        this.synonyms = synonyms;
        this.fileToCheck = fileToCheck;
        this.fileWithTuples = fileWithTuples;
    }

    /**
     * Checks for plagiarism with given synonyms, texts, and tuple size.
     */
    @Override
    public void plagiarismCheck() {
        // Get file lengths to see if either plagiarism file is empty
        long fileLength = new File(fileToCheck).length();
        long checkLength = new File(fileWithTuples).length();
        if (fileLength == 0 && checkLength == 0) {
            System.out.println("100%");
        } else if (fileLength == 0 || checkLength == 0) {
            System.out.println("0%");
        } else {
            // Get synonyms, then all tuples from the first file, then perform the check
            synonymMappings = getSynonymsFromFile(synonyms);
            allHashedStrings = getAllTuplesFromFile(fileWithTuples);
            if (allHashedStrings == null || allHashedStrings.size() == 0) {
                System.out.println("0%");
            }
            System.out.printf("%.2f", calculatePlagiarismFromFile(fileToCheck) * 100);
            System.out.println("%");
        }
    }

    /**
     * Returns a table containing all mappings for synonyms in the given filePath.
     * For each word in each of filePath's lines, there will be an entry in the table
     * where the key is the word and the value is the hashCode of synonyms line for that word,
     * enclosed in brackets.
     *
     * @param filePath an absolute URL giving base location of synonyms file
     * @return Hashtable<String, Integer> with every word in filePath as key and the hashcode
     * of each word's complete list of synonyms as value
     */
    private Hashtable<String, Integer> getSynonymsFromFile(String filePath) {
        Hashtable<String, Integer> mappings = new Hashtable<>();
        try {
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNextLine()) {
                mappings = getAllSynonyms(scanner.nextLine(), mappings);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find file at: " + filePath);
        }
        return mappings;
    }

    /**
     * Opens filePath and reads word by word to get all tuples in the file.
     *
     * @param filePath an absolute URL giving base location of file from which to get tuples for
     *                 comparison
     * @return HashSet<Integer> set of all hashed word/synonym hashcodes of size tupleSize
     */
    private HashSet<Integer> getAllTuplesFromFile(String filePath) {
        try {
            Scanner scanner;
            scanner = new Scanner(new File(filePath));
            return getAllTuples(scanner);
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find file at: " + filePath);
            return null;
        }
    }

    /**
     * Opens filePath and calculates plagiarism using text from filePath.
     *
     * @param filePath an absolute URL giving base location of file from which to check for plagiarism
     * @return float percentage of plagiarised text
     */
    private float calculatePlagiarismFromFile(String filePath) {
        try {
            Scanner scanner;
            scanner = new Scanner(new File(filePath));
            return calculatePlagiarism(scanner);
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find file at: " + filePath);
            return 0;
        }
    }

    /**
     * Note: Instead of both getAllTuplesFromFile() and calculatePlagiarismFromFile() which basically
     * have the same code except for one line - the function call after getting the scanner. I thought
     * about making an enum as such: private enum Process { TUPLE, PLAGIARISM },
     * and then combining the two functions into one as such:
     private void getFromFile(String filePath, Process process) {
        try {
         Scanner scanner;
         scanner = new Scanner(new File(filePath));
         switch (process) {
             case TUPLE:
                allHashedStrings = getAllTuples(scanner);
             case PLAGIARISM:
                plagiarismCount = calculatePlagiarism(scanner);
             }
         } catch (FileNotFoundException e) {
            System.out.println("Couldn't find file at: " + filePath);
         }
     }
     * But, I created a new variable called plagiarismCount as a private variable for this class, and
     * this getFromFile() would assign the variables according to the {process} argument. This seems to
     * defeat the point of modularity (as the return type is void and assigning the variables straight
     * from this function seems to be a hacky way to go about this). Also, if more functions that require
     * reading from the scanner need to be made later, there may be even more unnecessary class variables
     * made for those calls...or the functions need to get broken up to return the correct return types
     * for each function call inside getFromFile(). So, for modularity, code clarity, and future-addition
     * purposes, I scrapped this idea and reverted it back to what is above.
     */
}
