import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Home page to the main search engine site
 * 
 * @author TingbinHuang
 *
 */

@SuppressWarnings("serial")
public class HomePageServlet extends BaseServlet {

	public HomePageServlet() {
		super();
		
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		 if (request.getParameter("logout") != null) {
		
			 clearCookies(request, response);
		 }
		prepareResponse("Home", response);
		setBody(request, response);
		finishResponse(response);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String query = request.getParameter("query");

		query = query == null ? "" : query;

		response.setStatus(HttpServletResponse.SC_OK);

		if (query.trim().isEmpty() == false && query != null) {
			response.sendRedirect(response.encodeRedirectURL("/result?query=" + URLEncoder.encode(query, "UTF-8")));
		} else {
			response.sendRedirect(response.encodeRedirectURL("/"));
		}

	}

	/**
	 * set up body of options for uses to log in or log out.
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void setBody(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		
		String userName = getUsername(request);
		String changePass = userName == null ? "" : "<p><a href=\"/passchange\">Want/Need to change Password?</a></p>";
		
		String logButton = userName == null ? "Login" : "Logout";
		String loginLink = userName == null ? "/login" : "/?logout=true";
		 
		// adding log-in feature 
		out.printf("<form method = \"post\" action=\"%s\" >", request.getServletPath()) ;
		out.printf("<p><a href=\"%s\">%s</a></p>%n%n", loginLink,logButton);
		out.printf("%s", changePass);
		out.printf("</form>%n");
		
		if (request.getParameter("passchangesuccess") != null) {
			
			out.println("<div class=\"text-center\">");
			out.println("<p class=\"alert alert-success\">Password change was successful!");
			out.println("</p>");
			out.println("</div>");
		}
		
		if (request.getParameter("logout") != null) {
			 
			clearCookies(request, response);
		}
		
		printForm(request, response);
		
		String delSearched = request.getParameter("delSearched");
		String delVisit = request.getParameter("delVisit");

		if (delSearched != null) {
		
			dbhandler.removeSearched(userName);
		} else if (userName != null) {
		
			printSearchHist(out, userName);
			
		}
		if (delVisit != null) {
		
			dbhandler.removeVisited(userName);
		} else if (userName != null) {
			
			printVisitHist(out, userName);
			
		}
		
	}

	/**
	 * set up format for users to enter searching queries to search from search engine.
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {		
		
		String userName = getUsername(request);		
		PrintWriter out = response.getWriter();
		String greeting = userName == null ? "How are you today?" : "What's in your mind ?";
		String image = "https://i.pinimg.com/originals/77/4e/8b/774e8b473970b1c8fc0a6b43f1bd555a.jpg";
		out.println();
		out.println("&nbsp &nbsp; &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp ");
	 	
		out.printf("<img src=\"%s\" height=\"200\"/>", image); 
		out.println("<br/><br/>");
		out.printf("<form action=\"%s\" method=\"POST\" class=\"form-inline\">",request.getServletPath());
		out.println("&nbsp; &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp ");
		out.printf("\t<input type=\"text\" name=\"query\" class=\"form-control\"  placeholder=\"%s\">",greeting);
		
		out.printf("\t<input type=\"submit\" value=\"Search\">%n");
		out.printf("</form>%n");

	}
	
	/**
	 * print a list search history for users.
	 * @param out
	 * @param user
	 */
	private static void printSearchHist(PrintWriter out, String user) {
		Map<String, String> hist = dbhandler.getSearched(user);

		if (hist != null) {
			out.printf("<div class=\"row\"><div class=\"col-md-offset-0 col-md-8\">");
			out.printf("<h2><small>Search History:</small></h2>");
			for (String string : hist.keySet()) {
				out.printf(
						"<li><a href=\"/result?query=%s\">%s</a><a class=\"text-muted\">&nbsp;&nbsp;&nbsp;&nbsp;Searched: %s</a></li>",
						string, string, hist.get(string));
			}
			out.println("<a href=\"/?delSearched=\">delete search history</a>");
			out.printf("</div></div>");
		}
	}
	
	/**
	 * print searching queries that users enter. 
	 * @param out
	 * @param user
	 */
	private static void printVisitHist(PrintWriter out, String user) {
		Map<String, String> hist = dbhandler.getVisit(user);

		if (hist != null) {
			for (String string : hist.keySet()) {
				out.printf(
						"<a href=\"/leaving?visitLink=%s\">%s</a><a >%s</a>",
						string, string, hist.get(string));
			}
			out.println("<a href=\"/?delVisit=\">delete visit history</a>");
			
		}
	}

}