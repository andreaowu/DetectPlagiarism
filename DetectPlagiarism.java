import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This class detects plagiarism given a synonyms list, two iterable texts to check against each other, and
 * the tuple size. First, a table is made for tracking synonyms. It parses the synonyms list and for
 * each word found, the table has an entry with the word as the key and the hashcode of the synonyms
 * list (including that word) as the value. Next, it parses one iterable text to get all the tuples in that
 * file, and a set keeps track of all the tuples from this file. For each tuple, it replaces the word
 * with its corresponding synonyms hashcode value if possible, otherwise the word is present in the tuple.
 * The tuple's hashcode is put into the set. Finally, it parses the other iterabe text to check against the first
 * file. The hashcode for every tuple in this file is checked against the set from the first file, and
 * the percentage of found tuples is calculated.
 *
 * Complexity:
 * O(n) where n is the number of words in the larger text.
 *
 * Assumptions:
 * - a word in the synonyms list will be found only one time in the entire synonyms list
 * - hashcode for each string will be unique and have no collisions
 *      -> If this assumption is wrong, fix it by keeping a hashtable for allHashedStrings
 *         where the key is the hashcode and the value is a set of tuples (strings) that have that
 *         hashcode. Every time allHashedStrings is checked for a tuple's hashcode, check if
 *         that tuple string is in the value set for the given word.
 * - duplicates count as plagiarism (ie if synonyms have a line with "run jog sprint",
 *   file1 has "go run now go jog now", and file2 has "go jog now". the outcome will be 100%)
 *      -> If this assumption is wrong, delete words from set of tuples every time a tuple is seen
 * - memory is big enough to fit all words in synonyms text and hashcodes from first text's tuples
 *      -> If this assumption is wrong, use a database. Store all words/hashcodes in local
 *         memory if possible and use local memory as a cache. Use a LinkedHashMap/LinkedHashSet instead of a
 *         set to preserve the order of insertion. If adding a new entry to the LinkedHashMap/LinkedHashSet
 *         causes it to run out of memory, insert the first n items to the database and remove them
 *         from LinkedHashMap/LinkedHashSet, where n is the maximum number of items that can fit per
 *         chunk of database memory accessed per time. After adding all tuple's hashcodes into the set,
 *         write the result into the database as well. Keep that hashmap/hashset in local memory.
 *         For checking if an item is in the set, if it's there, great. If not, remove the first
 *         item out of the map/set and check the databse for it. If it's there, add it to the map/set. Otherwise,
 *         the item is actually not there, and handle as such.
 */
public abstract class DetectPlagiarism {

    protected int tupleSize;

    // Key: word, value: synonyms string's hashcode for word
    protected Hashtable<String, Integer> synonymMappings;

    // Set of all hashcodes for tuples in fileToCheck
    protected HashSet<Integer> allHashedStrings;

    DetectPlagiarism(int tupleSize) {
        this.tupleSize = tupleSize;
        synonymMappings = new Hashtable<>();
        allHashedStrings = new HashSet<>();
    }

    /**
     * Checks for plagiarism with given synonyms, texts, and tuple size.
     * Each class that extends DetectPlagiarism will have its own plagiarismCheck, because it will
     * need to get the synonyms and texts in its own way.
     */
    public abstract void plagiarismCheck();

    /**
     * Returns a table containing given table's mappings as well as entries for each word in given line.
     * For each word in give line, there will be an entry in the table where the key is the word and the
     * value is the hashCode of synonyms line for that word,
     * enclosed in brackets.
     *
     * Note: this function can be reused for different kinds of contexts, assuming synonyms will
     * always be given in a line. A new hashtable needs to be made in the function that calls this
     * function, then hashtable can keep being updated accordingly by calling this function.
     *
     * @param line line with words that are synonyms with each other
     * @param mappings table with every word seen so far and its corresponding synonyms hashcode
     * @return Hashtable<String, Integer> with given line's addition of words and newly calculated
     * corresponding synonyms hashcode
     */
    protected Hashtable<String, Integer> getAllSynonyms(String line, Hashtable<String, Integer> mappings) {
        // Key: word, value: synonyms string's hashcode for word
        for (String word : line.split(" ")) {
            mappings.put(word, line.hashCode());
        }
        return mappings;
    }

