package RockPaperScissors;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import RockPaperScissors.rpsServer;
import RockPaperScissors.ServerThread;

public class ServerThread extends Thread {
	private Socket st_sock;
	private DataInputStream st_in;
	private DataOutputStream st_out;
	private StringBuffer st_buffer;

	private static Hashtable<String, ServerThread> logonHash;
	private static Vector<String> logonVector;
	private static Hashtable<String, ServerThread> playerHash;
	private static Vector<String> playerVector;

	private static int isOpenRoom = 0;
	private rpsClient st_client;
	private static final String SEPARATOR = "|";
	private static final String DELIMETER = "`";

	public String st_ID;
	private static String choose1 = null;
	private static String choose2 = null;
	private static ServerThread client1 = null;
	private static ServerThread client2 = null;
	private static int chooseInt1 = 0;
	private static int chooseInt2 = 0;
	private Play gameroom;

	private static final int REQ_LOGON = 1001;
	private static final int REQ_SENDWORDS = 1021;
	private static final int REQ_LOGOUT = 1031;
	private static final int REQ_QUITROOM = 1041;
	private static final int REQ_GETRESULT = 1042;
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

	private static final int ERR_NOUSER = 2015;
	private static final int ERR_REJECITON = 2019;
	private static final int ERR_ALREADYPLAYER = 2033;
	private static final int MSG_ALREADYUSER = 3001;
	private static final int MSG_SERVERFULL = 3002;
	private static final int MSG_CANNOTOPEN = 3011;

	static {
		logonHash = new Hashtable<String, ServerThread>(rpsServer.maxclient);
		logonVector = new Vector<String>(rpsServer.maxclient);
		playerHash = new Hashtable<String, ServerThread>(rpsServer.maxclient);
		playerVector = new Vector<String>(rpsServer.maxclient);
	}

