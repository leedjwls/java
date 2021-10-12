package RockPaperScissors;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;

import RockPaperScissors.rpsClient;
import RockPaperScissors.ClientThread;

public class Play extends Frame implements ActionListener {

	private Button bt_scissors, bt_paper, bt_rock;
	public Label lb_status;

	public TextArea dr_taContents;
	public List dr_lstMember;

	public TextField userID1, roomname;

	public static ClientThread dr_thread;
	public static rpsClient dr_client;
	public String choose;

	public Play(RockPaperScissors.ClientThread clientThread, String title, String id, String idTo) {
		super(title);
		dr_thread = clientThread;
		setLayout(new BorderLayout());

		Panel northpanel = new Panel();
		northpanel.setLayout(new BorderLayout());
		Panel roomnamepanel = new Panel();
		roomname = new TextField(id + " vs " + idTo);
		roomname.setEditable(false);
		northpanel.add(roomname);
		
		roomname.setEnabled(false);
		roomnamepanel.add(roomname, BorderLayout.EAST);
		northpanel.add(roomnamepanel, BorderLayout.NORTH);
		

		Panel buttonpanel = new Panel();
		bt_scissors = new Button("가위");
		buttonpanel.add(bt_scissors, BorderLayout.EAST);
		bt_scissors.addActionListener(this);

		bt_paper = new Button("바위");
		buttonpanel.add(bt_paper, BorderLayout.CENTER);
		bt_paper.addActionListener(this);

		bt_rock = new Button("보");
		buttonpanel.add(bt_rock, BorderLayout.WEST);
		bt_rock.addActionListener(this);

		Panel southpanel = new Panel();
		lb_status = new Label("게임 진행중");
		southpanel.add(lb_status);

		add("North", northpanel);
		add("Center", buttonpanel);
		add("South", southpanel);

		addWindowListener(new WinListener());

	}

	class WinListener extends WindowAdapter {
		public void windowClosing(WindowEvent we) {
			dr_thread.requestQuiterRoom(dr_thread.st_ID);
		}
	}

	public void actionPerformed(ActionEvent ae) {
		Button b = (Button) ae.getSource();
		if (b.getLabel().equals("가위")) {
			dr_thread.requestSendResult("0");

		} else if (b.getLabel().equals("바위")) {
			dr_thread.requestSendResult("1");
		} else if (b.getLabel().equals("보")) {
			dr_thread.requestSendResult("2");
		}

	}

}