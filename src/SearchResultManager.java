import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * Storing all the search results corresponding to the query file.
 * 
 * @author William_Chuang
 * 
 * @version 2.01, 19 March 2018
 * 
 */
public class SearchResultManager {
	private final Charset charset = java.nio.charset.StandardCharsets.UTF_8;
	private final TreeMap<String, List<SearchResult>> myQuery;

	/**
	 * Initializes a new SearchResults object
	 */
	public SearchResultManager() {
		myQuery = new TreeMap<String, List<SearchResult>>();
	}

	/**
	 * Input the given query file, call Wordparser class to clean the words, adds
	 * them to an array list
	 * 
	 * @param path
	 *            - the path for inputting the given query and output to a file
	 * @return uniqueList - a sorted, unique list of Strings
	 */
	public void parseFile(String searchFlag, Path path, InvertedIndex invertedindex) {
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			List<SearchResult> mySearchList = new ArrayList<>();
			String input = reader.readLine();
			while ((input) != null) {
				String[] queryKeys = WordParser.parseWords(input);
				String temp_string = "";
				int counter = 0;
				for (String key_check : queryKeys) {
					counter++;
					if (key_check != "") {
						if (counter != queryKeys.length) {
							temp_string += key_check + " ";
						} else {
							temp_string += key_check;
						}
					}
				}
				queryKeys = temp_string.split(" ");
				Arrays.sort(queryKeys);
				if (searchFlag == "-exact") {
					mySearchList = invertedindex.exactForage(queryKeys);
				} else if (searchFlag == "-query") {
					if (temp_string != "") {
						mySearchList = invertedindex.partialForage(queryKeys);
					}
				}
				if (temp_string != "") {
					String text = String.join(" ", queryKeys);
					myQuery.put(text, mySearchList);
				}
				input = reader.readLine();
			}
		} catch (IOException e) {
			System.out.println("The input query file has an ERROR. " + path.toString());
		}
	}

	/**
	 * This is a helper method for outputting the search results.
	 * 
	 * @param path
	 *            - the file location to output the search results to
	 */
	public void printHelper(Path path) {
		JSONWriter mWriter = new JSONWriter();
		
		mWriter.outputmyforge(myQuery, path);

	}

}
