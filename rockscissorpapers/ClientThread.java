package RockPaperScissors;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;
import RockPaperScissors.Play;
import RockPaperScissors.rpsClient;
import RockPaperScissors.MessageBox;

public class ClientThread extends Thread {

	private rpsClient ct_client;
	private Socket ct_sock;
	private DataInputStream ct_in;
	private DataOutputStream ct_out;
	private StringBuffer ct_buffer;
	private Thread thisThread;
	private Play playroom;
	public static String st_ID, withID;
	private static String choose1 = null;
	private static String choose2 = null;

	private static final String SEPARATOR = "|";
	private static final String DELIMETER = "`";

	private static final int REQ_LOGON = 1001;
	private static final int REQ_SENDWORDS = 1021;
	private static final int REQ_LOGOUT = 1031;
	private static final int REQ_GETRESULT = 1042;
	private static final int REQ_QUITROOM = 1041;
	private static final int REQ_SENDMESSAGE = 1045;
	private static final int REQ_PLAYGAME = 1055;

	private static final int YES_LOGON = 2001;
	private static final int NO_LOGON = 2002;
	private static final int MDY_PLAYMEMBER = 2013;
	private static final int MDY_PLAYERS = 2005;
	private static final int YES_QUITROOM = 2042;
	private static final int YES_LOGOUT = 2031;
	private static final int NO_LOGOUT = 2032;
	private static final int YES_GETRESULT = 2041;
	private static final int MDY_MEMVER = 2004;
	private static final int NO_PLAYGAME = 2014;
	private static final int YES_PLAYGAME = 2018;

	private static final int MSG_ALREADYUSER = 3001;
	private static final int MSG_SERVERFULL = 3002;
	private static final int MSG_CANNOTOPEN = 3011;
	private static final int ERR_NOUSER = 2015;
	private static final int ERR_REJECTION = 2019;
	private static final int ERR_ALREADYPLAYER = 2033;

	private static MessageBox msgBox, logonbox;

	public ClientThread(rpsClient client) {
		try {
			ct_sock = new Socket(InetAddress.getLocalHost(), 3000);
			ct_in = new DataInputStream(ct_sock.getInputStream());
			ct_out = new DataOutputStream(ct_sock.getOutputStream());
			ct_buffer = new StringBuffer(4096);
			thisThread = this;
			ct_client = client;

		} catch (IOException e) {
			MessageBoxLess msgout = new MessageBoxLess(client, "????????????", "????????? ????????? ??? ????????????.");
			msgout.show();
		}
	}

