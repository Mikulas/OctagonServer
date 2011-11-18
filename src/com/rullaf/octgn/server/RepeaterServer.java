package com.rullaf.octgn.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;

public class RepeaterServer {

	public static void main(String[] args) {
		try {
			// create a Jetty server with the 8091 port.
			Server server = new Server(4723);
			// register ChatWebSocketHandler in the Jetty server instance.
			RepeaterHandler repeater = new RepeaterHandler();
			repeater.setHandler(new DefaultHandler());
			server.setHandler(repeater);
			// start the Jetty server.
			server.start();
			// Jetty server is stopped when the Thread is interruped.
			server.join();
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
