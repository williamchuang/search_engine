import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class BaseServer {

	private final ThreadSafeInvertedIndex index;
	private final int port;

	public BaseServer(ThreadSafeInvertedIndex index, int port) {
		this.index = index;
		this.port = port;
	}
	
	public void serverStarts(){
		Server server = new Server(port);
		ServletHandler handler = new ServletHandler();
		
		handler.addServletWithMapping(new ServletHolder(new HomePageServlet()), "/");
		handler.addServletWithMapping(new ServletHolder(new ResultServlet(index)), "/result");
		handler.addServletWithMapping(new ServletHolder(new LoginServlet()), "/login");
		handler.addServletWithMapping(new ServletHolder(new LoginRegisterServlet()), "/register");
		handler.addServletWithMapping(new ServletHolder(new VisitServlet()), "/leaving"); 
		handler.addServletWithMapping(new ServletHolder(new ChangePassWordServlet()), "/passchange");
		
		server.setHandler(handler);
		try{
			server.start();
			server.join();
			
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
		
	}
	


}