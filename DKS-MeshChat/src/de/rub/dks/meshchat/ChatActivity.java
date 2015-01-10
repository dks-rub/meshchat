package de.rub.dks.meshchat;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import de.rub.dks.meshchat.IM.Message;
import de.rub.dks.meshchat.IM.MessageReceiver;
import de.rub.dks.meshchat.IM.MessageSender;
import de.rub.dks.meshchat.account.Account;

public class ChatActivity extends Activity {

	// Persistent Settings
	private final String sharedPref = "de.rub.dks.meshchat.sharedPrefereces";
	private SharedPreferences sPref;
	private final String ID_data = "de.rub.dks.meshchat.ID";
	private final String ID_color_data = "de.rub.dks.meshchat.ID_color";
	private final String nickname_data = "de.rub.dks.meshchat.nickname";

	// Account
	private String ID = null;
	private int ID_color = 0;
	private String nickname = null;

	private String test_msg;

	private MessageReceiver receiver;
	private MessageSender sender;
	private EditText msg;
	private ArrayList<Message> messages;
	private Button snd_bt;
	private Time time = new Time(Time.getCurrentTimezone());
	private ScrollView chat;

	//
	private void init(SharedPreferences.Editor editor) {
		if (ID == null && ID_color == 0) {
			Account.createAccount();
			this.ID = Account.getID();
			this.nickname = this.ID;
			this.ID_color = Account.getColor();
		}
		this.ID = sPref.getString(ID_data, ID);
		this.ID_color = sPref.getInt(ID_color_data, ID_color);
		this.nickname = sPref.getString(nickname_data, nickname);
		editor.putString(ID_data, ID);
		editor.putString(nickname_data, nickname);
		editor.putInt(ID_color_data, ID_color);
		editor.commit();
		try {
			findViewById(R.id.profil).setBackgroundColor(ID_color);
			((TextView) findViewById(R.id.nickname)).setText(nickname);
		} catch (Exception e) {
			Log.d(Globals.TAG, e.toString());
		}
		return;
	}

	private void resetNickname(String newNickname) {
		this.nickname = newNickname;
		SharedPreferences.Editor editor = sPref.edit();
		editor.putString(nickname_data, newNickname);
		editor.commit();
		((TextView) findViewById(R.id.nickname)).setText(nickname);
		return;
	}

	private View getMessageView(Message m) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View layout_msg = null;
		if (m.getID().equals(ID)) {
			layout_msg = inflater.inflate(R.layout.snd_msg, null);
		} else {
			layout_msg = inflater.inflate(R.layout.rec_msg, null);
		}
		layout_msg.findViewById(R.id.profil).setBackgroundColor(m.getColor());
		((TextView) layout_msg.findViewById(R.id.nickname)).setText(m.getNickname());
		((TextView) layout_msg.findViewById(R.id.text)).setText(m.getText());
		((TextView) layout_msg.findViewById(R.id.date)).setText(m.getDate());

		// Adding next message and scroll down
		return layout_msg;

	}

	public void refreshMessages() {
		LinearLayout msg_container = (LinearLayout) findViewById(R.id.msg_container);
		if (messages.size() < 2 || (messages.get(messages.size() - 1).compareTo(messages.get(messages.size() - 2)) >= 0)) {
			msg_container.addView(getMessageView(messages.get(messages.size() - 1)));
		} else {
			Collections.sort(messages);
			msg_container.removeAllViews();
			for (Message m : messages)
				msg_container.addView(getMessageView(m));
		}
		chat.post(new Runnable() {
			public void run() {
				chat.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		messages = new ArrayList<Message>();

		Handler handler = new Handler(Looper.getMainLooper());

		receiver = new MessageReceiver(getApplicationContext(), handler, new Runnable() {
			public void run() {
				Message m = receiver.getMessage();
				if (m == null)
					return;
				Log.d(Globals.TAG, "Received: " + m.toString());
				if (ID.equals(m.getID())) {
					Log.d(Globals.TAG, "Dropped own message");
					return;
				}
				messages.add(m);
				refreshMessages();
			}
		});
		receiver.start();
		sender = new MessageSender(getApplicationContext());

		Toast.makeText(getApplicationContext(), "Multicast IP: " + sender.getMulticastIp(), Toast.LENGTH_LONG).show();

		sPref = this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sPref.edit();

		// Init or restore current profil settings
		init(editor);

		msg = (EditText) findViewById(R.id.chat_field);
		snd_bt = (Button) findViewById(R.id.send_bt);
		chat = (ScrollView) findViewById(R.id.chat_container);

		snd_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (msg.getText().toString().trim().equals(""))
					return;
				time.setToNow();
				Message m = new Message(msg.getText().toString().trim(), ID, nickname, time.format("%d.%m.%y, %k:%M:%S"), ID_color);
				messages.add(m);
				msg.setText("");
				Log.d(Globals.TAG, "Sending: " + m.toString());
				refreshMessages();
				sender.sendMessage(m);
				// receiver.receive(m);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onStop() {
		receiver.stop();
		super.onStop();
	}
}