	public void run() {
		try {
			Thread currThread = Thread.currentThread();
			while (currThread == thisThread) {
				String recvData = ct_in.readUTF();
				StringTokenizer st = new StringTokenizer(recvData, SEPARATOR);
				int command = Integer.parseInt(st.nextToken());
				switch (command) {

				case YES_LOGON: {
					this.ct_client.cc_btLogon.setEnabled(false);
					logonbox.dispose();

					ct_client.cc_tfStatus.setText("????????? ??????!");
					String ids = st.nextToken();
					StringTokenizer users = new StringTokenizer(ids, DELIMETER);
					ct_client.cc_tfLogon.setEnabled(false);

					while (users.hasMoreTokens()) {
						ct_client.cc_lstMember.add(users.nextToken());
					}
					break;
				}
				// ????????? ??????
				case NO_LOGON: {
					st_ID = "";
					int errcode = Integer.parseInt(st.nextToken());
					if (errcode == MSG_ALREADYUSER) {
						logonbox.dispose();
						msgBox = new MessageBox(ct_client, "?????????", "?????? ???????????? ??????????????????.");
						msgBox.show();
					} else if (errcode == MSG_SERVERFULL) {
						logonbox.dispose();
						msgBox = new MessageBox(ct_client, "?????????", "???????????? ???????????????.");
						msgBox.show();
					}
					break;
				}
				// ????????? ????????? ????????? ?????? ??????
				case MDY_MEMVER: {
					ct_client.cc_lstMember.clear();
					String ids = st.nextToken();
					StringTokenizer users = new StringTokenizer(ids, DELIMETER);
					while (users.hasMoreTokens()) {
						ct_client.cc_lstMember.add(users.nextToken());
					}
					break;

				}
				// ?????? ????????? ?????? ??????
				case MDY_PLAYERS: {
					String players = st.nextToken();
					if (players.equals("????????? ??????")) {
						ct_client.cc_lstPlayer.clear();
					} else {
						ct_client.cc_lstPlayer.clear();
						StringTokenizer users = new StringTokenizer(players, DELIMETER);
						while (users.hasMoreTokens()) {
							ct_client.cc_lstPlayer.add(users.nextToken());
						}
					}
					break;

				}

				// LOGOUT ????????? ??????
				case YES_LOGOUT: {
					this.ct_client.cc_btLogon.setEnabled(true);
					ct_client.cc_tfStatus.setText("????????? ??????????????? ID??? ??????????????????");

					ct_client.cc_tfLogon.setEnabled(true);
					ct_client.cc_tfLogon.setText("");
					ct_client.cc_lstMember.removeAll();

					break;
				}
				case REQ_PLAYGAME: {
					String id = st.nextToken();
					String idTo = st.nextToken();
					String message = id + "(???)??? ?????? ????????????";
					int value = JOptionPane.showConfirmDialog(null, message, "?????? ??????", JOptionPane.YES_NO_OPTION);

					if (value == 1) {
						try { // ???????????? ????????? ???????????? ???
							ct_buffer.setLength(0);
							ct_buffer.append(NO_PLAYGAME);
							ct_buffer.append(SEPARATOR);
							ct_buffer.append(id);
							ct_buffer.append(SEPARATOR);
							ct_buffer.append(ERR_REJECTION);
							send(ct_buffer.toString());

						} catch (IOException e) {
							System.out.println(e);
						}
					} else { // ???????????? ????????? ?????? ?????????
						try {
							ct_buffer.setLength(0);
							ct_buffer.append(YES_PLAYGAME);
							ct_buffer.append(SEPARATOR);
							ct_buffer.append(id);
							ct_buffer.append(SEPARATOR);
							ct_buffer.append(idTo);
							send(ct_buffer.toString());

						} catch (IOException e) {
							System.out.println(e);
						}
						ct_client.dispose();
						playroom = new Play(this, "?????????", id, idTo);
						playroom.pack();
						playroom.setSize(350,150);
						playroom.show();
					}
					break;
				}
				// ?????? ?????????
				case NO_PLAYGAME: {
					int code = Integer.parseInt(st.nextToken());
					String id = st.nextToken();

					if (code == ERR_REJECTION) {
						String message = id + "???(???) ????????? ?????????????????????.";
						JOptionPane.showConfirmDialog(null, message, "?????? ??????", JOptionPane.ERROR_MESSAGE);
						break;
					} else if (code == ERR_NOUSER) {
						String message = id + "???(???) ???????????? ????????????.";
						JOptionPane.showConfirmDialog(null, message, "?????? ??????", JOptionPane.ERROR_MESSAGE);
						break;
					}
					// ???????????? ?????? ????????? ???????????????
					else if (code == ERR_ALREADYPLAYER) {
						String message = id + "???(???) ?????? ?????? ????????????.";
						JOptionPane.showConfirmDialog(null, message, "?????? ??????", JOptionPane.ERROR_MESSAGE);
						break;

					}
				}
				// ?????? ??????
				case YES_PLAYGAME: {
					ct_client.dispose();
					String idTo = st.nextToken();
					playroom = new Play(this, "?????????", st_ID, idTo);
					playroom.pack();
					playroom.setSize(350,150);
					playroom.show();
					break;
				}
				case YES_GETRESULT: {
					String result = st.nextToken(); 
					System.out.println(st_ID);
					try {
						playroom.lb_status.setText(result);
					} catch (NoSuchElementException e) {
					}

					break;

				}
				case YES_QUITROOM: {
					playroom.dispose();
					ct_client.show();
					break;
				}

				}

				Thread.sleep(200);

			}

		} catch (InterruptedException e) {
			System.out.println(e);
			release();

		} catch (IOException e) {
			System.out.println(e);
			release();
		}
	}

	public void release() {
	};

	public void requestLogon(String id) {
		try {
			st_ID = id;
			logonbox = new MessageBox(ct_client, "?????????", "????????? ????????? ????????????.");
			logonbox.show();
			System.out.println(st_ID);
			ct_buffer.setLength(0);
			ct_buffer.append(REQ_LOGON);
			ct_buffer.append(SEPARATOR);
			ct_buffer.append(id);
			send(ct_buffer.toString());

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void requestLogout(String id) {
		try {
			st_ID = "";
			logonbox = new MessageBox(ct_client, "????????????", "???????????? ???????????? ???????????????.");
			logonbox.show();
			ct_buffer.setLength(0);
			ct_buffer.append(REQ_LOGOUT);
			ct_buffer.append(SEPARATOR);
			ct_buffer.append(id);
			send(ct_buffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	// ?????? ???
	public void requestPlayGame(String idTo) {
		withID = idTo;
		try {
			ct_buffer.setLength(0);
			ct_buffer.append(REQ_PLAYGAME);
			ct_buffer.append(SEPARATOR);
			ct_buffer.append(st_ID);
			ct_buffer.append(SEPARATOR);
			ct_buffer.append(idTo);
			send(ct_buffer.toString());

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	// ????????? ?????? ????????? ?????????????????? ??????
	public void requestSendResult(String choose) {
		try {

			ct_buffer.setLength(0);
			ct_buffer.append(REQ_GETRESULT);
			ct_buffer.append(SEPARATOR);
			ct_buffer.append(st_ID);
			ct_buffer.append(SEPARATOR);
			ct_buffer.append(choose);
			ct_buffer.append(SEPARATOR);
			send(ct_buffer.toString());

		} catch (IOException e) {
			System.out.println(e);
		}

	}

	// ??? ?????????
	public void requestQuiterRoom(String id) {

		try {
			ct_buffer.setLength(0);
			ct_buffer.append(REQ_QUITROOM);
			ct_buffer.append(SEPARATOR);
			ct_buffer.append(id);
			send(ct_buffer.toString());
			withID = "";
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	// ????????? ??????
	private void send(String sendData) throws IOException {
		    ct_out.writeUTF(sendData);
		    ct_out.flush();
	 }
}