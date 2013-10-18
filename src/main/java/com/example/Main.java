package com.example;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://ec2-54-224-127-191.compute-1.amazonaws.com:8080/myapp/";
    static HttpServer server = null;

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig().packages("com.example");
    	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
            System.out.println("Stopping server..");
            server.stop();
        }
    }, "shutdownHook"));
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	try {
        server = startServer();
	System.out.println("Press CTRL^C to exit..");
	Thread.currentThread().join();
        //System.out.println(String.format("Jersey app started with WADL available at "
         //       + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        //System.in.read();
        //server.stop();
	} catch (Exception ex) {
		System.out.println("There was an error starting Grzzly Server.");
	}
    }
}

