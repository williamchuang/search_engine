
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses links from HTML. Assumes the HTML is valid, and all attributes are
 * properly quoted and URL encoded.
 * 
 */
public class LinkParser {

	/**
	 * The regular expression used to parse the HTML for links.
	 */
	public static final String REGEX = "(?si)<a\\s*[^>]*?\\s*href\\s*=\\s*\"(.*?)\"\\s*.*?\\s*>";

	/** Port used by socket. For web servers, should be port 80. */
	public static final int DEFAULT_PORT = 80;

	/** Version of HTTP used and supported. */
	public static final String version = "HTTP/1.1";

	/**
	 * The group in the regular expression that captures the raw link.
	 */
	public static final int GROUP = 1;

	/** Valid HTTP method types. */
	public static enum HTTP {
		OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT
	};

	/**
	 * Parses the provided text for HTML links.
	 *
	 * @param text
	 *            - valid HTML code, with quoted attributes and URL encoded links
	 * @return list of URLs found in HTML code
	 */
	public static ArrayList<String> listLinks(String text) {
		// list to store links
		ArrayList<String> links = new ArrayList<String>();

		// compile string into regular expression
		Pattern p = Pattern.compile(REGEX);

		// match provided text against regular expression
		Matcher m = p.matcher(text);

		// loop through every match found in text
		while (m.find()) {
			// add the appropriate group from regular expression to list
			links.add(m.group(GROUP));
		}

		return links;
	}

	/**
	 * fetch and clean entire html page and then parse words from the page.
	 * 
	 * @param url
	 * @return
	 */
	public static String[] fetchWords(URL url) {
		String text = HTMLCleaner.stripHTML(fetchHTML(url));

		return WordParser.parseWords(text);

	}

	public static String fetchHTML(URL url) {

		String request = craftHTTPRequest(url, HTTP.GET);
		List<String> lines = null;
		try {
			lines = fetchLines(url, request);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		int start = 0;
		int end = lines.size();

		while (!lines.get(start).trim().isEmpty() && start < end) {
			start++;
		}

		Map<String, String> fields = parseHeaders(lines.subList(0, start + 1));
		String type = fields.get("Content-Type");

		if (type != null && type.toLowerCase().contains("html")) {

			return String.join(System.lineSeparator(), lines.subList(start + 1, end));
		}

		return null;
	}

	/**
	 * Crafts a minimal HTTP/1.1 request for the provided method.
	 *
	 * @param url
	 *            - url to fetch
	 * @param type
	 *            - HTTP method to use
	 *
	 * @return HTTP/1.1 request
	 *
	 * @see {@link HTTP}
	 */
	public static String craftHTTPRequest(URL url, HTTP type) {
		String host = url.getHost();
		String resource = url.getFile().isEmpty() ? "/" : url.getFile();

		// The specification is specific about where to use a new line
		// versus a carriage return!
		return String.format("%s %s %s\r\n" + "Host: %s\r\n" + "Connection: close\r\n" + "\r\n", type.name(), resource,
				version, host);
	}

	public static List<String> fetchLines(URL url, String request) throws UnknownHostException, IOException {
		ArrayList<String> lines = new ArrayList<>();
		int port = url.getPort() < 0 ? DEFAULT_PORT : url.getPort();

		try (Socket socket = new Socket(url.getHost(), port);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				PrintWriter writer = new PrintWriter(socket.getOutputStream());) {

			writer.println(request);
			writer.flush();

			String line = null;

			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		return lines;
	}

	public static Map<String, String> parseHeaders(List<String> headers) {
		Map<String, String> fields = new HashMap<>();

		if (headers.size() > 0 && headers.get(0).startsWith(version)) {
			fields.put("Status", headers.get(0).substring(version.length()).trim());

			for (String line : headers.subList(1, headers.size())) {
				String[] pair = line.split(":", 2);

				if (pair.length == 2) {
					fields.put(pair[0].trim(), pair[1].trim());
				}
			}
		}

		return fields;
	}

	public static ArrayList<URL> crawlURL(URL seed) throws UnknownHostException, IOException, URISyntaxException {

		int port = seed.getPort() < 0 ? DEFAULT_PORT : seed.getPort();
		String request = craftHTTPRequest(seed, HTTP.GET);
		try (Socket socket = new Socket(seed.getHost(), port);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				PrintWriter writer = new PrintWriter(socket.getOutputStream());) {

			writer.println(request);
			writer.flush();
			String line = null;
			ArrayList<String> links = new ArrayList<>();

			while ((line = reader.readLine()) != null) {
				if (hasURL(line)) {
					links.add(line);
				}
			}
			return listLinks(seed, links);

		}
	}

	public static boolean hasURL(String url) {
		return url.contains("href");
	}

	public static ArrayList<URL> listLinks(URL base, ArrayList<String> htmls) throws MalformedURLException {

		int GROUP = 1;
		ArrayList<URL> links = new ArrayList<URL>();
		String REGEX = "(?i)<a(?:[^<>]*?)href=\"([^\"]+?)\"";

		if (htmls.isEmpty() == false) {
			for (String html : htmls) {

				URL link = null;

				Pattern pattern = Pattern.compile(REGEX);
				Matcher match = pattern.matcher(html.replaceAll("\\s", "").trim());

				while (match.find()) {
					String site = match.group(GROUP);

					if (!site.startsWith("http")) {

						link = new URL(base, site);

					} else {
						link = new URL(site);
					}
					if (link.toString().startsWith("http")) {
						links.add(clean(link));
					}

				}
			}
		}

		return links;
	}

	public static URL clean(URL url) {
		try {
			return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), null).toURL();
		} catch (URISyntaxException | MalformedURLException e) {
			return url;
		}
	}

}