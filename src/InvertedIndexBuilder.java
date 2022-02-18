import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.TreeSet;

/**
* Obtain the input data structure, and the directory, traverse through each
* sub-directory, open all files that meet the requirements, by
* dirTraverser(Path dir, InvertedIndex i), and traverse(Path path,
* InvertedIndex index).
* 
* Secondly, it is responsible for calling HTMLCleaner, and WordParser to clean
* the content of the opened files. Then generate an array for deriving the word
* index. Finally, store the index, and if the word is new, then store a new key
* with position into the given data structure.
* 
* @author William_Chuang
* @version 2.01, 19 March 2018
*/

public class InvertedIndexBuilder {
	

	/**
	 * The method receives the path and an inverted index object, and open files
	 * from the given path.
	 * 
	 * @param path
	 *            - the path of the file that is going to be opened
	 * @param I
	 *            - which is the data structure
	 * @throws IOException
	 */
	public static void dirTraverser(Path dir, InvertedIndex i) {
		try {
			if (Files.isDirectory(dir)) {
				traverse(dir, i);
			} else {
				if (dir.getFileName().toString().trim().toLowerCase().endsWith(".html")) {
					fileParser(dir, i);
				}
				if (dir.getFileName().toString().trim().toLowerCase().endsWith(".htm")) {
					fileParser(dir, i);
				}
			}
		} catch (IOException e) {
			System.out.println("IOException - Input file doesn't exist");
		}
	}
	

	/**
	* Traversing directories. Do the recursive calls, traverse through each
	* sub-directory.
	* 
	* @param path
	*            - the path of the file that is going to be open
	* @param index
	*            - the data structure
	* @throws IOException
	*/
	private static void traverse(Path path, InvertedIndex index) throws IOException {

		try (DirectoryStream<Path> mylist = Files.newDirectoryStream(path)) {

			for (Path element : mylist) {

				if (Files.isDirectory(element)) {
					dirTraverser(element, index);
				} else {
					if (element.getFileName().toString().trim().toLowerCase().endsWith(".html")) {
						fileParser(element, index);
					}
					if (element.getFileName().toString().trim().toLowerCase().endsWith(".htm")) {
						fileParser(element, index);
					}
				}
			}
		}
	}

	
	/**
	 * Open the file that matches the requirements. Parsing the contents of files
	 * into the data structure.
	 * 
	 * @param file
	 *            - the path of the file that's going to be opened
	 * @param index
	 *            - the data structure
	 * @throws IOException
	 */
	public static void fileParser(Path file, InvertedIndex index) throws IOException {

		String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);	
		String line = HTMLCleaner.stripHTML(content);
		String[] words = WordParser.parseWords(line);
		int position = 0;
		for (String k : words) {
			if (!k.isEmpty()) {
				position++;
				TreeMap<String, TreeSet<Integer>> path_position = new TreeMap<String, TreeSet<Integer>>();
				index.add(k, file.toString(), position, path_position);
			}
		}
	}
}
