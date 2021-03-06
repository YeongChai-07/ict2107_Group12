import java.awt.BorderLayout;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Window;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;// added
import java.awt.BorderLayout; // added
import javax.swing.border.LineBorder; // added

import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JList;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.SystemColor;
import java.awt.Font;



public class Group12ChatApp extends JFrame {
	// Using
	public static final int MulticastGroupPort = 6789;
	public static final String commonMulticastGroupIP = "235.1.1.1";
	private String userStatus = "Offline";
	
	
	
	private String prevUserName = "";
	Vector<String> emptyFriendVector;
	Vector<String> emptyGroupVector;
	
	JLabel iconStatus;

	InetAddress commonMulticastGroup = null;
	MulticastSocket commonMulticastSocket = null;

	private JTextField txtUserName;
	private JButton btnRegisterUser;

	JList<String> friendList;
	Vector<String> friendVector;
	List<String> friendHistoryList;
	private JTextField txtFriendName;

	JList<String> groupList;
	Vector<String> groupVector;
	private Map<String, String> groupMap;
	private JTextField txtGroupName;

	JTextField friendField = new JTextField(10);
	JTextArea taChatBox;
	JTextField txtMessage;

	MulticastSocket groupMulticastSocket = null;
	InetAddress groupMulticastGroup = null;

	JLabel lblGroup;
	JLabel lblChatGroupName;
	JLabel lblStatus;
	JLabel lblTempID;

	JButton btnChatJoin;
	JButton btnChatLeave;
	JButton btnMessageSend;
	
	
	
	// JScrollPane (A container with scrollbars that places a UI component
	// within it)
	// for our JTextArea and JList
	JScrollPane textArea_ScrollPane;
	JScrollPane groupList_ScrollPane;
	JScrollPane friendList_ScrollPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new Group12ChatApp();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Group12ChatApp() {
		friendHistoryList = new ArrayList<String>();
		groupMap = new HashMap<String, String>();
		
		
		
		setTitle("Group 12 Group Chat Application");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 678, 444);
		Border thickBorder = new LineBorder(Color.BLACK, 1); // added
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {				
				try {
					// Notify all user have offline
					String message = "UserStatus:All:" + txtUserName.getText().trim() + ":Offline";
					byte[] buf = message.getBytes();
					DatagramPacket dgpUserStatus = new DatagramPacket(buf, buf.length, commonMulticastGroup,
							MulticastGroupPort);
					commonMulticastSocket.send(dgpUserStatus);
					
					if (JOptionPane.showConfirmDialog(new JFrame(), "Are you sure to leave the application?", "", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
						System.exit(0);
				    }else{
				    	// Notify all user have online back
						String message2 = "UserStatus:All:" + txtUserName.getText().trim() + ":Online";
						byte[] buf2 = message2.getBytes();
						DatagramPacket dgpUserStatus2 = new DatagramPacket(buf2, buf2.length, commonMulticastGroup,
								MulticastGroupPort);
						commonMulticastSocket.send(dgpUserStatus2);
				    }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				
			}
		});
		setVisible(true);

