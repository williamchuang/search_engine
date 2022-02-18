import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * Whenever the user needs to write out either the inverted index or the search
 * results in pretty JSON style, the data structure is parsed through this class
 * to write out.
 * 
 * @author William_Chuang
 * @version 2.01, 19 March 2018
 */
public class JSONWriter {

	/**
	 * The method is called by the "Driver.jave" and loop through the data structure
	 * and write to the output file.
	 * 
	 * 
	 * @see Driver.java The driver class
	 * @param outPutFile
	 *            The file that contains all the InvertedIndex, and is written in
	 *            pretty JSON style.
	 * @param index
	 *            data structure of the InvertedIndex
	 */
	public void writeNestedObject(String outPutFile, TreeMap<String, TreeMap<String, TreeSet<Integer>>> index) {
		TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements = index;
		
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outPutFile.toString())));
			writer.append("{" + "\n");
			int counter = 0;
			for (Map.Entry<String, TreeMap<String, TreeSet<Integer>>> wordEntry : elements.entrySet()) {
				writer.append(indent(1) + quote(wordEntry.getKey().toString()) + ":" + " { \n");
				int counrecket = 0;
				for (Entry<String, TreeSet<Integer>> pathEntry : wordEntry.getValue().entrySet()) {
					counrecket++;
					String currentPath = pathEntry.getKey();
					writer.append(indent(2) + quote(currentPath) + ": [" + "\n");
					writer.append(indent(3));
					for (Integer c : pathEntry.getValue()) {
						if (c != pathEntry.getValue().last()) {
							writer.append(c.toString() + "," + "\n" + indent(3));
						} else
							writer.append(c.toString() + "\n");
					}
					if (counrecket < wordEntry.getValue().entrySet().size()) {
						writer.append(indent(2) + "]," + "\n");
					} else {
						writer.append(indent(2) + "]" + "\n");
					}
				}
				counter++;
				if (counter < elements.entrySet().size()) {
					writer.append(indent(1) + "}," + "\n");
				} else {
					writer.append(indent(1) + "}" + "\n");
				}
			}
			writer.append("}" + "\n");
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("Exception thrown  : FileNotFoundException");
		} catch (IOException e) {
			System.out.println("Exception thrown  : IOException");
		}
	}

	/**
	 * The method is called by the "Driver.jave" and loop through the data structure
	 * of the given query file. The data structure is built in SearchResultManager,
	 * and write to the output file.
	 * 
	 * @param path
	 *            - the file location to print the results to
	 * @param finishedBuildQuery
	 *            - the data structure containing the results to be printed
	 * @see Driver.java
	 * @param outPutFile
	 */
	public void outputmyforge(TreeMap<String, List<SearchResult>> result, Path path) {
		
		try (BufferedWriter writer = Files.newBufferedWriter(path);) {
			writer.write("[\n");
			int myCounter = 0;

			if (!result.isEmpty()) {
				int count = 0;
				for (String key : result.keySet()) {
					writer.write(indent(1) + "{\n");
					int SearchResultCount = 0;
					if (myCounter == 0) {
						writer.write(indent(2) + quote("queries") + ": " + quote(key) + "," + "\n" + indent(2)
								+ quote("results") + ": [");
						myCounter++;
					} else {
						writer.write(indent(2) + quote("queries") + ": " + quote(key) + "," + "\n" + indent(2)
								+ quote("results") + ": [");
					}
					for (SearchResult qq : result.get(key)) {
						int size = result.get(key).size();
						if (SearchResultCount == size - 1) {
							writer.write("\n" + indent(3) + "{\n" + indent(4) + quote("where") + ": "
									+ quote(qq.getLocation()) + ",\n" + indent(4) + quote("count") + ": "
									+ qq.getFrequency() + ",\n" + indent(4) + quote("index") + ": " + qq.getPosition()
									+ "\n" + indent(3) + "}");
						} else {
							writer.write("\n" + indent(3) + "{\n" + indent(4) + quote("where") + ": "
									+ quote(qq.getLocation()) + ",\n" + indent(4) + quote("count") + ": "
									+ qq.getFrequency() + ",\n" + indent(4) + quote("index") + ": " + qq.getPosition()
									+ "\n" + indent(3) + "},");
						}
						SearchResultCount++;
					}
					writer.write("\n" + indent(2) + "]\n");
					count++;
					if (count != result.keySet().size()) {
						writer.write(indent(1) + "},\n");
					} else {
						writer.write(indent(1) + "}");
					}

				}

				writer.write("\n]");
			}
			myCounter = 0;
			writer.write("\n");
		} catch (IOException e) {
			System.err.println("Exception thrown  : IOException.");
		}
	}

	/**
	 * Helper method for quoting a string.
	 * 
	 * <pre>
	 * String input = "hi there";
	 * System.out.println(quote(input)); // print: "hi there"
	 * </pre>
	 *
	 * @param text
	 *            input to surround with quotation marks
	 * @return quoted text
	 */
	public static String quote(String input) {
		return "\"" + input + "\"";
	}

	/**
	 * Helper method for write indent string with the n times spacings.
	 * 
	 * @return an indent string
	 * @throws IOException
	 */
	private static String indent(int times) {
		return new String(new char[times]).replace("\0", "\t");
	}
	
	

}
