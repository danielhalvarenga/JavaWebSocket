package com.websocket.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;

public class SwingScketchApp extends JFrame {

	private static final long serialVersionUID = 1L;

	private Color color;
	private Color previousColor;

	private JPanel mainPanel;
	private JPanel scketchPanel;
	private JPanel scketchControlPanel;
	private JPanel colorPanel;
	private JButton eraseButton;

	private ScketchAppClient scketchAppClient;

	public static void main(String[] args) {
		try {
			new SwingScketchApp();
		} catch (DeploymentException | IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public SwingScketchApp() throws DeploymentException, IOException, URISyntaxException {
		initializeMainPanel();
		setTitle("WebSocket Swing Scketch App.");
		setSize(600, 400);
		setVisible(true);
		String serverPath = "ws://localhost:8080/WebSocketTomCat/scketchServerEndpoint";
		scketchAppClient = new ScketchAppClient(this);
		ContainerProvider.getWebSocketContainer().connectToServer(scketchAppClient, new URI(serverPath));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				scketchAppClient.close();
			}
		});
	}

	private void initializeMainPanel() {
		initializeScketchPanel();
		initializeScketchControlPanel();
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(scketchPanel, BorderLayout.CENTER);
		mainPanel.add(scketchControlPanel, BorderLayout.SOUTH);
		add(mainPanel);
	}

	private void initializeScketchPanel() {
		if (scketchPanel != null) {
			return;
		}
		scketchPanel = new JPanel();
		scketchPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				pointScketch(e.getPoint());
			}
		});
		scketchPanel.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				pointScketch(e.getPoint());
			}
		});
		scketchPanel.setBackground(Color.WHITE);
	}

	private void initializeScketchControlPanel() {
		initializeColorPanel();
		scketchControlPanel = new JPanel();
		eraseButton = new JButton("Erase");
		eraseButton.addActionListener(e -> erase());
		scketchControlPanel.setLayout(new BoxLayout(scketchControlPanel, BoxLayout.X_AXIS));
		scketchControlPanel.setBorder(new LineBorder(Color.DARK_GRAY));
		scketchControlPanel.add(eraseButton);
		scketchControlPanel.add(colorPanel);
	}

	private void initializeColorPanel() {
		if (colorPanel != null) {
			return;
		}
		colorPanel = new JPanel();
		colorPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				chooseColor();
			}
		});
		color = new Color(Color.BLACK.getRGB());
		colorPanel.setBackground(color);
	}

	public void chooseColor() {
		color = JColorChooser.showDialog(null, "Color", this.color);
		colorPanel.setBackground(color);
	}

	public void pointScketch(Point point) {
		int size = 10;
		if (color == Color.WHITE) {
			size = 80;
		}
		String sketchAppMessage = Json.createObjectBuilder().add("x", point.x).add("y", point.y).add("size", size)
				.add("color", color.getRGB()).build().toString();
		try {
			messageScketch(sketchAppMessage);
			scketchAppClient.getSession().getBasicRemote().sendText(sketchAppMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void erase() {
		if (color != Color.WHITE) {
			scketchPanel.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			previousColor = color;
			color = Color.WHITE;
			colorPanel.setVisible(false);
			eraseButton.setText("Sketch");
			return;
		}
		scketchPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		color = previousColor;
		colorPanel.setBackground(color);
		colorPanel.setVisible(true);
		eraseButton.setText("Erase");
	}

	public void messageScketch(String scketchAppMessage) {
		JsonObject jsonObject = Json.createReader(new StringReader(scketchAppMessage)).readObject();
		Point point = new Point(jsonObject.getInt("x"), jsonObject.getInt("y"));
		Color color = new Color(jsonObject.getInt("color"));
		int size = jsonObject.getInt("size");
		Graphics graphics = scketchPanel.getGraphics();
		graphics.setColor(color);
		graphics.fillOval(point.x - size, point.y - size, size, size);
	}

}
