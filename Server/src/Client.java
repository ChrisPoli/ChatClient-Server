import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client {

	private Socket s;
	public static JTextArea incoming;
	private JTextField outgoing;
	private JFrame frame;
	private JPanel mainPanel;
	private JLabel label;
	private JButton send;
	private PrintWriter writer;
	private BufferedReader reader;
	private boolean done = true;
	private static String ip = "127.0.0.1";
	private static int port = 4998;

	private JFrame popupWindow;
	private JTextField ipField, portField;
	private JButton doneButton, defaultButton;
	private JLabel ipLabel, portLabel;
	
	

	public void go() {
		//constructs the chat client GUI
		frame = new JFrame("Messenger");
		Font basic = new Font("serif", 10, 16);
		incoming = new JTextArea(20, 30);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		incoming.setFont(basic);
		outgoing = new JTextField(20);

		send = new JButton("Send");
		send.addActionListener(new SendText());

		mainPanel = new JPanel();
		mainPanel.add(incoming, BorderLayout.NORTH);
		mainPanel.add(outgoing);
		mainPanel.add(send);
		frame.getRootPane().setDefaultButton(send);
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		frame.setBounds(400, 150, 400, 560);
		frame.setVisible(true);
		//calls to start establishing networking
		initializeNetworking(ip, port);
		// create thread that reads string in from server
		Thread readerThread = new Thread(new ReadingFromServer());
		readerThread.start();
	}

	public static void main(String[] args) {
		Client client = new Client();
		client.IpAndPortPopUp();
	}

	void initializeNetworking(String ip, int port) {
		Thread t = new Thread(new Text());
		
		try {
			incoming.append("Looking for server...");
			s = new Socket(ip, port);
			InputStreamReader streamReader = new InputStreamReader(s.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(s.getOutputStream());
			incoming.append("\nNetworking established...");
			t.start();
		} catch (Exception e) {
			done = false;
			incoming.append("\nCannot find server...");
			e.printStackTrace();
			
		} 
	}

	public void IpAndPortPopUp() {
		JPanel mainPanel;
		popupWindow = new JFrame("Enter ip and port of server to connect");
		popupWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		ipField = new JTextField(10);
		ipField.setText("127.0.0.1");
		portField = new JTextField(10);
		
		doneButton = new JButton("Done");
		doneButton.addActionListener(new IpAndPort());

		mainPanel = new JPanel();
		mainPanel.add(ipField);
		mainPanel.add(portField);
		mainPanel.add(doneButton, BorderLayout.SOUTH);
		popupWindow.getContentPane().add(mainPanel, BorderLayout.CENTER);
		popupWindow.setBounds(380, 300, 430, 100);
		popupWindow.setVisible(true);
	}

	public class IpAndPort implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			ip = ipField.getText();
			port = Integer.parseInt(portField.getText());
			popupWindow.dispose();
			go();
		
		}
	}

	// When send button is pressed, outgoing text is sent to server
	public class SendText implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			try {
				writer.println(outgoing.getText());
				writer.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			outgoing.setText("");
			outgoing.requestFocus();
		}
	}

	// Thread that waits for String sent from server
	public class ReadingFromServer implements Runnable {
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					incoming.append(message + "\n");
				}
			} catch (Exception ex) {
			}
		}
	}

	public class Text implements Runnable {
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			incoming.setText("");
		}
	}
	
}
