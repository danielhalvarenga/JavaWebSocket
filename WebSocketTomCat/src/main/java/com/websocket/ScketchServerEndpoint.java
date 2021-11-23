package com.websocket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/scketchServerEndpoint")
public class ScketchServerEndpoint {

	static Set<Session> scketchUsers = Collections.synchronizedSet(new HashSet<>());

	@OnOpen
	public void handleOpen(Session userSession) {
		scketchUsers.add(userSession);
	}

	@OnClose
	public void handleClose(Session userSession) {
		scketchUsers.remove(userSession);
	}

	@OnMessage
	public void handleMessage(String incomingScketchMessage, Session userSession) {
		scketchUsers.stream().filter(x -> !x.equals(userSession)).forEach(x -> {
			try {
				x.getBasicRemote().sendText(incomingScketchMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@OnError
	public void handleError(Throwable t) {
		System.out.println(t);
	}
}
