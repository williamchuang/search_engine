import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handler for users to log into the search engine.
 * 
 * @author TingbinHuang
 *
 */
@SuppressWarnings("serial")
public class LoginServlet extends BaseServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		if (getUsername(request) == null) {
			prepareResponse("Log in", response);
			setBody(request, response);
			finishResponse(response);
		} else {
			response.sendRedirect(response.encodeRedirectURL("/"));
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String user = request.getParameter("user");
		String pass = request.getParameter("pass");

		// testing if user name and pass are matching to database.
		Status status = dbhandler.authenticateUser(user, pass);

		try {
			if (status == Status.OK) {
				response.addCookie(new Cookie("login", "true"));
				response.addCookie(new Cookie("name", user));
				response.sendRedirect(response.encodeRedirectURL("/"));
			} else {
				response.addCookie(new Cookie("login", "false"));
				response.addCookie(new Cookie("name", ""));
				response.sendRedirect(response.encodeRedirectURL("/login?error=" + status.ordinal()));
			}
		} catch (Exception ex) {
			log.debug("Unable to continue login.", ex);
		}
	}

	/**
	 * set up a body of options for users to log in.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void setBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");

		int code = 0;

		if (error != null) {
			try {
				code = Integer.parseInt(error);
			} catch (Exception ex) {
				code = -1;
			}

			String errorMessage = getStatusMessage(code);
			out.println("<p class=\"alert alert-danger\">" + errorMessage + "</p>");
		}

		if (request.getParameter("newuser") != null) {
			out.println("<p>Registration was successful!");
			out.println("Login with your new username and password below.</p>");
		}

		printForm(out, request.getServletPath());

	}

	/**
	 * set up and print the format of options for users.
	 * 
	 * @param out
	 * @param request_action
	 */
	private void printForm(PrintWriter out, String request_action) {
		out.println();

		out.println("<p><a href=\"/\">Home</a></p>");
		out.printf("<form action=\"%s\" method=\"post\" class=\"form-inline\">", request_action);
		out.println("\t<div class=\"form-group\">");
		out.println("\t\t<label for=\"user\">Username:</label>");
		out.println(
				"\t\t<input type=\"text\" name=\"user\" class=\"form-control\" id=\"user\" placeholder=\"Username\">");
		out.println("\t</div>\n");

		out.println("\t<div class=\"form-group\">");
		out.println("\t\t<label for=\"pass\">Password:</label>");
		out.println(
				"\t\t<input type=\"password\" name=\"pass\" class=\"form-control\" id=\"pass\" placeholder=\"Password\">");
		out.println("\t</div>\n");

		out.println("\t<button type=\"submit\" class=\"btn btn-primary\">Login</button>\n");
		out.println("</form>");
		out.println("<br/>\n");

		out.println("<p>(<a href=\"/register\">new user? register here.</a>)</p>");

	}

}