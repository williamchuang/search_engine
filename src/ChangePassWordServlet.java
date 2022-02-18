import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ChangePassWordServlet extends BaseServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		if(getUsername(request) != null){
			prepareResponse("Password Changing", response);
			setBody(request, response);
			finishResponse(response);
		} else {
			response.sendRedirect(response.encodeRedirectURL("/"));
		}
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String oldPassWord = request.getParameter("oldpass");
		String newPassWord = request.getParameter("newpass");
		Status status = dbhandler.updatePass(getUsername(request), oldPassWord, newPassWord);
		
		if (status == Status.OK) {
			response.sendRedirect(response.encodeRedirectURL("/?passchangesuccess=true"));
		} else {
			String url = "/passchange?error=" + status.name();
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url);
		}
		
		
		
	}
	
	/**
	 * set up and print a body of options for users go to Home page or change passwords.
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void setBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");
		
		out.println("<p><a href=\"/\">Click here to Home</a></p>");
		
		if (error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<div class=\"col-sm-offset-1 col-md-4\">");
			out.println("<p class=\"alert alert-danger\">" + errorMessage + "</p>");
			out.println("</div>");
		}
		
		printForm(request, out);
		
	}
	
	/**
	 * set format of options for users to change passwords.
	 * @param request
	 * @param out
	 */
	private void printForm(HttpServletRequest request, PrintWriter out) {
		out.println();
		out.printf("<form action=\"%s\" method=\"POST\" class=\"form-inline\">",request.getServletPath());
		out.printf("\t<input type = \"password\" name = \"oldpass\" class=\"form-control\"  placeholder=\"old password\">%n%n");
		out.printf("\t<input type = \"password\" name = \"newpass\" class=\"form-control\"  placeholder=\"new password\">%n%n");
		out.printf("\t<input type=\"submit\" value=\"Change\">%n");
		out.printf("</form>%n");
	}
}