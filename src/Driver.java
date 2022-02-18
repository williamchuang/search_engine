import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Driver class performs main method, handles command-line arguments
 * {@code -path} <directory> to point out the exact directory to build the
 * output file of the inverted index. Secondly, {@code -index} is the flag for
 * pointing the directory <filename> of the output file. In project 2, it
 * accepts command-line arguments {@code -exact}, {@code -query}, and
 * {@code -result}.
 * 
 * @author William_Chuang
 * @version 3.01, 17 April 2018
 */

public class Driver {

	public static void main(String[] args) {
for(String i : args) {
	System.out.println(i);
}
		final int default_port = 8080;
		WorkQueue workers = null;
		MultithreadedQueryBuilder query;
		String directory = "";
		String indexfilepath = "";
		InvertedIndex invertedindex = new InvertedIndex();
		ArgumentParser argumentParser = new ArgumentParser(args);
		SearchResultManager mySearchResultManager = new SearchResultManager();
		
		
		final int default_maxURL = 50;
		try {
			if (argumentParser.getValue("-threads") != null && Integer.parseInt(argumentParser.getValue("-threads")) > 1
					&& argumentParser.hasFlag("-threads")) {
				int threadCount = 5;

				try {
					if (Integer.parseInt(argumentParser.getValue("-threads")) > 1) {
						threadCount = Integer.parseInt(argumentParser.getValue("-threads"));
					}
				} catch (Exception e) {
					System.out.println("Error with multithread argument");
				}
				workers = new WorkQueue(threadCount);
				ThreadSafeInvertedIndex threadSafeIndex = new ThreadSafeInvertedIndex();
				query = new MultithreadedQueryBuilder(threadSafeIndex, workers);
				ConcurrentWebCrawler webCrawler=null;
				BaseServer server =null;
				if (argumentParser.hasFlag("-path")) {
					try {
						Path dir = Paths.get(argumentParser.getValue("-path"));
						MultithreadedInvertedIndexBuilder.dirTraverser(dir, threadSafeIndex, workers);
					} catch (Exception e) {
						System.out.println("Error with directory argument");
					}
				}
				if (argumentParser.hasFlag("-url")) {

					URL seed = new URL(argumentParser.getString("-url"));
					int limit = default_maxURL;
					if (argumentParser.hasValue("-limit")) {
						limit = argumentParser.getInteger("-limit", default_maxURL);
					}
					webCrawler = new ConcurrentWebCrawler(threadSafeIndex, workers);
					System.out.println("reach");
					if(argumentParser.hasFlag("-port")){
						System.out.println("int inputPort");
						int inputPort = argumentParser.getInteger("-port", default_port);
						server = new BaseServer(threadSafeIndex, inputPort);
						
				
					}

				}
				if (argumentParser.hasFlag("-url")) {

					URL seed = new URL(argumentParser.getString("-url"));
					int limit = default_maxURL;

					if (argumentParser.hasValue("-limit")) {
						limit = argumentParser.getInteger("-limit", default_maxURL);
					}

					try {
						webCrawler.crawl(seed, limit);

					} finally {
						System.out.println("finish URL parsing");
					}
							

				}
				// look for "-port"
				if(argumentParser.hasFlag("-port")){
					System.out.println("argumentParser.hasFlag(\"-port\")");
					server.serverStarts();
				}

				if (argumentParser.hasFlag("-query") && argumentParser.hasValue("-query")) {
					String inputQuery = argumentParser.getValue("-query");
					Path inputQueryPath = Paths.get(inputQuery);
					if (argumentParser.hasFlag("-exact")) {
						if (argumentParser.hasValue("-exact")) {
							Path exactPath = Paths.get(argumentParser.getValue("-exact"));
							query.parseQuery(exactPath, 1);
						} else {
							Path exactPath = Paths.get(argumentParser.getValue("-query"));
							mySearchResultManager.parseFile("-exact", exactPath, invertedindex);
							query.parseQuery(inputQueryPath, 0);
						}
					} else {
						Path queryPath = Paths.get(argumentParser.getValue("-query"));
						mySearchResultManager.parseFile("-query", queryPath, invertedindex);
						query.parseQuery(inputQueryPath, 0);
					}
				}
				
				if (argumentParser.hasFlag("-index")) {
					String output = argumentParser.getValue("-index", "index.json");
					Path outputPath = Paths.get(output);
					workers.finish();

					threadSafeIndex.print(outputPath);
					
				}
				if (argumentParser.hasFlag("-results")) {
					String outputQuery = argumentParser.getValue("-results", "results.json");
					Path outputQueryPath = Paths.get(outputQuery);
					query.printHelper(outputQueryPath);
				}

			} else {
				WebCrawler crawler;

				if (argumentParser.hasFlag("-path")) {
					directory = argumentParser.getValue("-path");
					Path dir = Paths.get(directory);
					InvertedIndexBuilder.dirTraverser(dir, invertedindex);
				}

				if (argumentParser.hasFlag("-url") && argumentParser.hasValue("-url")) {
					String link = argumentParser.getValue("-url");
					int limit = default_maxURL;

					if (argumentParser.hasValue("-limit")) {
						limit = argumentParser.getInteger("-limit", default_maxURL);
					}
					try {
						crawler = new WebCrawler(invertedindex);
						crawler.crawl(new URL(link), limit);

					} catch (RuntimeException e) {
						e.printStackTrace();
						System.out.println("IOException");
					}

				}

				if (argumentParser.hasFlag("-query") && argumentParser.hasValue("-query")) {
					if (argumentParser.hasFlag("-exact")) {
						if (argumentParser.hasValue("-exact")) {
							Path exactPath = Paths.get(argumentParser.getValue("-exact"));
							mySearchResultManager.parseFile("-exact", exactPath, invertedindex);
						} else {
							Path exactPath = Paths.get(argumentParser.getValue("-query"));
							mySearchResultManager.parseFile("-exact", exactPath, invertedindex);
						}
					} else {
						Path queryPath = Paths.get(argumentParser.getValue("-query"));
						mySearchResultManager.parseFile("-query", queryPath, invertedindex);
					}
				}
				if (argumentParser.hasFlag("-results")) {
					Path results = Paths.get(argumentParser.getValue("-results", "results.json"));
					mySearchResultManager.printHelper(results);
				}

				if (argumentParser.hasFlag("-index")) {

					indexfilepath = argumentParser.getValue("-index", "index.json");

					invertedindex.InvertedIndexWriter(indexfilepath);
				}

			}
			if (workers != null) {
				workers.shutdown();
			}
		} catch (Exception e) {
			System.out.println("Error with multithread argument!");
		}
	}

}
