import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
* To create a data structure to store the input data that read in from the
* input files.
* 
* @author William_Chuang
* @version 2.01, 19 March 2018
* 
*          This work complies with code that were finished in USFCA CS212
*          Homework (HTMLCleaner.java, WordParser.java).
*/


public class InvertedIndex {

	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;
	/**
	 * Constructor, initialize a new data structure - a nested TreeMap.
	 */
	public InvertedIndex() {
		this.index = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
	}

	
	/**
	 * This method is for adding data into the data structure.
	 * 
	 * @param k
	 *            - key
	 * @param dirFiles
	 *            - the given input directory
	 * @param position
	 *            - position of the word
	 * @param path_position
	 *            - path of the file to be opened
	 */
	public void add(String k, String dirFiles, int position, TreeMap<String, TreeSet<Integer>> path_position) {
		if (index.isEmpty() || !index.containsKey(k)) {
			path_position.put(dirFiles, new TreeSet<Integer>());
			path_position.get(dirFiles).add(new Integer(position));
			index.put(k, path_position);
		} else {
			if (!index.get(k).containsKey(dirFiles)) {
				index.get(k).put(dirFiles, new TreeSet<Integer>());
				index.get(k).get(dirFiles).add(new Integer(position));
			} else {
				index.get(k).get(dirFiles).add(new Integer(position));
			}
		}
	}
	
	/**
	 * Adds a node containing the word, file the word is located in, and the
	 * position of the word within the file to an inverted index
	 * 
	 * @param word
	 *            - the cleaned word from the parseFile function
	 * @param file
	 *            - the file which the specified word is located within
	 * @param position
	 *            - the location of the word within the designated file
	 */
		public void add(String word, String path, int position) {

		if (index.get(word) == null) {
			index.put(word, new TreeMap<String, TreeSet<Integer>>());
			index.get(word).put(path, new TreeSet<Integer>());
			index.get(word).get(path).add(position);
		} else {
			if (index.get(word).get(path) == null) {
				index.get(word).put(path, new TreeSet<Integer>());
				index.get(word).get(path).add(position);
			} else {
				index.get(word).get(path).add(position);
			}
		}
	}
	
	/**
	 * Add contents to tree-map in this class by calling a hash-map from another
	 * class of InvertedIndex.
	 * 
	 * @param words
	 *            String array of words
	 * @param path
	 *            path to store
	 */
	public void addAll(InvertedIndex other) {
		
		for (String word : other.index.keySet()) {
			
			if (this.index.containsKey(word) == false) {
				
				this.index.put(word, other.index.get(word));
				
			} else {
				
				for (String path : other.index.get(word).keySet()) {
					if (this.index.get(word).containsKey(path) == false) {
						this.index.get(word).put(path, other.index.get(word).get(path));
					} else {
						this.index.get(word).get(path).addAll(other.index.get(word).get(path));
					}

				}
			}
		}
	}
	
	/**
	 * A helper method to avoid duplicating the code. This method is called by both
	 * partialForage and exactForage.
	 * 
	 * @param myList -a list for accumulating the found query from SearchResult 
	 * 
	 * @param myTreeMap - found query in the inverted index data structure
	 * @param SearchResult  - a map for accumulating the found query
	 * -
	 *            
	 */

	public void searchHelper(List<SearchResult> myList, TreeMap<String, TreeSet<Integer>> myTreeMap,
			Map<String, SearchResult> SearchResult) {
		for (Map.Entry<String, TreeSet<Integer>> entry : myTreeMap.entrySet()) {
			TreeSet<Integer> set = entry.getValue();
			int frequency = set.size();
			int initalIndex = Collections.min(set);
			if (!SearchResult.containsKey(entry.getKey())) {
				SearchResult result = new SearchResult(frequency, entry.getKey(), initalIndex);
				SearchResult.put(entry.getKey(), result);
				myList.add(result);
			} else {
				SearchResult previousSearch = SearchResult.get(entry.getKey());
				previousSearch.updateIndex(initalIndex);
				previousSearch.updateFrequency(frequency);
			}
		}
	}
	
	
	/**
	 * Called by the Driver.java, and do the exact search in the InvertedIndex data
	 * structure for the given query.
	 * 
	 * @param input
	 *            - a given query
	 * @return myList - a list for summarizing the found query
	 */
	public List<SearchResult> exactForage(String[] input) {
		Map<String, SearchResult> exactResults = new HashMap<>();
		List<SearchResult> myList = new ArrayList<>();
		int i=0;
		exactForageHelper(i, input, exactResults, myList);
		Collections.sort(myList);
		return myList;
	}
	
