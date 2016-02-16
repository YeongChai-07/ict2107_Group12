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

public class Group12ChatApp extends JFrame {
	// Using
	public static final int MulticastGroupPort = 6789;
	public static final String commonMulticastGroupIP = "235.1.1.1";
	private String userStatus = "Offline";

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 551, 417);
		setVisible(true);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		lblTempID = new JLabel("ID");
		lblTempID.setText(ManagementFactory.getRuntimeMXBean().getName());
		lblTempID.setBounds(362, 22, 109, 14);
		contentPane.add(lblTempID);

		lblStatus = new JLabel("Status");
		lblStatus.setBounds(467, 22, 46, 14);
		contentPane.add(lblStatus);

		taChatBox = new JTextArea();
		taChatBox.setBounds(228, 149, 285, 185);
		contentPane.add(taChatBox);

		// *********************************************************************
		// Default listening channel
		try {
			commonMulticastSocket = new MulticastSocket(MulticastGroupPort);
			commonMulticastGroup = InetAddress.getByName(commonMulticastGroupIP);
			commonMulticastSocket.joinGroup(commonMulticastGroup);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
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
								btnRegisterUser.setText("Register");
								JOptionPane.showMessageDialog(new JFrame(), data, "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
						// Friend Request
						// command = FriendRequest ; user = receiver; data =
						// sender user name ; extra = null
						else if (command.equals("FriendRequest")) {
							// Only if message is for you
							if (user.equals(txtUserName.getText().trim())) {
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
							if (user.equals(txtUserName.getText().trim())) {
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
									// TODO Auto-generated catch block
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
							if (Arrays.asList(friends).contains(txtUserName.getText().trim())) {
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
							//Check IP duplicated?
							// Check everyone groupMap IP but not requester
							if (groupMap.containsValue(extra) && !txtUserName.getText().equals(data)) {
								String message = "UnicastInviteReply:" + data
										+ ":Assigned IP had already been taken.Please try again.:IP";	
								byte[] buf = message.getBytes();
								DatagramPacket dgpUnicastInviteReply = new DatagramPacket(buf, buf.length,
										commonMulticastGroup, MulticastGroupPort);
								commonMulticastSocket.send(dgpUnicastInviteReply);
							}else{
								// If its receiver 
								if(txtUserName.getText().equals(user)){
									String message = "";
									String dialogMessage = "\"" + data
											+ "\" wants to private message you, will you accept the request?";
									int requestResult = JOptionPane.showConfirmDialog(null, dialogMessage, "Friend Request",
											JOptionPane.YES_NO_OPTION);
									// Accept unicast
									if (requestResult == JOptionPane.YES_OPTION){
										message = "UnicastInviteReply:" + data
												+ ":\""+user+"\" had accepted the private messaging.:Accept";
										
										//Add IP to group map (sender name, IP)
										groupMap.put(data, extra);
										// Join group				
										lblGroup.setText("User:");
										lblChatGroupName.setText(data);
										
										groupMulticastSocket = new MulticastSocket(MulticastGroupPort);
										groupMulticastGroup = InetAddress.getByName(extra);
										groupMulticastSocket.joinGroup(groupMulticastGroup);
										taChatBox.setText("");
										
										//Send a joined message
										String joinmessage = txtUserName.getText() + " joined";
										byte[] buf = joinmessage.getBytes();
										DatagramPacket dgpChatJoin = new DatagramPacket(buf, buf.length, groupMulticastGroup, MulticastGroupPort);
										groupMulticastSocket.send(dgpChatJoin);										
										//Create a new thread to keep listening for packets from the group
										new Thread(new Runnable() {
											@Override
											public void run() {
												byte buf1[] = new byte[1000];
												DatagramPacket dgpMessageReceived= new DatagramPacket(buf1, buf1.length);
												while(true){
													try{
														groupMulticastSocket.receive(dgpMessageReceived);
														byte[] receivedData = dgpMessageReceived.getData();
														int length = dgpMessageReceived.getLength();
														//Assured we received string
														String msg = new String(receivedData,0, length);
														taChatBox.append(msg + "\n");
													}catch(IOException ex){
														ex.printStackTrace();
													}
												}
											}
										}).start();
										
										//Toggle button
										btnChatJoin.setEnabled(false);
										btnChatLeave.setEnabled(true);
										btnMessageSend.setEnabled(true);
									}
									//Rejected unicast
									else{
										message = "UnicastInviteReply:" + data
												+ ":\""+user+"\" had rejected the private messaging.:Rejected";								
									}
									byte[] buf = message.getBytes();
									DatagramPacket dgpUnicastInviteReply = new DatagramPacket(buf, buf.length,
											commonMulticastGroup, MulticastGroupPort);
									commonMulticastSocket.send(dgpUnicastInviteReply);
								}
							}
							
						}
						// Unicast Invite Reply
						// command = Unicast Invite Reply; user = requester user name
						// comma; data = invalid message ; extra = IP/Accept/Reject
						else if (command.equals("UnicastInviteReply")) {
							// Only if is requester
							if(txtUserName.getText().equals(user)){
								if(extra.equals("IP")){
									JOptionPane.showMessageDialog(new JFrame(), data, "Alert", JOptionPane.INFORMATION_MESSAGE);									
									//Clear chat group name
									lblGroup.setText("");
									lblChatGroupName.setText("");
								}else if(extra.equals("Reject")){
									JOptionPane.showMessageDialog(new JFrame(), data, "Alert", JOptionPane.INFORMATION_MESSAGE);
									//Clear chat group name
									lblGroup.setText("");
									lblChatGroupName.setText("");
								}else if(extra.equals("Accept")){
									JOptionPane.showMessageDialog(new JFrame(), data, "Alert", JOptionPane.INFORMATION_MESSAGE);									
									// Join group													
									groupMulticastSocket = new MulticastSocket(MulticastGroupPort);
									groupMulticastGroup = InetAddress.getByName(groupMap.get(lblChatGroupName.getText()));
									groupMulticastSocket.joinGroup(groupMulticastGroup);
									taChatBox.setText("");
									
									//Send a joined message
									String joinmessage = txtUserName.getText() + " joined";
									byte[] buf = joinmessage.getBytes();
									DatagramPacket dgpChatJoin = new DatagramPacket(buf, buf.length, groupMulticastGroup, MulticastGroupPort);
									groupMulticastSocket.send(dgpChatJoin);										
									//Create a new thread to keep listening for packets from the group
									new Thread(new Runnable() {
										@Override
										public void run() {
											byte buf1[] = new byte[1000];
											DatagramPacket dgpMessageReceived= new DatagramPacket(buf1, buf1.length);
											while(true){
												try{
													groupMulticastSocket.receive(dgpMessageReceived);
													byte[] receivedData = dgpMessageReceived.getData();
													int length = dgpMessageReceived.getLength();
													//Assured we received string
													String msg = new String(receivedData,0, length);
													taChatBox.append(msg + "\n");
												}catch(IOException ex){
													ex.printStackTrace();
												}
											}
										}
									}).start();
									
									//Toggle button
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
		lblUserName.setBounds(26, 22, 74, 14);
		contentPane.add(lblUserName);

		txtUserName = new JTextField();
		txtUserName.setText("");
		txtUserName.setBounds(111, 19, 141, 20);
		contentPane.add(txtUserName);
		txtUserName.setColumns(10);

		btnRegisterUser = new JButton("Register");
		btnRegisterUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Toggle status and button display
					if (btnRegisterUser.getText().equals("Register")) {
						// Notify all user have online
						String message = "UserStatus:All:" + txtUserName.getText().trim() + ":Online";
						byte[] buf = message.getBytes();
						DatagramPacket dgpUserStatus = new DatagramPacket(buf, buf.length, commonMulticastGroup,
								MulticastGroupPort);
						commonMulticastSocket.send(dgpUserStatus);

						userStatus = "Online";
						btnRegisterUser.setText("Offline");
					} else {
						// Notify all user have offline
						String message = "UserStatus:All:" + txtUserName.getText().trim() + ":Offline";
						byte[] buf = message.getBytes();
						DatagramPacket dgpUserStatus = new DatagramPacket(buf, buf.length, commonMulticastGroup,
								MulticastGroupPort);
						commonMulticastSocket.send(dgpUserStatus);

						userStatus = "Offline";
						btnRegisterUser.setText("Register");
					}

					// Broadcast Username check
					String message = "UserCheck:All:" + txtUserName.getText() + ":" + lblTempID.getText();
					byte[] buf = message.getBytes();
					DatagramPacket dgpUserNameCheck = new DatagramPacket(buf, buf.length, commonMulticastGroup,
							MulticastGroupPort);
					commonMulticastSocket.send(dgpUserNameCheck);

					Thread.sleep(100);

					lblStatus.setText(userStatus); // Own reference

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnRegisterUser.setBounds(263, 18, 89, 23);
		contentPane.add(btnRegisterUser);
		// *********************************************************************

		// ********************FRIEND LIST***********************************
		JLabel lblFriendName = new JLabel("Friend Name :");
		lblFriendName.setBounds(26, 53, 89, 14);
		contentPane.add(lblFriendName);

		txtFriendName = new JTextField();
		txtFriendName.setBounds(111, 50, 141, 20);
		contentPane.add(txtFriendName);
		txtFriendName.setColumns(10);

		JButton btnFriendAdd = new JButton("Add");
		btnFriendAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Friend Request
				try {
					String message = "FriendRequest:" + txtFriendName.getText().trim() + ":" + txtUserName.getText()
							+ ": ";
					byte[] buf = message.getBytes();
					DatagramPacket dgpFriendRequest = new DatagramPacket(buf, buf.length, commonMulticastGroup,
							MulticastGroupPort);
					commonMulticastSocket.send(dgpFriendRequest);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnFriendAdd.setBounds(263, 49, 89, 23);
		contentPane.add(btnFriendAdd);

		JButton btnFriendDelete = new JButton("Delete");
		btnFriendDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Remove from friend History
				if (friendHistoryList.contains(friendList.getSelectedValue())) {
					friendHistoryList.remove(friendList.getSelectedValue());
				}

				// remove from JList
				friendVector.remove(friendList.getSelectedValue());
				friendList.setListData(friendVector);
			}
		});
		btnFriendDelete.setBounds(362, 49, 89, 23);
		contentPane.add(btnFriendDelete);

		JLabel lblFriendList = new JLabel("Friend List");
		lblFriendList.setBounds(26, 125, 63, 14);
		contentPane.add(lblFriendList);

		// Create a vector that can store String objects
		friendVector = new Vector<String>();
		// Create a JList that is capable of storing String type items
		friendList = new JList<String>(friendVector);
		friendList.setBounds(26, 148, 90, 185);
		friendList.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        JList list = (JList)evt.getSource();
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
						String message = "UnicastInvite:" + friendVector.get(index) + ":"
								+ txtUserName.getText().trim() + ":" + requestIP;
						byte[] buf = message.getBytes();
						DatagramPacket dgpUnicastInvite = new DatagramPacket(buf, buf.length, commonMulticastGroup,
								MulticastGroupPort);
						commonMulticastSocket.send(dgpUnicastInvite);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    }
		});
		contentPane.add(friendList);

		// *********************************************************************

		// ********************GROUP LIST***********************************
		JLabel lblGroupName = new JLabel("Group Name :");
		lblGroupName.setBounds(26, 82, 89, 14);
		contentPane.add(lblGroupName);

		txtGroupName = new JTextField();
		txtGroupName.setBounds(111, 79, 141, 20);
		contentPane.add(txtGroupName);
		txtGroupName.setColumns(10);

		JButton btnGroupAdd = new JButton("Add");
		btnGroupAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
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

						// Thread.sleep(1000);
						// JOptionPane.showMessageDialog(new JFrame(), "Add
						// friends
						// yo", "Add group participants",
						// JOptionPane.INFORMATION_MESSAGE);

						// friendHistoryList.add("Test1");
						// friendHistoryList.add("Test2");
						// friendHistoryList.add("Test3");
						// friendHistoryList.add("Test4");
						// friendHistoryList.add("Test5");
						//
						// JPanel al = new JPanel();
						// for (String mc : friendHistoryList){
						// JCheckBox box = new JCheckBox(mc);
						// al.add(box);
						// }
						// int requestResult =
						// JOptionPane.showConfirmDialog(null,
						// al, "Add Friend ",JOptionPane.YES_NO_OPTION);
						// if (requestResult == JOptionPane.YES_OPTION){
						//
						// }

						// ***** NEED CHANGE UI*********

						JPanel myPanel = new JPanel();
						myPanel.add(new JLabel("Add group participants"));
						myPanel.add(friendField);
						int result = JOptionPane.showConfirmDialog(null, myPanel, "Add group participants",
								JOptionPane.OK_CANCEL_OPTION);
						if (result == JOptionPane.OK_OPTION) {
							// Assume data will be "friend1,friend2,friend3"
							String inviteMessage = "GroupInvite:" + friendField.getText() + ":"
									+ txtGroupName.getText().trim() + ":" + requestIP;
							byte[] buf2 = inviteMessage.getBytes();
							DatagramPacket dgpGroupInvite = new DatagramPacket(buf2, buf2.length, commonMulticastGroup,
									MulticastGroupPort);
							commonMulticastSocket.send(dgpGroupInvite);
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnGroupAdd.setBounds(263, 78, 89, 23);
		contentPane.add(btnGroupAdd);

		JButton btnGroupEdit = new JButton("Edit");
		btnGroupEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});
		btnGroupEdit.setBounds(362, 78, 89, 23);
		contentPane.add(btnGroupEdit);

		JLabel lblGroupList = new JLabel("Group List");
		lblGroupList.setBounds(127, 125, 63, 14);
		contentPane.add(lblGroupList);

		groupVector = new Vector<String>();
		groupList = new JList<String>(groupVector);
		groupList.setBounds(127, 149, 90, 184);
		groupList.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        JList list = (JList)evt.getSource();
		        if (evt.getClickCount() == 2) {
		            // Double-click detected
		            int index = list.locationToIndex(evt.getPoint());
		            lblGroup.setText("Group:");
		            lblChatGroupName.setText(groupVector.get(index));
		            btnChatJoin.setEnabled(true);
		        }
		    }
		});
		contentPane.add(groupList);

		// *********************************************************************

		// ***********************GROUP JOIN/LEAVE**************************************
		lblGroup = new JLabel("");
		lblGroup.setBounds(225, 124, 52, 14);
		contentPane.add(lblGroup);
		
		lblChatGroupName = new JLabel("");
		lblChatGroupName.setBounds(263, 124, 89, 14);
		contentPane.add(lblChatGroupName);

		btnChatJoin = new JButton("Join");
		btnChatJoin.setEnabled(false);
		btnChatJoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				try {
					String groupName = lblChatGroupName.getText();
					groupMulticastSocket = new MulticastSocket(MulticastGroupPort);
					groupMulticastGroup = InetAddress.getByName(groupMap.get(groupName));
					groupMulticastSocket.joinGroup(groupMulticastGroup);
					taChatBox.setText("");
					
					//Send a joined message
					String message = txtUserName.getText() + " joined";
					byte[] buf = message.getBytes();
					DatagramPacket dgpChatJoin = new DatagramPacket(buf, buf.length, groupMulticastGroup, MulticastGroupPort);
					groupMulticastSocket.send(dgpChatJoin);
					
					//Create a new thread to keep listening for packets from the group
					new Thread(new Runnable() {
						@Override
						public void run() {
							byte buf1[] = new byte[1000];
							DatagramPacket dgpMessageReceived= new DatagramPacket(buf1, buf1.length);
							while(true){
								try{
									groupMulticastSocket.receive(dgpMessageReceived);
									byte[] receivedData = dgpMessageReceived.getData();
									int length = dgpMessageReceived.getLength();
									//Assured we received string
									String msg = new String(receivedData,0, length);
									taChatBox.append(msg + "\n");
								}catch(IOException ex){
									ex.printStackTrace();
								}
							}
						}
					}).start();
					
					//Toggle button
					btnChatJoin.setEnabled(false);
					btnChatLeave.setEnabled(true);
					btnMessageSend.setEnabled(true);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnChatJoin.setBounds(356, 121, 70, 23);
		contentPane.add(btnChatJoin);

		btnChatLeave = new JButton("Leave");
		btnChatLeave.setEnabled(false);
		btnChatLeave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					String message = txtUserName.getText() + " : is leaving";
					byte[] buf = message.getBytes();
					DatagramPacket dgpChatLeave= new DatagramPacket(buf, buf.length, groupMulticastGroup, MulticastGroupPort);
					groupMulticastSocket.send(dgpChatLeave);
					groupMulticastSocket.leaveGroup(groupMulticastGroup);
					
					//Toggle button
					btnChatJoin.setEnabled(true);
					btnMessageSend.setEnabled(false);
					btnChatLeave.setEnabled(false);
					
				}catch(IOException ex){
					ex.printStackTrace();
				}
			}
		});
		btnChatLeave.setBounds(436, 122, 70, 23);
		contentPane.add(btnChatLeave);
		// *********************************************************************

		// ************************SENDING*********************************
		JLabel lblMessage = new JLabel("Message");
		lblMessage.setBounds(23, 348, 52, 14);
		contentPane.add(lblMessage);

		txtMessage = new JTextField();
		txtMessage.setBounds(85, 345, 327, 20);
		contentPane.add(txtMessage);
		txtMessage.setColumns(10);

		btnMessageSend = new JButton("Send");
		btnMessageSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String message = txtUserName.getText() + ": " + txtMessage.getText().trim();
					byte[] buf = message.getBytes();
					DatagramPacket dgpMessageSend = new DatagramPacket(buf, buf.length, groupMulticastGroup, MulticastGroupPort);
					groupMulticastSocket.send(dgpMessageSend);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		btnMessageSend.setEnabled(false);
		btnMessageSend.setBounds(422, 344, 89, 23);
		contentPane.add(btnMessageSend);
		
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
