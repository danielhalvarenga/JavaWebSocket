package com.websocket.client;

import java.io.IOException;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

@ClientEndpoint
public class ScketchAppClient {

	private Session session;
	private SwingScketchApp scketchApp;
	
	public ScketchAppClient(SwingScketchApp scketchApp) {
		this.scketchApp = scketchApp;
	}
	
	@OnOpen
	public void handleOpen(Session session) {
		this.session = session;
	}

	public void close() {
		try {
			session.close();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Disconnect Error... " + e);
		}
	}

	@OnMessage
	public void handleMessage(String scketchMessage, Session userSession) {
		scketchApp.messageScketch(scketchMessage);
	}

	@OnError
	public void handleError(Throwable t) {
		System.out.println(t.getMessage());
	}
	
	public Session getSession() {
		return session;
	}
}