	/**
	 * A helper method for exactForage. 
	 * 
	 * @param myList -a list for accumulating the found query from SearchResult 
	 * 
	 * @param input
	 *            - a given query
	 * @param exactResults  - a map for accumulating the found query
	 * -
	 *            
	 */
	public void exactForageHelper(int i, String[] input, Map<String, SearchResult> exactResults, List<SearchResult> myList) {
	    if (i < input.length) {
	    		if (this.index.containsKey(input[i])) {
	    				TreeMap<String, TreeSet<Integer>> map = this.index.get(input[i]);
	    				searchHelper(myList, map, exactResults);
			}
	    		i++;
	    		exactForageHelper(i, input, exactResults, myList);
	    }
	}
	

	/**
	 * Called by the Driver.java, and do the partial search in the InvertedIndex
	 * data structure for the given query.
	 * 
	 * @param input
	 *            - a given query
	 * @return myList - a list for summarizing the found query
	 */
	public List<SearchResult> partialForage(String[] input) {
		List<SearchResult> myList = new ArrayList<>();
		Map<String, SearchResult> partialSearchResult = new HashMap<>();
		for (int k = 0; k < input.length; k++) {
			String fromKey = input[k];
			if(input[k]!="") {
			for (Entry<String, TreeMap<String, TreeSet<Integer>>> e : this.index.tailMap(fromKey).entrySet()) {
				String key = e.getKey();
				TreeMap<String, TreeSet<Integer>> map = e.getValue();
				if (!key.startsWith(fromKey)) {
					break;
				} else {
					searchHelper(myList, map, partialSearchResult);
				}

			}
			}
		}
		Collections.sort(myList);
		return myList;
	}

	/**
	 * Check whether the map is empty.
	 * 
	 * @return return true when the inverted index is empty
	 */
	public boolean isEmpty() {
		return index.isEmpty();
	}
	
	/**
	 * Adds the array of words at once, assuming the first word in the array is at
	 * the provided starting position
	 *
	 * @param words
	 *            array of words to add
	 * @param start
	 *            starting position
	 */
	public void addAll(String[] words, int start, String dirFiles, TreeMap<String, TreeSet<Integer>> path_position) {
		/*
		 * TODO: Add each word using the start position. (You can call your other
		 * methods here.)
		 */
		int position = start;
		for (int i = 0; i <= words.length; i++) {
			add(words[i], dirFiles, position ,path_position);
			//add(words[i], position);
			position++;
		}

	}


	/**
	 * Prints the inverted index by iterating through the TreeMap of words, the
	 * TreeMap of files, and the TreeSet of integer positions, writing them to
	 * the designated output location in a pretty print JSON format
	 * 
	 * @param index
	 *            - the complete inverted index, populated with data
	 * @param path
	 *            - the designated location which the function will print to
	 * @throws IOException
	 *             - the exception through if the bufferedReader is unable to
	 *             print to the path
	 */
	public void print(Path path) {
		JSONWriter mWriter = new JSONWriter();
		mWriter.writeNestedObject(path.toString(), index);
	}



	/**
	 * Returns the size of the inverted index.
	 * 
	 * @return
	 */
	public int size() {
		return index.size();
	}

	/**
	 * Returns true if word is found in the inverted index, false is the word is
	 * not found in the inverted index.
	 * 
	 * @param word
	 *            the word being searched for in the index.
	 * @return
	 */
	public boolean containsWord(String word) {
		return index.containsKey(word);
	}

	/**
	 * To speed up: add local data structure to the global
	 * 
	 * @param localIndex
	 *            The temporary accumulated local inverted index
	 */
	public void addIndex(InvertedIndex localIndex) {
		for (Map.Entry<String, TreeMap<String, TreeSet<Integer>>> entry : localIndex.index.entrySet()) {
			String key = entry.getKey();
			if (index.containsKey(key)) {
				for (Map.Entry<String, TreeSet<Integer>> secondaryEntry : localIndex.index.get(key)
						.entrySet()) {
					String path = secondaryEntry.getKey();
					if (!index.get(key).containsKey(path)) {
						index.get(key).put(path, secondaryEntry.getValue());
					} else {
						index.get(key).get(path).addAll(localIndex.index.get(key).get(path));
					}
				}
			} else {
				index.put(key, entry.getValue());
			}
		}
	}
	
	/**
	 * A writer method for outputting the inverted index by calling JSONWriter
	 * class.
	 * 
	 */
	public void InvertedIndexWriter(String indexfilepath) {
		JSONWriter mWriter = new JSONWriter();
		mWriter.writeNestedObject(indexfilepath, index);
	}

	/**
	 * Adds quotes to any string, useful in pretty printing JSON
	 * 
	 * @param text
	 *            - the string to be quoted
	 * @return the string with quotations on it
	 */
	public static String quote(String text) {
		return String.format("\"%s\"", text);
	}

	/**
	 * @return the Inverted Index as a string
	 */
	@Override
	public String toString() {
		return index.toString();
	}


	/**
	 * split string array to single word and add word with path and position to
	 * index map.
	 * 
	 * @param words
	 * @param path
	 */
	public void addIndex(String[] words, String path) {
		int wordPosition = 0;
		for (String word : words) {

			wordPosition += 1;
	
			
			add(word, path, wordPosition);
			
		}
	}

}


