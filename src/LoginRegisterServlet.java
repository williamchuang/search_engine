import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles users registration.
 * 
 * @author TingbinHuang
 *
 */
@SuppressWarnings("serial")
public class LoginRegisterServlet extends BaseServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		if (getUsername(request) == null) {
			prepareResponse("Register", response);
			setBody(request, response);
			finishResponse(response);
		} else {
			response.sendRedirect(response.encodeRedirectURL("/"));
		}
		
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		prepareResponse("Register New User", response);
		
		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pass");
		Status status = dbhandler.registerUser(newuser, newpass);

		if (status == Status.OK) {
			response.sendRedirect(response.encodeRedirectURL("/login?newuser=true"));
		} else {
			String url = "/register?error=" + status.name();
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url);
		}
	}
	
	/**
	 * set the body of options for users.
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void setBody (HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");
		
		if (error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p class=\"alert alert-danger\">" + errorMessage + "</p>");

		}
		
		 printForm(request, out);
	}
	
	/**
	 * set up the format of register options for users.
	 * @param request
	 * @param out
	 */
	private void printForm(HttpServletRequest request, PrintWriter out) {
		
		out.println();
		out.printf("<form action=\"%s\" method=\"POST\" class=\"form-inline\">",request.getServletPath());

		out.println("\t<div class=\"form-group\">");
		out.println("\t\t<label for=\"user\">Username:</label>");
		out.println("\t\t<input type=\"text\" name=\"user\" class=\"form-control\" id=\"user\" placeholder=\"username\">");
		out.println("\t</div>\n");

		out.println("\t<div class=\"form-group\">");
		out.println("\t\t<label for=\"pass\">Password:</label>");
		out.println("\t\t<input type=\"password\" name=\"pass\" class=\"form-control\" id=\"pass\" placeholder=\"password\">");
		out.println("\t</div>\n");

		out.println("\t<button type=\"submit\" class=\"btn btn-primary\">Register</button>\n");
		out.println("\t<p><a href=\"/login\">Already registered? Click here</a></p>");
		out.println("</form>");
		out.println("<br/>\n");
	}
	
	
}