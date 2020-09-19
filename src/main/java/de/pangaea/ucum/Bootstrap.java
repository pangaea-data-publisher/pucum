package de.pangaea.ucum;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import de.pangaea.ucum.v1.PanUcumApp;

public final class Bootstrap {
  
  private static final Logger logger = LogManager.getLogger(Bootstrap.class);
  
  private Bootstrap() {
    // no instance
  }
  
  public static void main(String[] args) throws Exception {
    final String host = System.getProperty("host", "127.0.0.1");
    final int port = Integer.parseInt(System.getProperty("port", "3838"));
    
    logger.info("Starting PUCUM on {}:{}...", host, port);
    final Server server = new Server();
    
    final ServerConnector connector = new ServerConnector(server);
    connector.setHost(host);
    connector.setPort(port);
    server.addConnector(connector);
    
    final ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);    
    ctx.setContextPath("/pucum");
    server.setHandler(ctx);
    
    final ServletHolder serHol = ctx.addServlet(ServletContainer.class, "/*");
    serHol.setInitOrder(1);
    serHol.setInitParameter("javax.ws.rs.Application", PanUcumApp.class.getName());
    
    try {
      server.start();
      server.join();
    } finally {
      server.destroy();
      logger.info("Server shutdown.");
    }
  }
  
}