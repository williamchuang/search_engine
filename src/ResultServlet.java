import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * handles result servlet.
 * @author TingbinHuang
 *
 */

@SuppressWarnings("serial")
public class ResultServlet extends BaseServlet {
	
	private final ThreadSafeInvertedIndex index;
	
	public ResultServlet(ThreadSafeInvertedIndex index){
		this.index = index;
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String searchQuery = request.getParameter("query");
		
		if(searchQuery != null ){
			prepareResponse("Results", response);
			makeBody(request, response, searchQuery);
			finishResponse(response);
		
		} 
		else{
			response.sendRedirect(response.encodeRedirectURL("/"));
		
		}
		
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		
		String searchQuery = request.getParameter("query");
		searchQuery = searchQuery == null ? "" : searchQuery;
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(
		response.encodeRedirectURL("/result?searchQuery=" + URLEncoder.encode(searchQuery, "UTF-8")));
	}
	
	/**
	 * make a body of result page.
	 * @param request
	 * @param response
	 * @param word
	 * @throws IOException
	 */
	private void makeBody(HttpServletRequest request, HttpServletResponse response, String word) throws IOException {
		
		String userName = getUsername(request);
		PrintWriter out = response.getWriter();
		printResult(out, word, userName);
	}
	
	/**
	 * add each element from array-list links to list and sort list 
	 * @param list
	 * @param links
	 * @return
	 */
	private ArrayList<String> sortList(ArrayList<String> list, ArrayList<SearchResult> links){
		for(SearchResult link : links){
			list.add(link.getLocation());
		}
		Collections.sort(list);
		return list;
	}
	
	
	/**
	 * print a list of result to web page.
	 * @param out
	 * @param words
	 * @param userName
	 */
	private void printResult(PrintWriter out, String words, String userName){
		
		String[] query = WordParser.parseWords(words);
		
		if (userName != null) {
			dbhandler.addSearched(userName, String.join(" ", query), getDate());
		}
		
		out.printf("<h2><p>Query are entered: %s</p></h2>%n", String.join(" ", query));
		
		ArrayList<String> list = new ArrayList<>();
		ArrayList<SearchResult> links = (ArrayList<SearchResult>) index.partialForage(query);
		
		if(links.isEmpty() == false){
			for(String location : sortList(list, links)){

				out.printf("<h4><p><a href=\"%s\">%s</a></p></h4>%n", location,location);

			}
		} else {
			out.printf("There is no result for \"%s\".",String.join(" ", query));
		}

		out.println("<p>");
		out.println("<br/><br/><br/>");
		out.printf("<a href=\"/\">Click here back to Home page.</a></p>");
	}
	
	
}