    /**
     * Makes tuples of size tupleSize as each word is seen and replaces word with synonyms hashcode
     * when possible. Puts the hashcode of each tupleSize'd string into a set and returns the set.
     *
     * @param wordsIterator iterator with words to form tuples and their respective synonym hashcodes
     * @return HashSet<Integer> set of all hashed word/synonym hashcodes of size tupleSize
     */
    protected HashSet<Integer> getAllTuples(Iterator<String> wordsIterator) {
        // Set of all hashcodes for tuples in fileToCheck
        HashSet<Integer> allTuples = new HashSet<>();

        // For each word, delete first synonym hashcode / word in curr, then add new word's synonym
        // hashcode if it has one to curr, otherwise add word to curr and put its hash into allTuples
        String curr = "";
        while (wordsIterator.hasNext()) {
            String word = wordsIterator.next();
            if (curr.length() == 0) {
                curr = getFirstTuple(wordsIterator, word);
                allTuples.add(curr.hashCode());
                continue;
            }
            curr = getNextTuple(curr.substring(curr.indexOf("]") + 2), word);
            allTuples.add(curr.hashCode());
        }
        return allTuples;

    }

    /**
     * Makes the first tuple in the file. Iterates through the scanner until it has processed
     * enough words to make a tuple of size tupleSize. For each word, concatenate either the word
     * itself or the word's corresponding synonyms list's hashcode to the existing string.
     *
     * @param wordsIterator from which to get each word from
     * @param word to be looked up in the synonyms table and replaced with its synonyms list's hashcode
     * @return String of words replaced with its corresponding synonyms list
     */
    private String getFirstTuple(Iterator<String> wordsIterator, String word) {
        int countWords = 1;
        String firstTuple = getNextTuple("", word);
        while (countWords < tupleSize && wordsIterator.hasNext()) {
            word = wordsIterator.next();
            firstTuple = getNextTuple(firstTuple, word);
            countWords += 1;
        }
        return firstTuple;
    }

    /**
     * Make the next tuple to be examined. Removes the first word / synonyms hashcode in the current
     * string. Finds the synonym hashcode for the given word and concatenates it to the current string.
     * If the given word has no synonyms, concatenate just the word, enclosed in brackets.
     *
     * @param currentString current string to be edited
     * @param nextWord word to be added next to the current string
     * @return String with new word / corresponding synonyms list concatenated
     */
    private String getNextTuple(String currentString, String nextWord) {
        return currentString.concat(synonymMappings.get(nextWord) != null
                ? "[" + synonymMappings.get(nextWord) + "] " : "[" + nextWord + "] ");
    }

    /**
     * Makes tuples of size tupleSize as each word is read, and replaces word with synonyms hashcode
     * when possible. Keep track of the number of tuples seen, and for each tuple, check if its hashcode
     * is in allTuples. If so, add it to the plagiarism count. At the end, divide plagiarism count by
     * the number of tuples to get the percentage of plagiarism in the given file.
     *
     * @param wordsIterator iterator with words to form tuples and check for plagiarism
     * @return float percentage of plagiarised text
     */
    protected float calculatePlagiarism(Iterator<String> wordsIterator) {
        // Keep track of tuple string, total number of tuples seen, and total number of plagiarized tuples
        String curr = "";
        float tupleCount = 0;
        float plagiarizedCount = 0;

        // For each word, delete first synonym hashcode / word in curr, then add new word's synonym hashcode
        // if it has one to curr, otherwise add word to curr and check for its hash in allHashedStrings
        while (wordsIterator.hasNext()) {
            String word = wordsIterator.next();
            if (curr.length() == 0) {
                curr = getFirstTuple(wordsIterator, word);
                tupleCount += 1;
                plagiarizedCount = allHashedStrings.contains(curr.hashCode())
                        ? plagiarizedCount + 1 : plagiarizedCount;
                continue;
            }
            curr = getNextTuple(curr.substring(curr.indexOf("]") + 2), word);
            tupleCount += 1;
            plagiarizedCount = allHashedStrings.contains(curr.hashCode())
                    ? plagiarizedCount + 1 : plagiarizedCount;
        }
        return plagiarizedCount / tupleCount;
    }
}