		JPanel contentPane = new JPanel();
		contentPane.setBackground(new Color(153, 204, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		lblTempID = new JLabel("ID");
		lblTempID.setText(ManagementFactory.getRuntimeMXBean().getName());
		lblTempID.setVisible(false);
		lblTempID.setBounds(522, 31, 109, 14);
		contentPane.add(lblTempID);

		iconStatus = new JLabel();
		iconStatus.setIcon(new ImageIcon("img/offline.png"));
		iconStatus.setBounds(67, 42, 32, 32);
		contentPane.add(iconStatus);
		
		lblStatus = new JLabel("Offline");
		lblStatus.setBounds(28, 59, 46, 14);
		contentPane.add(lblStatus);

		taChatBox = new JTextArea();
		taChatBox.setBackground(new Color(255, 255, 255));
		taChatBox.setBounds(0, 0, 285, 185);

		// Let's create a JScrollPane container and place the JTextArea over it
		textArea_ScrollPane = new JScrollPane(taChatBox);
		textArea_ScrollPane.getViewport().setBackground(Color.pink);// added
		
		
		// Setting the textArea scrollpane's x,y coordinates, width and height
		textArea_ScrollPane.setBounds(30, 80, 370, 240);

		// Enabling both horizontal and vertical scrolling
		textArea_ScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		textArea_ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		/*
		 * Instead of adding JTextArea directly to contentPane, we add the
		 * JScrollPane //which already had the JTextArea attached to it to the
		 * contentPane //contentPane.add(taChatBox);
		 */
		contentPane.add(textArea_ScrollPane);

		// *********************************************************************
		// Default listening channel
		try {
			commonMulticastSocket = new MulticastSocket(MulticastGroupPort);
			commonMulticastGroup = InetAddress.getByName(commonMulticastGroupIP);
			commonMulticastSocket.joinGroup(commonMulticastGroup);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// Always listen to the default channel
		new Thread(new Runnable() {
			@Override
			public void run() {
				byte buf1[] = new byte[1000];
				DatagramPacket dgpReceived = new DatagramPacket(buf1, buf1.length);
				while (true) {
					try {
						commonMulticastSocket.receive(dgpReceived);
						byte[] receivedData = dgpReceived.getData();
						int length = dgpReceived.getLength();
						// Assured we received string
						String msg = new String(receivedData, 0, length);
						// System.out.println(msg);

						// Split String by ":"
						String[] parts = msg.split(":");
						String command = parts[0];
						String user = parts[1];
						String data = parts[2];
						String extra = parts[3];

						// Username validation
						// command = Usernamecheck; user = all; data =
						// requested User Name ; extra = TempID
						if (command.equals("UserCheck")) {
							// Check if username been used
							if (data.trim().equals(txtUserName.getText().trim()) && !extra.equals(lblTempID.getText())
									&& userStatus.equals("Online")) {
								String message = "UserCheckReply:All:\"" + data + "\" had already been taken:" + extra;
								byte[] buf = message.getBytes();
								DatagramPacket dgpUserNameValidation = new DatagramPacket(buf, buf.length,
										commonMulticastGroup, MulticastGroupPort);
								commonMulticastSocket.send(dgpUserNameValidation);
							}
						}
						// Username check reply
						// command = UserCheckReply ; user = all; data =
						// user existed message ; extra = TempID
						else if (command.equals("UserCheckReply")) {
							// Check for UserName Reply
							if (extra.trim().equals(lblTempID.getText())) {
								String message = "UserStatus:All:" + txtUserName.getText().trim() + ":Offline";
								byte[] buf = message.getBytes();
								DatagramPacket dgpUserStatus = new DatagramPacket(buf, buf.length, commonMulticastGroup,
										MulticastGroupPort);
								commonMulticastSocket.send(dgpUserStatus);

								userStatus = "Offline";
								
								txtUserName.setEnabled(true);
								btnRegisterUser.setText("Online");
								JOptionPane.showMessageDialog(new JFrame(), data, "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
						// Friend Request
						// command = FriendRequest ; user = receiver; data =
						// sender user name ; extra = null
						else if (command.equals("FriendRequest")) {
							// Only if message is for you
							if (user.equals(txtUserName.getText().trim()) && userStatus.equals("Online")) {
								// Yes/ No dialog to accept request or not
								String requester = data;
								String dialogMessage = "\"" + requester
										+ "\" wants to be your friend, will you accept the request?";
								int requestResult = JOptionPane.showConfirmDialog(null, dialogMessage, "Friend Request",
										JOptionPane.YES_NO_OPTION);
								String message = "";
								if (requestResult == JOptionPane.YES_OPTION) {
									// Include requester name in own list
									if (!friendHistoryList.contains(requester)) {
										friendHistoryList.add(requester);
									}
									// add into vector for display in JList
									friendVector.add(requester);
									friendList.setListData(friendVector);

									// Request accepted (send out a notify
									// accept)
									message = "FriendRequestReply:" + requester + ":" + txtUserName.getText().trim()
											+ ":Yes";
								} else {
									// Request rejected
									message = "FriendRequestReply:" + requester + ":" + txtUserName.getText().trim()
											+ ":No";
								}
								byte[] buf = message.getBytes();
								DatagramPacket dgpFriendRequestReply = new DatagramPacket(buf, buf.length,
										commonMulticastGroup, MulticastGroupPort);
								commonMulticastSocket.send(dgpFriendRequestReply);

							}
						}
						// Friend Request Reply
						// command = FriendRequestReply ; user = receiver; data
						// = sender user name ; extra = Yes/No
						else if (command.equals("FriendRequestReply")) {
							// Only if message is for you
							if (user.equals(txtUserName.getText().trim()) && userStatus.equals("Online")) {
								if (extra.equals("Yes")) {
									// Add friend into Friend list
									// Add into friend history for detect
									// online/offline later
									if (!friendHistoryList.contains(data)) {
										friendHistoryList.add(data);
									}
									// add into vector for display in JList
									friendVector.add(data);
									friendList.setListData(friendVector);
								}
							}
						}
						// Friend delete
						// command = Friend Delete ; user = deleted friend my sender list ; data
						// = sender user name ; extra = null
						else if(command.equals("FriendDelete")){
							if(txtUserName.getText().trim().equals(user)){
								friendVector.remove(data);
								friendList.setListData(friendVector);
								if(friendHistoryList.contains(data)){
									friendHistoryList.remove(data);
								}
							}
							// All except user
							else if(user.equals("All") && !txtUserName.getText().trim().equals(data)){
								//to tell all sender not using this username anymore
								if(friendHistoryList.contains(data)){
									friendHistoryList.remove(data);
								}
							}
						}
						
						// User Status update
						// command = User status ; user = all ; data
						// = sender user name ; extra = online/offline
						else if (command.equals("UserStatus")) {
							if (friendHistoryList.contains(data)) {
								if (extra.equals("Online")) {
									// add into vector for display in JList
									friendVector.add(data);
									friendList.setListData(friendVector);
								} else {
									// remove from vector for display in JList
									friendVector.remove(data);
									friendList.setListData(friendVector);
									
									//remove the IP they hold
									if (groupMap.containsKey(data)){
										groupMap.remove(data);
									}
								}
							}
						}
						// Group Check
						// command = Group Check ; user = sender User name ;
						// data
						// = request Group name ; extra = request IP
						else if (command.equals("GroupCheck")) {
							if (!txtUserName.getText().trim().equals(user)) {
								// Check is GroupName exist
								String message = "";
								if (groupMap.containsKey(data)) {
									message = "GroupCheckReply:" + user + ":\"" + data + "\" had already been taken:"
											+ data;
									byte[] buf = message.getBytes();
									DatagramPacket dgpGroupCheckReply = new DatagramPacket(buf, buf.length,
											commonMulticastGroup, MulticastGroupPort);
									commonMulticastSocket.send(dgpGroupCheckReply);
								}
								// Check is GroupIP exist
								else if (groupMap.containsValue(extra)) {
									message = "GroupCheckReply:" + user
											+ ":Assigned IP had already been taken.Please try again.:" + data;
									byte[] buf = message.getBytes();
									DatagramPacket dgpGroupCheckReply = new DatagramPacket(buf, buf.length,
											commonMulticastGroup, MulticastGroupPort);
									commonMulticastSocket.send(dgpGroupCheckReply);
								}
							}
						}
						// Group Check Reply
						// command = Group Check Reply; user = target User name
						// ; data = invalid message ; extra = IP
						else if (command.equals("GroupCheckReply")) {
							// Check if message to for you
							if (user.equals(txtUserName.getText().trim())) {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								Window w = SwingUtilities.getWindowAncestor(friendField);
								w.setVisible(false);

								// Remove from group List
								groupMap.remove(extra);
								groupVector.remove(extra);
								groupList.setListData(groupVector);
								JOptionPane.showMessageDialog(new JFrame(), data, "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
						// Group Invite
						// command = Group Invite; user = a long string with
						// comma; data = group name ; extra = group ip
						else if (command.equals("GroupInvite")) {
							// splited string to check if message to for you
							String[] friends = user.split(",");
							if (Arrays.asList(friends).contains(txtUserName.getText().trim()) && userStatus.equals("Online")) {
								String dialogMessage = "You have been invited to join \"" + data
										+ "\", will you accept the request?";
								int requestResult = JOptionPane.showConfirmDialog(null, dialogMessage,
										"Group Invitation", JOptionPane.YES_NO_OPTION);
								if (requestResult == JOptionPane.YES_OPTION) {
									// Add to group Map and List
									groupMap.put(data, extra);
									groupVector.add(data);
									groupList.setListData(groupVector);
								}
							}
						}
						// Unicast Invite
						// command = Unicast Invite; user = target user name
						// comma; data = sender user name ; extra = unicast ip
						else if (command.equals("UnicastInvite")) {
							// Check IP duplicated?
							// Check everyone groupMap IP but not requester
							if (groupMap.containsValue(extra) && !txtUserName.getText().equals(data)) {
								String message = "UnicastInviteReply:" + data
										+ ":Assigned IP had already been taken.Please try again.:IP";
								byte[] buf = message.getBytes();
								DatagramPacket dgpUnicastInviteReply = new DatagramPacket(buf, buf.length,
										commonMulticastGroup, MulticastGroupPort);
								commonMulticastSocket.send(dgpUnicastInviteReply);
							} else {
								// If its receiver
								if (txtUserName.getText().equals(user)) {
									String message = "";
									String dialogMessage = "\"" + data
											+ "\" wants to private message you, will you accept the request?";
									int requestResult = JOptionPane.showConfirmDialog(null, dialogMessage,
											"Friend Request", JOptionPane.YES_NO_OPTION);
									// Accept unicast
									if (requestResult == JOptionPane.YES_OPTION) {
										message = "UnicastInviteReply:" + data + ":\"" + user
												+ "\" had accepted the private messaging.:Accept";

										// Add IP to group map (sender name, IP)
										groupMap.put(data, extra);
										// Join group
										lblGroup.setText("User:");
										lblChatGroupName.setText(data);

										groupMulticastSocket = new MulticastSocket(MulticastGroupPort);
										groupMulticastGroup = InetAddress.getByName(extra);
										groupMulticastSocket.joinGroup(groupMulticastGroup);
										taChatBox.setText("");

										// Send a joined message
										String joinmessage = txtUserName.getText() + " joined";
										byte[] buf = joinmessage.getBytes();
										DatagramPacket dgpChatJoin = new DatagramPacket(buf, buf.length,
												groupMulticastGroup, MulticastGroupPort);
										groupMulticastSocket.send(dgpChatJoin);
										// Create a new thread to keep listening
										// for packets from the group
										new Thread(new Runnable() {
											@Override
											public void run() {
												byte buf1[] = new byte[1000];
												DatagramPacket dgpMessageReceived = new DatagramPacket(buf1,
														buf1.length);
												while (true) {
													try {
														groupMulticastSocket.receive(dgpMessageReceived);
														byte[] receivedData = dgpMessageReceived.getData();
														int length = dgpMessageReceived.getLength();
														// Assured we received
														// string
														String msg = new String(receivedData, 0, length);
														taChatBox.append(msg + "\n");
													} catch (IOException ex) {
														ex.printStackTrace();
													}
												}
											}
										}).start();

										// Toggle button
										btnChatJoin.setEnabled(false);
										btnChatLeave.setEnabled(true);
										btnMessageSend.setEnabled(true);
									}
									// Rejected unicast
									else {
										message = "UnicastInviteReply:" + data + ":\"" + user
												+ "\" had rejected the private messaging.:Reject";
									}
									byte[] buf = message.getBytes();
									DatagramPacket dgpUnicastInviteReply = new DatagramPacket(buf, buf.length,
											commonMulticastGroup, MulticastGroupPort);
									commonMulticastSocket.send(dgpUnicastInviteReply);
								}
							}

						}
						// Unicast Invite Reply
						// command = Unicast Invite Reply; user = requester user
						// name
						// comma; data = invalid message ; extra =
						// IP/Accept/Reject
						else if (command.equals("UnicastInviteReply")) {
							// Only if is requester
							if (txtUserName.getText().equals(user)) {
								if (extra.equals("IP")) {
									JOptionPane.showMessageDialog(new JFrame(), data, "Alert",
											JOptionPane.INFORMATION_MESSAGE);
									// Clear chat group name
									lblGroup.setText("");
									lblChatGroupName.setText("");
								} else if (extra.equals("Reject")) {
									JOptionPane.showMessageDialog(new JFrame(), data, "Alert",
											JOptionPane.INFORMATION_MESSAGE);
									// Clear chat group name
									lblGroup.setText("");
									lblChatGroupName.setText("");
								} else if (extra.equals("Accept")) {
									JOptionPane.showMessageDialog(new JFrame(), data, "Alert",
											JOptionPane.INFORMATION_MESSAGE);
									// Join group
									groupMulticastSocket = new MulticastSocket(MulticastGroupPort);
									groupMulticastGroup = InetAddress
											.getByName(groupMap.get(lblChatGroupName.getText()));
									groupMulticastSocket.joinGroup(groupMulticastGroup);
									taChatBox.setText("");

									// Send a joined message
									String joinmessage = txtUserName.getText() + " joined";
									byte[] buf = joinmessage.getBytes();
									DatagramPacket dgpChatJoin = new DatagramPacket(buf, buf.length,
											groupMulticastGroup, MulticastGroupPort);
									groupMulticastSocket.send(dgpChatJoin);
									// Create a new thread to keep listening for
									// packets from the group
									new Thread(new Runnable() {
										@Override
										public void run() {
											byte buf1[] = new byte[1000];
											DatagramPacket dgpMessageReceived = new DatagramPacket(buf1, buf1.length);
											while (true) {
												try {
													groupMulticastSocket.receive(dgpMessageReceived);
													byte[] receivedData = dgpMessageReceived.getData();
													int length = dgpMessageReceived.getLength();
													// Assured we received
													// string
													String msg = new String(receivedData, 0, length);
													taChatBox.append(msg + "\n");
												} catch (IOException ex) {
													ex.printStackTrace();
												}
											}
										}
									}).start();

									// Toggle button
									btnChatJoin.setEnabled(false);
									btnChatLeave.setEnabled(true);
									btnMessageSend.setEnabled(true);

								}
							}
						}

					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();
		// *********************************************************************

		// *********************REGISTER USER ROW*******************************
		JLabel lblUserName = new JLabel("User Name :");
		lblUserName.setBounds(415, 35, 74, 14);
		contentPane.add(lblUserName);

		txtUserName = new JTextField();
		txtUserName.setBackground(new Color(255, 255, 255));
		txtUserName.setText("");
		txtUserName.setBounds(415, 54, 100, 22);
		contentPane.add(txtUserName);
		txtUserName.setColumns(10);

		btnRegisterUser = new JButton("Online");
		btnRegisterUser.setBackground(new Color(255, 255, 255));
		btnRegisterUser.setBorder(thickBorder);
		btnRegisterUser.setBorder(thickBorder);
		btnRegisterUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Validation
				if(txtUserName.getText().trim().equals("")){
					JOptionPane.showMessageDialog(new JFrame(), "Username cannot be blank", "Error", JOptionPane.ERROR_MESSAGE);
				}else{
					try {
						// Toggle status and button display
						if (btnRegisterUser.getText().equals("Online")) {
							// Notify all user have online
							String message = "UserStatus:All:" + txtUserName.getText().trim() + ":Online";
							byte[] buf = message.getBytes();
							DatagramPacket dgpUserStatus = new DatagramPacket(buf, buf.length, commonMulticastGroup,
									MulticastGroupPort);
							commonMulticastSocket.send(dgpUserStatus);
	
							userStatus = "Online";
							txtUserName.setEnabled(false);
							btnRegisterUser.setText("Offline");
							if(txtUserName.getText().trim().equals(prevUserName)){
								friendList.setListData(friendVector);
								groupList.setListData(groupVector);
							}else{
								// New list
								friendVector = new Vector<String>();
								groupVector = new Vector<String>();
								friendList.setListData(friendVector);
								groupList.setListData(groupVector);
	
								// Remove my username from other active process
								String message2 = "FriendDelete:All:" + prevUserName
								+ ": ";
								byte[] buf2 = message2.getBytes();
								DatagramPacket dgpFriendDelete = new DatagramPacket(buf2, buf2.length, commonMulticastGroup,
										MulticastGroupPort);
								commonMulticastSocket.send(dgpFriendDelete);
							}
							
						} else {
							// Notify all user have offline
							String message = "UserStatus:All:" + txtUserName.getText().trim() + ":Offline";
							byte[] buf = message.getBytes();
							DatagramPacket dgpUserStatus = new DatagramPacket(buf, buf.length, commonMulticastGroup,
									MulticastGroupPort);
							commonMulticastSocket.send(dgpUserStatus);
	
							userStatus = "Offline";
							prevUserName = txtUserName.getText().trim();						
							txtUserName.setEnabled(true);
							btnRegisterUser.setText("Online");
							//Clear friend and group list
							friendList.setListData(new Vector<String>());
							groupList.setListData(new Vector<String>());
						}
	
						// Broadcast Username check
						String message = "UserCheck:All:" + txtUserName.getText() + ":" + lblTempID.getText();
						byte[] buf = message.getBytes();
						DatagramPacket dgpUserNameCheck = new DatagramPacket(buf, buf.length, commonMulticastGroup,
								MulticastGroupPort);
						commonMulticastSocket.send(dgpUserNameCheck);
	
						Thread.sleep(100);
	
						lblStatus.setText(userStatus); // Own reference
						String imgLink = "img/"+userStatus+".png";
						iconStatus.setIcon(new ImageIcon(imgLink));
	
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnRegisterUser.setBounds(522, 52, 50, 25);
		contentPane.add(btnRegisterUser);
		// *********************************************************************

		// ********************FRIEND LIST***********************************
		JLabel lblFriendName = new JLabel("Friend Name :");
		lblFriendName.setBounds(415, 78, 89, 14);
		contentPane.add(lblFriendName);

		txtFriendName = new JTextField();
		txtFriendName.setBackground(new Color(255, 255, 255));
		txtFriendName.setBounds(415, 94, 100, 22);
		contentPane.add(txtFriendName);
		txtFriendName.setColumns(10);

		JButton btnFriendAdd = new JButton("Add");
		btnFriendAdd.setBackground(new Color(255, 255, 255));
		btnFriendAdd.setBorder(thickBorder);
		btnFriendAdd.setBorder(thickBorder);
		btnFriendAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Validation
				if(txtFriendName.getText().trim().equals("")){
					JOptionPane.showMessageDialog(new JFrame(), "Friend name cannot be blank", "Error", JOptionPane.ERROR_MESSAGE);
				}else{
					// Friend Request
					try {
						if(txtFriendName.getText().trim().equals(txtUserName.getText().trim())){
							JOptionPane.showMessageDialog(new JFrame(), "Adding yourself as friend is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
						}else{
							String message = "FriendRequest:" + txtFriendName.getText().trim() + ":" + txtUserName.getText()
									+ ": ";
							byte[] buf = message.getBytes();
							DatagramPacket dgpFriendRequest = new DatagramPacket(buf, buf.length, commonMulticastGroup,
									MulticastGroupPort);
							commonMulticastSocket.send(dgpFriendRequest);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnFriendAdd.setBounds(522, 90, 50, 25);
		contentPane.add(btnFriendAdd);

		JButton btnFriendDelete = new JButton("Delete");
		btnFriendDelete.setBackground(new Color(255, 255, 255));
	
		btnFriendDelete.setBorder(thickBorder);
		
		btnFriendDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Remove from friend History
				try{
					if (friendHistoryList.contains(friendList.getSelectedValue())) {
						if (JOptionPane.showConfirmDialog(new JFrame(), "Are you sure to remove \""+friendList.getSelectedValue()+"\" from friend list?", "Friend Delete", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
							String removeFriend = friendList.getSelectedValue();
							friendHistoryList.remove(removeFriend);
							// remove from JList
							friendVector.remove(removeFriend);
							friendList.setListData(friendVector);
							
							// Remove my username from deleted friend
							String message = "FriendDelete:" + removeFriend + ":" + txtUserName.getText()
							+ ": ";
							byte[] buf = message.getBytes();
							DatagramPacket dgpFriendDelete = new DatagramPacket(buf, buf.length, commonMulticastGroup,
									MulticastGroupPort);
							commonMulticastSocket.send(dgpFriendDelete);
					    }					
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				
			}
		});
		btnFriendDelete.setBounds(579, 91, 50, 25);
		contentPane.add(btnFriendDelete);

		JLabel lblFriendList = new JLabel("Friend List");
		lblFriendList.setBounds(415, 162, 63, 14);
		contentPane.add(lblFriendList);

		// Create a vector that can store String objects
		friendVector = new Vector<String>();
		// Create a JList that is capable of storing String type items
		friendList = new JList<String>(friendVector);
		friendList.setBackground(new Color(255, 255, 255));
		friendList.setBounds(0, 0, 90, 185);
		friendList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				JList list = (JList) evt.getSource();
				if (evt.getClickCount() == 2) {
					try {
						// Double-click detected
						int index = list.locationToIndex(evt.getPoint());
						// change groupchat title
						lblGroup.setText("User:");
						lblChatGroupName.setText(friendVector.get(index));

						// Attempt Unicast
						String requestIP = randomGenerateIP();
						groupMap.put(friendVector.get(index).toString(), requestIP);
						String message = "UnicastInvite:" + friendVector.get(index) + ":" + txtUserName.getText().trim()
								+ ":" + requestIP;
						byte[] buf = message.getBytes();
						DatagramPacket dgpUnicastInvite = new DatagramPacket(buf, buf.length, commonMulticastGroup,
								MulticastGroupPort);
						commonMulticastSocket.send(dgpUnicastInvite);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		// Let's create a JScrollPane container and place the JList over it
		friendList_ScrollPane = new JScrollPane(friendList);

		// Setting the List scrollpane's x,y coordinates, width and height
		friendList_ScrollPane.setBounds(415, 182, 211, 67);

		// Enabling vertical scrolling
		friendList_ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		/*
		 * Instead of adding JList directly to contentPane, we add the
		 * JScrollPane which already had the JList attached to it to the
		 * contentPane
		 */
		// contentPane.add(friendList);
		contentPane.add(friendList_ScrollPane);

		// *********************************************************************

		// ********************GROUP LIST***********************************
		JLabel lblGroupName = new JLabel("Group Name :");
		lblGroupName.setBounds(415, 120, 89, 14);
		contentPane.add(lblGroupName);

		txtGroupName = new JTextField();
		txtGroupName.setBackground(new Color(255, 255, 255));
		txtGroupName.setBounds(415, 136, 100, 22);
		contentPane.add(txtGroupName);
		txtGroupName.setColumns(10);

		JButton btnGroupAdd = new JButton("Add");
		btnGroupAdd.setBackground(new Color(255, 255, 255));
		btnGroupAdd.setBorder(thickBorder);
		btnGroupAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Validation
				if(txtGroupName.getText().trim().equals("")){
					JOptionPane.showMessageDialog(new JFrame(), "Group name cannot be blank", "Error", JOptionPane.ERROR_MESSAGE);
				}else{
					try {
						// check for name conflict
						// check for IP conflict
	
						// Validation
						if (groupMap.containsKey(txtGroupName.getText().trim())) {
							String data = "\"" + txtGroupName.getText().trim() + "\"had already been taken";
							JOptionPane.showMessageDialog(new JFrame(), data, "Error", JOptionPane.ERROR_MESSAGE);
						} else {
							// Broadcast Group name check
							String requestIP = randomGenerateIP();
							String message = "GroupCheck:" + txtUserName.getText().trim() + ":"
									+ txtGroupName.getText().trim() + ":" + requestIP;
							byte[] buf = message.getBytes();
							DatagramPacket dgpGroupCheck = new DatagramPacket(buf, buf.length, commonMulticastGroup,
									MulticastGroupPort);
							commonMulticastSocket.send(dgpGroupCheck);
	
							// Add to own group list
							groupMap.put(txtGroupName.getText().trim(), requestIP);
							groupVector.add(txtGroupName.getText().trim());
							groupList.setListData(groupVector);
	
							JList list = new JList(friendVector);
							//Modify JList to accept multiple selection
							list.setSelectionModel(new DefaultListSelectionModel() {
							    @Override
							    public void setSelectionInterval(int index0, int index1) {
							        if(super.isSelectedIndex(index0)) {
							            super.removeSelectionInterval(index0, index1);
							        }
							        else {
							            super.addSelectionInterval(index0, index1);
							        }
							    }
							});
					        JScrollPane jscrollpane=new JScrollPane();
					        jscrollpane.setViewportView(list);

					        if(JOptionPane.showConfirmDialog(null, jscrollpane, "Add group participants", JOptionPane.YES_NO_OPTION)== JOptionPane.YES_OPTION){
					        	List<String> selectValue = list.getSelectedValuesList();
					        	String selectFriend = "";
					        	for (String value : selectValue) {
					        		selectFriend = selectFriend + value + ",";
					        	}
					        	// remove the last comma
					        	selectFriend = selectFriend.substring(0, selectFriend.length() - 1);
					        	System.out.println(selectFriend);
					        	
					        	// Assume data will be "friend1,friend2,friend3"
								String inviteMessage = "GroupInvite:" + selectFriend + ":"
										+ txtGroupName.getText().trim() + ":" + requestIP;
								byte[] buf2 = inviteMessage.getBytes();
								DatagramPacket dgpGroupInvite = new DatagramPacket(buf2, buf2.length, commonMulticastGroup,
										MulticastGroupPort);
								commonMulticastSocket.send(dgpGroupInvite);
					        }else{
					        	// remove to own group list
								groupMap.remove(txtGroupName.getText().trim());
								groupVector.remove(txtGroupName.getText().trim());
								groupList.setListData(groupVector);
					        }
						}
	
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		btnGroupAdd.setBounds(522, 132, 50, 25);
		contentPane.add(btnGroupAdd);

		JButton btnGroupEdit = new JButton("Edit");
		btnGroupEdit.setBackground(new Color(255, 255, 255));
		btnGroupEdit.setBorder(thickBorder);
		btnGroupEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});
		btnGroupEdit.setBounds(579, 131, 50, 25);
		contentPane.add(btnGroupEdit);

		JLabel lblGroupList = new JLabel("Group List");
		lblGroupList.setBounds(415, 260, 63, 14);
		contentPane.add(lblGroupList);

		groupVector = new Vector<String>();
		groupList = new JList<String>(groupVector);
		groupList.setBackground(new Color(255, 255, 255));
		groupList.setBounds(0, 0, 90, 184);
		groupList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				JList list = (JList) evt.getSource();
				if (evt.getClickCount() == 2) {
					// Double-click detected
					int index = list.locationToIndex(evt.getPoint());
					lblGroup.setText("Group:");
					lblChatGroupName.setText(groupVector.get(index));
					try {
						String groupName = lblChatGroupName.getText();
						groupMulticastSocket = new MulticastSocket(MulticastGroupPort);
						groupMulticastGroup = InetAddress.getByName(groupMap.get(groupName));
						groupMulticastSocket.joinGroup(groupMulticastGroup);
						taChatBox.setText("");

						// Send a joined message
						String message = txtUserName.getText() + " joined";
						byte[] buf = message.getBytes();
						DatagramPacket dgpChatJoin = new DatagramPacket(buf, buf.length, groupMulticastGroup,
								MulticastGroupPort);
						groupMulticastSocket.send(dgpChatJoin);

						// Create a new thread to keep listening for packets from
						// the group
						new Thread(new Runnable() {
							@Override
							public void run() {
								byte buf1[] = new byte[1000];
								DatagramPacket dgpMessageReceived = new DatagramPacket(buf1, buf1.length);
								while (true) {
									try {
										groupMulticastSocket.receive(dgpMessageReceived);
										byte[] receivedData = dgpMessageReceived.getData();
										int length = dgpMessageReceived.getLength();
										// Assured we received string
										String msg = new String(receivedData, 0, length);
										taChatBox.append(msg + "\n");
									} catch (IOException ex) {
										ex.printStackTrace();
									}
								}
							}
						}).start();

						// Toggle button
						btnChatJoin.setEnabled(false);
						btnChatLeave.setEnabled(true);
						btnMessageSend.setEnabled(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		// Let's create a JScrollPane container and place the JList over it
		groupList_ScrollPane = new JScrollPane(groupList);

		// Setting the List scrollpane's x,y coordinates, width and height
		groupList_ScrollPane.setBounds(415, 283, 211, 75);

		// Enabling vertical scrolling
		groupList_ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		/*
		 * Instead of adding JList directly to contentPane, we add the
		 * JScrollPane which already had the JList attached to it to the
		 * contentPane
		 */
		// contentPane.add(groupList);
		contentPane.add(groupList_ScrollPane);

		// *********************************************************************

		// ***********************GROUP
		// JOIN/LEAVE**************************************
		lblGroup = new JLabel("");
		lblGroup.setBounds(99, 58, 58, 14);
		contentPane.add(lblGroup);

		lblChatGroupName = new JLabel("");
		lblChatGroupName.setBounds(147, 58, 83, 14);
		contentPane.add(lblChatGroupName);

		btnChatJoin = new JButton("Join");
		btnChatJoin.setBackground(new Color(255, 255, 255));
		//btnChatJoin.setBorder(thickBorder);
		btnChatJoin.setEnabled(false);
		btnChatJoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String groupName = lblChatGroupName.getText();
					groupMulticastSocket = new MulticastSocket(MulticastGroupPort);
					groupMulticastGroup = InetAddress.getByName(groupMap.get(groupName));
					groupMulticastSocket.joinGroup(groupMulticastGroup);
					taChatBox.setText("");

					// Send a joined message
					String message = txtUserName.getText() + " joined";
					byte[] buf = message.getBytes();
					DatagramPacket dgpChatJoin = new DatagramPacket(buf, buf.length, groupMulticastGroup,
							MulticastGroupPort);
					groupMulticastSocket.send(dgpChatJoin);

					// Create a new thread to keep listening for packets from
					// the group
					new Thread(new Runnable() {
						@Override
						public void run() {
							byte buf1[] = new byte[1000];
							DatagramPacket dgpMessageReceived = new DatagramPacket(buf1, buf1.length);
							while (true) {
								try {
									groupMulticastSocket.receive(dgpMessageReceived);
									byte[] receivedData = dgpMessageReceived.getData();
									int length = dgpMessageReceived.getLength();
									// Assured we received string
									String msg = new String(receivedData, 0, length);
									taChatBox.append(msg + "\n");
								} catch (IOException ex) {
									ex.printStackTrace();
								}
							}
						}
					}).start();

					// Toggle button
					btnChatJoin.setEnabled(false);
					btnChatLeave.setEnabled(true);
					btnMessageSend.setEnabled(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnChatJoin.setBounds(245, 53, 75, 25);
		contentPane.add(btnChatJoin);

		btnChatLeave = new JButton("Leave");
		btnChatLeave.setBackground(new Color(255, 255, 255));
		//btnChatLeave.setBorder(thickBorder);
		btnChatLeave.setEnabled(false);
		btnChatLeave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String message = txtUserName.getText() + " left";
					byte[] buf = message.getBytes();
					DatagramPacket dgpChatLeave = new DatagramPacket(buf, buf.length, groupMulticastGroup,
							MulticastGroupPort);
					groupMulticastSocket.send(dgpChatLeave);
					groupMulticastSocket.leaveGroup(groupMulticastGroup);

					// Toggle button
					btnChatJoin.setEnabled(true);
					btnMessageSend.setEnabled(false);
					btnChatLeave.setEnabled(false);

				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		btnChatLeave.setBounds(325, 53, 75, 25);
		contentPane.add(btnChatLeave);
		// *********************************************************************

		// ************************SENDING*********************************
		JLabel lblMessage = new JLabel("Message :");
		lblMessage.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblMessage.setBounds(32, 327, 89, 17);
		contentPane.add(lblMessage);

		txtMessage = new JTextField();
		txtMessage.setFont(new Font("Tahoma", Font.PLAIN, 17));
		txtMessage.setBounds(122, 322, 198, 25);
		contentPane.add(txtMessage);
		txtMessage.setColumns(10);

		btnMessageSend = new JButton("Send");
		btnMessageSend.setBackground(new Color(255, 255, 255));
		//btnMessageSend.setBorder(thickBorder);
		btnMessageSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String message = txtUserName.getText() + ": " + txtMessage.getText().trim();
					byte[] buf = message.getBytes();
					DatagramPacket dgpMessageSend = new DatagramPacket(buf, buf.length, groupMulticastGroup,
							MulticastGroupPort);
					groupMulticastSocket.send(dgpMessageSend);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		btnMessageSend.setEnabled(false);
		btnMessageSend.setBounds(325, 322, 75, 25);
		contentPane.add(btnMessageSend);	
		
		JPanel panel = new JPanel();
		Border thickBorder2 = new LineBorder(Color.BLACK, 1);
		panel.setBorder(thickBorder2);
		panel.setBackground(SystemColor.textHighlightText);
		panel.setBounds(15, 31, 395, 327);
		contentPane.add(panel);
		

		// *********************************************************************

	}

	public String randomGenerateIP() {
		Random generator = new Random();
		int num3 = generator.nextInt(256);
		generator = new Random();
		int num4 = generator.nextInt(256);
		return ("235.1." + num3 + "." + num4);
	}
}
