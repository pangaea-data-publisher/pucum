package de.pangaea.ucum;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import de.pangaea.ucum.v1.PanUcumApp;

public final class Bootstrap {
  
  private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
  
  private Bootstrap() {
    // no instance
  }
  
  public static void main(String[] args) throws Exception {
    final String host = System.getProperty("host", "127.0.0.1");
    final int port = Integer.parseInt(System.getProperty("port", "8384"));
    final String contextPath = System.getProperty("contextPath", "/");
    
    logger.info("Starting PUCUM on http://{}:{}{}...", host, port, contextPath);
    final Server server = new Server(InetSocketAddress.createUnresolved(host, port));

    final ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.NO_SECURITY | ServletContextHandler.NO_SESSIONS);    
    ctx.setContextPath(contextPath);
    server.setHandler(ctx);
    
    final ServletHolder serHol = ctx.addServlet(ServletContainer.class, "/*");
    serHol.setInitOrder(1);
    serHol.setInitParameter("javax.ws.rs.Application", PanUcumApp.class.getName());
    
    try {
      server.start();
      server.join();
    } finally {
      server.destroy();
      logger.info("PUCUM server was shutdown.");
    }
  }
  
}
