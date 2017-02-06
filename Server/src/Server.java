import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

import javax.swing.*;

public class Server {
	private ArrayList clientOutputStreams;
	private JFrame popupWindow;
	private JTextField portField;
	private JButton doneButton;
	private JPanel mainPanel;
	private Socket sock;
	private JTextArea serverStatusText;
	private static int port;

	public static void main(String[] args) {
		new Server().IpAndPortPopUp();
	}

	public void IpAndPortPopUp() {

		popupWindow = new JFrame("Enter port number of server");
		popupWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		portField = new JTextField(10);

		doneButton = new JButton("Done");
		doneButton.addActionListener(new IpAndPort());

		JPanel mainPanel = new JPanel();
		mainPanel.add(portField);
		mainPanel.add(doneButton, BorderLayout.SOUTH);

		popupWindow.getRootPane().setDefaultButton(doneButton);
		popupWindow.getContentPane().add(mainPanel, BorderLayout.CENTER);
		popupWindow.setBounds(380, 300, 430, 100);
		popupWindow.setVisible(true);
	}

	public class IpAndPort implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			port = Integer.parseInt(portField.getText());
			popupWindow.dispose();
			Thread m = new Thread(new MakeSocket());
			m.start();
			Thread t = new Thread(new ServerStatus());
			t.start();

		}
	}

	public class ClientHandler implements Runnable {
		
		BufferedReader reader;

		public ClientHandler(Socket clientSocket) {
			sock = clientSocket;
			
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isr);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					tellEveryone(message);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	public class MakeSocket implements Runnable {

		public void run() {
			clientOutputStreams = new ArrayList();
			try {
				ServerSocket socket = new ServerSocket(port);
				while (true) {
					Socket clientSocket = socket.accept();
					PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
					clientOutputStreams.add(writer);
					Thread t = new Thread(new ClientHandler(clientSocket));
					t.start();
					serverStatusText.append("\nGot connection " + sock.getLocalAddress());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public class ServerStatus implements ActionListener, Runnable {
		JFrame serverStatusFrame;
		
		JPanel serverPanel;

		public void run() {
			createComponents();
		}

		public void createComponents() {
			serverStatusFrame = new JFrame("Server Status");
			serverStatusFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			serverStatusText = new JTextArea(10, 35);
			serverStatusText.setEditable(false);
			serverStatusText.append("Server online");
			serverStatusText.append("\nPort: " + port);
			JScrollPane scroller = new JScrollPane(serverStatusText);
			scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			serverPanel = new JPanel();
			serverPanel.add(scroller);

			JButton disconnectServer = new JButton("Disconnect");
			disconnectServer.addActionListener(this);
			serverStatusFrame.getRootPane().setDefaultButton(disconnectServer);

			serverPanel.add(disconnectServer);

			serverStatusFrame.getContentPane().add(serverPanel, BorderLayout.CENTER);
			serverStatusFrame.setBounds(380, 300, 520, 300);
			serverStatusFrame.setVisible(true);
		}

		public void actionPerformed(ActionEvent ev) {
			Client.incoming.append("Disconnected");
			System.exit(0);
		}

	}

	public void tellEveryone(String message) {
		Iterator it = clientOutputStreams.iterator();
		while (it.hasNext()) {
			try {
				serverStatusText.append("\nRecieved message from user" + ": " + message);
				PrintWriter writer = (PrintWriter) it.next();
				writer.println(message);
				writer.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
