import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a thread-version of webcrawler class using multithreading
 * 
 * @author TingHuang
 *
 */

public class ConcurrentWebCrawler {

	private static final Logger logger = LogManager.getLogger();
	private final ThreadSafeInvertedIndex safeIndex;
	private final WorkQueue queue;
	private final Set<URL> set;

	/**
	 * Default constructor for ConcurrentWebCrawler class
	 * 
	 * @param safeIndex
	 *            safe version of invertedIndex class
	 * @param queue
	 *            workqueue
	 */
	public ConcurrentWebCrawler(ThreadSafeInvertedIndex safeIndex, WorkQueue queue) {
		this.safeIndex = safeIndex;
		this.queue = queue;
		this.set = new HashSet<>();

	}

	/**
	 * crawl url using work queue recursively from the base url as input
	 * 
	 * @param seed
	 *            base url
	 * @param limit
	 *            number of urls that should be crawled
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void crawl(URL seed, int limit) throws UnknownHostException, IOException, URISyntaxException {
		try {

			set.add(seed);
			queue.execute(new WebCrawler(seed, limit));

			queue.finish();

		} finally {
			logger.debug("parsing URL finished");
		}

	}

	/**
	 * 
	 * Runnable task that crawl each url and store info from url to
	 * InvertedIndex
	 *
	 */
	private class WebCrawler implements Runnable {

		private URL seed;
		private int limit;

		/**
		 * Default constructor
		 * 
		 * @param seed
		 *            base url
		 * @param limit
		 *            number of urls that should be crawled
		 */
		public WebCrawler(URL seed, int limit) {
			this.seed = seed;
			this.limit = limit;
		}

		@Override
		public void run() {
			try {

				this.parseURL(seed);
				String[] result = LinkParser.fetchWords(seed);
				InvertedIndex local = new InvertedIndex();
				local.addIndex(result, seed.toString());
				safeIndex.addAll(local);

			} catch (Exception e) {
				logger.warn("Unable to parse url due to {}", e.getMessage());
			}

		}

		/**
		 * parse each url from a list recursively
		 * 
		 * @param baseURL
		 * 
		 * @throws UnknownHostException
		 * @throws IOException
		 * @throws URISyntaxException
		 */
		private void parseURL(URL baseURL) throws UnknownHostException, IOException, URISyntaxException {
			synchronized (set) {
				ArrayList<URL> links = LinkParser.crawlURL(seed);

				if (links.isEmpty() == false) {
					for (URL link : links) {
						if (set.size() >= limit) {
							break;
						} else {
							if (set.contains(link) == false) {
								set.add(link);
								logger.debug("size of set: {}", set.size());
								queue.execute(new WebCrawler(link, limit));

							}
						}
					}

				}

			}
		}
	}

}