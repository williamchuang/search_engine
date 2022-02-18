import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class MultithreadedQueryBuilder {
	private final TreeMap<String, List<SearchResult>> result;
	private final ThreadSafeInvertedIndex multipleIndex;
	private final WorkQueue workers;

	/**
	 * Constructs a map that can hold words that were parsed from the input query
	 * files.
	 * 
	 * @param index
	 *            - the inverted index passed to the queryHelper
	 * @param workers
	 *            - the work queue
	 */

	public MultithreadedQueryBuilder(ThreadSafeInvertedIndex index, WorkQueue workers) {
		this.workers = workers;
		result = new TreeMap<String, List<SearchResult>>();
		multipleIndex = index;
	}

	/**
	 * Parses the input query file, loop through each line of the file to a new
	 * worker waiting in a work queue. After this is done, we do the partial search
	 * by calling the partialForage in the runnable.
	 * 
	 * @param file
	 *            - the location path of the query
	 * 
	 * @param searchFlag
	 *            - the flag that determines whether the search is partial or exact
	 */

	public void parseQuery(Path file, int searchFlag) {
		try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"));) {
			String line;
			while ((line = reader.readLine()) != null) {

				workers.execute(new QueryRunner(line, searchFlag));
			}
			workers.finish();
			reader.close();
		} catch (IOException e) {
			System.out.println("Cannont read the input query file.");
		}
	}

	private class QueryRunner implements Runnable {
		private String line;
		private int flag;

		public QueryRunner(String line, int flag) {
			this.line = line;
			this.flag = flag;
		}

		@Override
		public void run() {

			String[] queryKeys = WordParser.parseWords(line);
			Arrays.sort(queryKeys);
			String word = String.join(" ", queryKeys);
			if (!word.equals("")) {
				if (flag == 1) {
					List<SearchResult> results = multipleIndex.exactForage(queryKeys);
					synchronized (result) {
						result.put(word, results);
					}
				} else if (flag == 0) {
					List<SearchResult> results = multipleIndex.partialForage(queryKeys);
					synchronized (result) {
						result.put(word, results);
					}
				}
			}
		}
	}

	public void printHelper(Path path) {
		JSONWriter mWriter = new JSONWriter();
		mWriter.outputmyforge(result, path);
	}

}