	public ServerThread(Socket sock) {

		try {
			st_sock = sock;
			st_in = new DataInputStream(sock.getInputStream());
			st_out = new DataOutputStream(sock.getOutputStream());
			st_buffer = new StringBuffer(2048);

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void run() {
		try {
			while (true) {
				String recvData = st_in.readUTF();
				StringTokenizer st = new StringTokenizer(recvData, SEPARATOR);
				int command = Integer.parseInt(st.nextToken());
				switch (command) {

				case REQ_LOGON: {
					int result;
					String id = st.nextToken();
					result = addUser(id, this);
					st_buffer.setLength(0);
					if (result == 0) {
						st_buffer.append(YES_LOGON);
						st_buffer.append(SEPARATOR);
						st_buffer.append(SEPARATOR);
						String userIDs = getUsers();
						st_buffer.append(userIDs);
						send(st_buffer.toString());

						st_buffer.setLength(0);
						st_buffer.append(MDY_MEMVER);
						st_buffer.append(SEPARATOR);
						st_buffer.append(userIDs);
						broadcast_log(st_buffer.toString());

						String playerIDs = getPlayers();
						if (!playerIDs.equals("")) {
							st_buffer.setLength(0);
							st_buffer.append(MDY_PLAYERS);
							st_buffer.append(SEPARATOR);
							st_buffer.append(playerIDs);
							broadcast_log(st_buffer.toString());
						}

					} else { // 접속불가 상태
						st_buffer.append(NO_LOGON);
						st_buffer.append(SEPARATOR);
						st_buffer.append(result);
						send(st_buffer.toString());
					}
					break;
				}
				case REQ_LOGOUT: {
					st_buffer.setLength(0);
					String id = st.nextToken(); 

					logonVector.removeElement(id);
					logonHash.remove(id, this);

					st_buffer.append(YES_LOGOUT);
					send(st_buffer.toString());

					st_buffer.setLength(0);
					st_buffer.append(MDY_MEMVER);
					st_buffer.append(SEPARATOR);
					String userIDs = getUsers();
					st_buffer.append(userIDs);
					broadcast_log(st_buffer.toString());

					break;
				}
				// 게임 요청
				case REQ_PLAYGAME: {
					String id = st.nextToken();
					String idTo = st.nextToken();

					ServerThread client = null;
					ServerThread client_p = null;

					// 게임을 요청받은 사용자를 hash로 뽑아내 요청 메세지 보내기
					if ((client = (ServerThread) logonHash.get(idTo)) != null) {
						// 만약 게임방에 있는 사용자 일때
						if ((client_p = (ServerThread) playerHash.get(idTo)) != null) {
							st_buffer.setLength(0);
							st_buffer.append(NO_PLAYGAME);
							st_buffer.append(SEPARATOR);
							st_buffer.append(ERR_ALREADYPLAYER);
							st_buffer.append(SEPARATOR);
							st_buffer.append(idTo);
							send(st_buffer.toString());
							break;
						}
						st_buffer.setLength(0);
						st_buffer.append(REQ_PLAYGAME);
						st_buffer.append(SEPARATOR);
						st_buffer.append(id);
						st_buffer.append(SEPARATOR);
						st_buffer.append(idTo);
						client.send(st_buffer.toString());
						break;
					} else { // 사용자를 선택하지 않았을때
						st_buffer.setLength(0);
						st_buffer.append(NO_PLAYGAME);
						st_buffer.append(SEPARATOR);
						st_buffer.append(ERR_NOUSER);
						st_buffer.append(idTo);
						send(st_buffer.toString());
						break;
					}

				}
				// 상대방이 수락을 하여 게임을 시작한다.
				case YES_PLAYGAME: {
					String id = st.nextToken();
					String idTo = st.nextToken();

					playerVector.addElement(id); // 참여자들 벡터와 해쉬테이블에 추가
					playerHash.put(id, this);
					playerVector.addElement(idTo);
					playerHash.put(idTo, this);

					// 요청한 아이디에게 상대방이 게임을 수락하였다고 알려줌.
					ServerThread client = null;
					client = (ServerThread) logonHash.get(id);
					st_buffer.setLength(0);
					st_buffer.append(YES_PLAYGAME);
					st_buffer.append(SEPARATOR);
					st_buffer.append(idTo);
					client.send(st_buffer.toString());

					String playerIDs = getPlayers(); // 게임참여자목록 수정
					st_buffer.setLength(0);
					st_buffer.append(MDY_PLAYERS);
					st_buffer.append(SEPARATOR);
					st_buffer.append(playerIDs);
					broadcast_log(st_buffer.toString());

					break;

				}

				// 상대방이 게임을 거부한다.
				case NO_PLAYGAME: {
					String id = st.nextToken();
					ServerThread client = null;
					client = (ServerThread) logonHash.get(id);
					st_buffer.setLength(0);
					st_buffer.append(NO_PLAYGAME);
					st_buffer.append(SEPARATOR);
					st_buffer.append(ERR_REJECITON);
					st_buffer.append(SEPARATOR);
					st_buffer.append(id);

					client.send(st_buffer.toString());
					break;

				}

				// 게임 결과 계산하기
				case REQ_GETRESULT: {

					String id = st.nextToken();
					st_buffer.setLength(0);
					st_buffer.append(YES_GETRESULT);
					st_buffer.append(SEPARATOR);

					if (client1 == null) {
						client1 = (ServerThread) logonHash.get(id);
					} else { 
						client2 = (ServerThread) logonHash.get(id);
					}

					if (choose1 == null) {
						choose1 = st.nextToken();
						chooseInt1 = Integer.parseInt(choose1);

					} else {
						choose2 = st.nextToken();
						chooseInt2 = Integer.parseInt(choose2);
					}

					if (choose1 != null && choose2 != null) { // 두 버튼이 모두 눌려지면 결과 계산하기
						switch (chooseInt1) {
						case 0: // 가위
							if (chooseInt2 == 0) {
								st_buffer.append("상대방과 비겼습니다.");
								client1.send(st_buffer.toString());
								client2.send(st_buffer.toString());

								choose1 = choose2 = null;
								client1 = client2 = null;
								break;

							} else if (chooseInt2 == 1) {
								st_buffer.append("상대방에게 졌습니다.");
								client1.send(st_buffer.toString());
								System.out.println(st_buffer.toString() + "\n");
								st_buffer.delete(st_buffer.length() - 11, st_buffer.length() - 1);
								st_buffer.append("상대방에게 이겼습니다!!");
								client2.send(st_buffer.toString());

								choose1 = choose2 = null;
								client1 = client2 = null;
								break;

							} else if (chooseInt2 == 2) {
								st_buffer.append("상대방에게 이겼습니다!!");
								client1.send(st_buffer.toString());
								st_buffer.delete(st_buffer.length() - 12, st_buffer.length() - 1);
								st_buffer.append("상대방에게 졌습니다.");
								client2.send(st_buffer.toString());

								choose1 = choose2 = null;
								client1 = client2 = null;
								break;

							}

						case 1: // 바위

							if (chooseInt2 == 0) {
								st_buffer.append("상대방에게 이겼습니다!!");
								client1.send(st_buffer.toString());
								st_buffer.delete(st_buffer.length() - 12, st_buffer.length() - 1);
								st_buffer.append("상대방에게 졌습니다.");
								client2.send(st_buffer.toString());

								choose1 = choose2 = null;
								client1 = client2 = null;
								break;

							} else if (chooseInt2 == 1) {
								st_buffer.append("상대방과 비겼습니다.");
								client1.send(st_buffer.toString());
								client2.send(st_buffer.toString());

								choose1 = choose2 = null;
								client1 = client2 = null;
								break;

							} else if (chooseInt2 == 2) {
								st_buffer.append("상대방에게 졌습니다.");
								client1.send(st_buffer.toString());

								st_buffer.delete(st_buffer.length() - 11, st_buffer.length() - 1);
								st_buffer.append("상대방에게 이겼습니다!!");
								client2.send(st_buffer.toString());

								choose1 = choose2 = null;
								client1 = client2 = null;
								break;

							}
						case 2: // 보

							if (chooseInt2 == 0) {
								st_buffer.append("상대방에게 졌습니다.");
								client1.send(st_buffer.toString());
								st_buffer.delete(st_buffer.length() - 11, st_buffer.length() - 1);
								st_buffer.append("상대방에게 이겼습니다!!");
								client2.send(st_buffer.toString());

								choose1 = choose2 = null;
								client1 = client2 = null;
								break;

							} else if (chooseInt2 == 1) {
								st_buffer.append("상대방에게 이겼습니다!!");
								client1.send(st_buffer.toString());
								st_buffer.delete(st_buffer.length() - 12, st_buffer.length() - 1);
								st_buffer.append("상대방에게 졌습니다.");
								client2.send(st_buffer.toString());

								choose1 = choose2 = null;
								client1 = client2 = null;
								break;

							} else if (chooseInt2 == 2) {
								st_buffer.append("상대방과 비겼습니다.");
								client1.send(st_buffer.toString());
								client2.send(st_buffer.toString());

								choose1 = choose2 = null;
								client1 = client2 = null;
								break;
							}

						}
					}
					break;

				}
				// 게임방을 나갈때
				case REQ_QUITROOM: {
					st_buffer.setLength(0);
					String id = st.nextToken();

					playerVector.removeElement(id); // 게임참여자 리스트에서 제거
					playerHash.remove(id, this);

					st_buffer.append(YES_QUITROOM);
					send(st_buffer.toString());
					String PlayerIDs = getPlayers();

					st_buffer.setLength(0);
					st_buffer.append(MDY_PLAYERS);
					st_buffer.append(SEPARATOR);
					// PlayerIDs의 null예외를 막기위해
					if (PlayerIDs.equals(""))
						PlayerIDs = "참여자 없음";
					st_buffer.append(PlayerIDs);
					broadcast_log(st_buffer.toString());
					break;
				}

				}

				Thread.sleep(100);
			}

		} catch (NullPointerException e) {
		} catch (InterruptedException e) {
		} catch (IOException e) {
		}
	}

	private static synchronized int addUser(String id, ServerThread client) {
		if (checkUserID(id) != null) {
			return MSG_ALREADYUSER;
		}
		if (logonHash.size() >= rpsServer.maxclient) {
			return MSG_SERVERFULL;
		}
		logonVector.addElement(id);
		logonHash.put(id, client);
		client.st_ID = id;
		return 0;
	}

	private static ServerThread checkUserID(String id) {
		ServerThread alreadyClient = null;
		alreadyClient = (ServerThread) logonHash.get(id);
		return alreadyClient;
	}

	private String getUsers() {
		StringBuffer id = new StringBuffer();
		String ids;
		Enumeration<String> enu = logonVector.elements();
		while (enu.hasMoreElements()) {
			id.append(enu.nextElement());
			id.append(DELIMETER);
		}
		try {
			ids = new String(id); // 문자열로 변환한다.
			ids = ids.substring(0, ids.length() - 1); // 마지막 "`"를 삭제한다.
		} catch (StringIndexOutOfBoundsException e) {
			return "";
		}
		return ids;
	}

	// 게임방에 참여한 사용자 ID를 구한다.
	private String getPlayers() {
		StringBuffer id = new StringBuffer();
		String ids;
		Enumeration<String> enu = playerVector.elements();
		while (enu.hasMoreElements()) {
			id.append(enu.nextElement());
			id.append(DELIMETER);
		}
		try {
			ids = new String(id);
			ids = ids.substring(0, ids.length() - 1); // 마지막 "`"를 삭제한다.
		} catch (StringIndexOutOfBoundsException e) {
			return "";
		}
		return ids;
	}

	// 로그인한 사용자들에게 (브로드케스팅) 데이터를 전송한다.
	public synchronized void broadcast_log(String sendData) throws IOException {
		ServerThread client;
		Enumeration<String> enu = logonVector.elements();
		while (enu.hasMoreElements()) {
			client = (ServerThread) logonHash.get(enu.nextElement());
			client.send(sendData);
		}
	}

	// 게임방에 참여한 모든 사용자(브로드케스팅)에게 데이터를 전송한다.
	public synchronized void broadcast(String sendData) throws IOException {
		ServerThread client;
		Enumeration<String> enu = playerVector.elements();
		while (enu.hasMoreElements()) {
			client = (ServerThread) playerHash.get(enu.nextElement());
			client.send(sendData);
		}
	}

	// 데이터를 전송한다.
	public void send(String sendData) throws IOException {
		synchronized (st_out) {
			st_out.writeUTF(sendData);
			st_out.flush();
		}
	}
}