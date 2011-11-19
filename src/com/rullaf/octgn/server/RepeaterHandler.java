package com.rullaf.octgn.server;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

public class RepeaterHandler extends WebSocketHandler {
	private final Set<ChatWebSocket> webSockets = new CopyOnWriteArraySet<ChatWebSocket>();

	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new ChatWebSocket();
	}

	private class ChatWebSocket implements WebSocket.OnTextMessage {
		private Connection connection;

		public void onOpen(Connection connection) {
			// Tell all connected clients that new client has been opened so they can broadcast current status
			for (ChatWebSocket webSocket : webSockets) {
				try {
					webSocket.connection.sendMessage("{\"method\": \"announce_join\", \"count\": " + (webSockets.size() + 1) + "}");
				} catch (IOException e) {
					// Error was detected, close the ChatWebSocket client side
					this.connection.disconnect();
				}
			}
			
			// Client (Browser) WebSockets has opened a connection.
			// 1) Store the opened connection
			this.connection = connection;
			// 2) Add ChatWebSocket in the global list of ChatWebSocket instances instance.
			webSockets.add(this);
		}

		public void onMessage(String data) {
			if (data.equals("{\"method\": \"keep-alive\"}")) {
				return;
			}
			
			// Loop for each instance of ChatWebSocket to send message server to each client WebSockets.
			try {
				for (ChatWebSocket webSocket : webSockets) {
					// send a message to the current client WebSocket.
					webSocket.connection.sendMessage(data);
				}
			} catch (IOException x) {
				// Error was detected, close the ChatWebSocket client side
				this.connection.disconnect();
			}

		}

		public void onClose(int closeCode, String message) {
			// Remove ChatWebSocket in the global list of ChatWebSocket instance.
			webSockets.remove(this);
			
			// Tell all remaining clients that this client left
			for (ChatWebSocket webSocket : webSockets) {
				try {
					webSocket.connection.sendMessage("{\"method\": \"announce_leave\", \"count\": " + webSockets.size() + "}");
				} catch (IOException e) {
					// Error was detected, close the ChatWebSocket client side
					this.connection.disconnect();
				}
			}
		}
	}
}
