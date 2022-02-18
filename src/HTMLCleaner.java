import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cleans simple, validating HTML 4/5 into plain-text words using regular
 * expressions.
 * 
 *
 * @see <a href="https://validator.w3.org/">validator.w3.org</a>
 * @see <a href="https://www.w3.org/TR/html51/">HTML 5.1 Specification</a>
 * @see <a href="https://www.w3.org/TR/html401/">HTML 4.01 Specification</a>
 *
 * @see java.util.regex.Pattern
 * @see java.util.regex.Matcher
 * @see java.lang.String#replaceAll(String, String)
 * 
 * @author William_Chuang (responsible for stripEntities, strip, stripTags, and
 *         stripElement)
 * @version 2.01, 19 March 2018
 * 
 */
public class HTMLCleaner {

	/** Regular expression for removing special characters. */
	public static final String CLEAN_REGEX = "(?U)[^\\p{Alnum}\\p{Space}]+";

	/** Regular expression for splitting text into words by whitespace. */
	public static final String SPLIT_REGEX = "(?U)\\p{Space}+";

	public static enum HTTP {
		OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT
	};

	/** Version of HTTP used and supported. */
	public static final String version = "HTTP/1.1";

	public static final int DEFAULT_PORT = 80;

	/**
	 * Replaces all HTML entities with a single space. For example,
	 * "2010&ndash;2012" will become "2010 2012".
	 *
	 * @param html
	 *            text including HTML entities to remove
	 * @return text without any HTML entities
	 */
	public static String stripEntities(String html) {
		html = html.replaceAll("&[^\\s]+?;", " ");
		return html;
	}

	/**
	 * Replaces all HTML comments with a single space. For example, "A<!-- B -->C"
	 * will become "A C".
	 *
	 * @param html
	 *            text including HTML comments to remove
	 * @return text without any HTML comments
	 */
	public static String stripComments(String html) {
		html = html.replaceAll("<!--(?s).*?[\\n]*?.*?-->", " ");
		return html;

	}

	/**
	 * Replaces all HTML tags with a single space. For example, "A<b>B</b>C" will
	 * become "A B C".
	 *
	 * @param html
	 *            text including HTML tags to remove
	 * @return text without any HTML tags
	 */
	public static String stripTags(String html) {
		html = html.replaceAll("<[^>]*>", " ");
		return html;
	}

	/**
	 * Replaces everything between the element tags and the element tags themselves
	 * with a single space. For example, consider the html code: *
	 *
	 * <pre>
	 * &lt;style type="text/css"&gt;body { font-size: 10pt; }&lt;/style&gt;
	 * </pre>
	 *
	 * If removing the "style" element, all of the above code will be removed, and
	 * replaced with a single space.
	 *
	 * @param html
	 *            text including HTML elements to remove
	 * @param name
	 *            name of the HTML element (like "style" or "script")
	 * @return text without that HTML element
	 */
	public static String stripElement(String html, String name) {
		String clean = "";
		String temp = html;
		;
		try {
			clean = temp.replaceAll("(?is)<" + name + ".+?" + name + "\\s*>", " ");
		} catch (Exception e) {
		} finally {
		}
		return clean;
	}

	/**
	 * Fetches the webpage at the provided URL by opening a socket, sending an HTTP
	 * request, removing the headers, and returning the resulting HTML code.
	 *
	 * @param link
	 *            webpage to download
	 * @return html code
	 */
	public static String fetchHTML(String link) {
		try {
			return fetchHTMLHelper(link);
		} catch (IOException e) {
			System.err.println("Unable to fetch HTML for the link: " + link);
		}
		return null;
	}

	/**
	 * Fetches the webpage at the provided URL by opening a socket, sending an HTTP
	 * request, removing the headers, and returning the resulting HTML code.
	 * 
	 * @param link
	 *            Link to fetch
	 * @return HTML code
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static String fetchHTMLHelper(String link) throws UnknownHostException, MalformedURLException, IOException {
		URL target = new URL(link);

		String request = craftHTTPRequest(target, HTTP.GET);
		List<String> lines = fetchLines(target, request);

		int start = 0;
		int end = lines.size();

		// Determines start of HTML versus headers.
		while (!lines.get(start).trim().isEmpty() && start < end) {
			start++;
		}

		// Double-check this is an HTML file.
		Map<String, String> fields = parseHeaders(lines.subList(0, start + 1));
		String type = fields.get("Content-Type");

		if (type != null && type.toLowerCase().contains("html")) {
			return String.join(System.lineSeparator(), lines.subList(start + 1, end));
		}

		return null;
	}

	/**
	 * Removes all HTML (including any CSS and JavaScript).
	 *
	 * @param html
	 *            text including HTML to remove
	 * @return text without any HTML, CSS, or JavaScript
	 */
	public static String stripHTML(String html) {
		html = stripElement(html, "script");
		html = stripComments(html);

		html = stripElement(html, "head");
		html = stripElement(html, "style");

		html = stripTags(html);
		html = stripEntities(html);

		return html;
	}

	/**
	 * Parses the provided plain text (already cleaned of HTML tags) into individual
	 * words.
	 *
	 * @param text
	 *            plain text without html tags
	 * @return list of parsed words
	 */
	public static String[] parseWords(String text) {
		text = text.replaceAll(CLEAN_REGEX, "").toLowerCase().trim();
		return text.split(SPLIT_REGEX);
	}

	/**
	 * Removes all style and script tags (and any text in between those tags), all
	 * HTML tags, and all HTML entities.
	 *
	 * @param html
	 *            html code to parse
	 * @return plain text
	 */
	public static String cleanHTML(String html) {
		String text = html;
		text = stripElement("script", text);
		text = stripElement("style", text);
		text = stripTags(text);
		text = stripEntities(text);
		return text;
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
		return String.format("%s %s %s\n" + "Host: %s\n" + "Connection: close\n" + "\r\n", type.name(), resource,
				version, host);
	}

	/**
	 * Will connect to the web server and fetch the URL using the HTTP request
	 * provided. It would be more efficient to operate on each line as returned
	 * instead of storing the entire result as a list.
	 *
	 * @param url
	 *            - url to fetch
	 * @param request
	 *            - full HTTP request
	 *
	 * @return the lines read from the web server
	 *
	 * @throws IOException
	 * @throws UnknownHostException
	 */
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

	/**
	 * Helper method that parses HTTP headers into a map where the key is the field
	 * name and the value is the field value. The status code will be stored under
	 * the key "Status".
	 *
	 * @param headers
	 *            - HTTP/1.1 header lines
	 * @return field names mapped to values if the headers are properly formatted
	 */
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
}
