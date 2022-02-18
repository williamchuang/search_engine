import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * handles users visit record.
 * @author TimHuang
 *
 */
@SuppressWarnings("serial")
public class VisitServlet extends BaseServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String visit = request.getParameter("visitLink");
		String user = getUsername(request);

		if (user != null) {
			dbhandler.addVisited(user, visit, getDate());
		}
		response.sendRedirect(visit);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request, response);
	}
}