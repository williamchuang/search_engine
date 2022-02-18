import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WebCrawler {

	private URL seed;
	private final InvertedIndex index;
	private int limit;
	private final Set<URL> set;

	public WebCrawler(InvertedIndex index) {
		this.index = index;
		this.set = new HashSet<>();
	}

	/*
	 *
	 * @param s base url
	 * 
	 * @param l number of urls that should be crawled
	 */
	public void crawl(URL s, int l) {
		this.seed = s;
		this.limit = l;
		set.add(s);
		parseURL(s);
		String[] result = LinkParser.fetchWords(s);
		index.addIndex(result, s.toString());
	}

	public void crawlhelper(URL s, int l) {

		set.add(s);
		parseURL(s);
		String[] result = LinkParser.fetchWords(s);
		index.addIndex(result, s.toString());
	}

	private void parseURL(URL baseURL) {
		ArrayList<URL> links;
		try {
			links = LinkParser.crawlURL(seed);
			if (links.isEmpty() == false) {
				for (URL link : links) {
					if (set.size() >= limit) {
						break;
					} else {
						if (set.contains(link) == false) {
							set.add(link);
							// System.out.println(link);
							crawlhelper(link, limit);
							// System.out.println(link.toString());
						}
					}
				}
			}
		} catch (UnknownHostException e) {
			System.out.println("IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			System.out.println("IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			links = LinkParser.crawlURL(baseURL);
			if (links.isEmpty() == false) {
				for (URL link : links) {
					if (set.size() >= limit) {
						break;
					} else {
						if (set.contains(link) == false) {
							set.add(link);
							// System.out.println(link);
							crawlhelper(link, limit);
							// System.out.println(link.toString());
						}
					}
				}
			}
		} catch (UnknownHostException e) {
			System.out.println("IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			System.out.println("IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}