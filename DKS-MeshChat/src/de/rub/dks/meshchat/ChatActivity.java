package de.rub.dks.meshchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import de.rub.dks.meshchat.IM.ChatroomList;
import de.rub.dks.meshchat.IM.Message;
import de.rub.dks.meshchat.IM.MessageContainer;
import de.rub.dks.meshchat.IM.MessageReceiver;
import de.rub.dks.meshchat.IM.MessageSender;
import de.rub.dks.meshchat.account.Account;
import de.rub.dks.meshchat.helper.UpdateHelper;
import de.rub.dks.meshchat.notifications.MessageNotification;

public class ChatActivity extends Activity implements ListView.OnItemClickListener {

	// Persistent Settings
	private SharedPreferences sPref;

	// Account
	private String ID = null;
	private int ID_color = 0;
	private String nickname = null;

	// Shows if Activity is watched
	private boolean activity_active;

	// Drawer Layout
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ChatroomList chatroomList;
	private String chatroom = "Test Room";

	// Notification Service
	private MessageNotification notification;

	// Chat internals
	private MessageReceiver receiver;
	private MessageSender sender;
	private EditText msg;

	private Button snd_bt;
	private Time time = new Time(Time.getCurrentTimezone());
	private ScrollView chat;

	// Init for checking whether the account is created or not
	private void init(SharedPreferences.Editor editor) {
		notification = new MessageNotification(ChatActivity.this);
		// Account is not created
		if (sPref.getBoolean(Globals.FIRST_STARTUP, true)) {
			// Create the account
			Account.createAccount();
			this.ID = Account.getID();
			this.ID_color = Account.getColor();
			editor.putInt(Globals.ID_COLOR_DATA, this.ID_color);
			editor.putBoolean(Globals.FIRST_STARTUP, false);
			editor.apply();
		}
		// Restore default or previously saved values
		this.ID = sPref.getString(Globals.ID_DATA, ID);
		this.ID_color = sPref.getInt(Globals.ID_COLOR_DATA, ID_color);
		this.nickname = sPref.getString(Globals.NICKNAME_DATA, this.ID);
		editor.putString(Globals.ID_DATA, ID);
		editor.putString(Globals.NICKNAME_DATA, nickname);
		editor.putInt(Globals.ID_COLOR_DATA, ID_color);
		editor.commit();

		try {
			findViewById(R.id.profil).setBackgroundColor(ID_color);
			((TextView) findViewById(R.id.nickname)).setText(nickname);
		} catch (Exception e) {
			Log.d(Globals.TAG, e.toString());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Globals.INIT_SUCCESS && requestCode == Globals.FIRST_INIT) {
			if (data.hasExtra("nickname")) {
				SharedPreferences.Editor editor = sPref.edit();

				Message m = new Message(data.getExtras().getString("nickname") + getString(R.string.new_person), this.ID, time.format("%d.%m.%y, %k:%M:%S"), chatroom, ID_color);

				editor.putString(Globals.NICKNAME_DATA, data.getExtras().getString("nickname"));
				editor.commit();
				init(editor);

				// Broadcast new person is in the chat
				MessageContainer.getContainer().add(m);
				sender.sendMessage(m);
			} else {
				finish();
			}
		} else if (resultCode == Globals.INIT_SUCCESS && requestCode == Globals.RE_INIT) {
			if (data.hasExtra("nickname")) {
				SharedPreferences.Editor editor = sPref.edit();

				Message m = new Message(this.nickname + getString(R.string.change_nickname) + data.getExtras().getString("nickname"), this.ID, time.format("%d.%m.%y, %k:%M:%S"), chatroom, ID_color);

				editor.putString(Globals.NICKNAME_DATA, data.getExtras().getString("nickname"));
				editor.putBoolean(Globals.FIRST_STARTUP, true);
				editor.commit();
				init(editor);

				// Broadcast name change to chat
				MessageContainer.getContainer().add(m);
				sender.sendMessage(m);
			}
		} else if (resultCode == Globals.INIT_FAIL) {
			Log.d(Globals.TAG, Globals.INIT_FAIL + ": Intent failed");
		}
	}

	private View getMessageView(Message m) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Creates different views, for own and received messages
		View layout_msg = null;
		if (m.isBroadcast()) {
			layout_msg = inflater.inflate(R.layout.brc_msg, null);
			layout_msg.findViewById(R.id.brc_container).setBackgroundColor(m.getColor());
			((TextView) layout_msg.findViewById(R.id.brc_message)).setText(m.getText());
			return layout_msg;
		}

		if (m.getID().equals(ID)) {
			layout_msg = inflater.inflate(R.layout.snd_msg, null);
		} else {
			layout_msg = inflater.inflate(R.layout.rec_msg, null);
		}
		// Adding content to the view
		layout_msg.findViewById(R.id.profil).setBackgroundColor(m.getColor());
		((TextView) layout_msg.findViewById(R.id.nickname)).setText(m.getNickname());
		((TextView) layout_msg.findViewById(R.id.text)).setText(m.getText());
		((TextView) layout_msg.findViewById(R.id.date)).setText(m.getDate());

		// Adding next message and scroll down
		return layout_msg;

	}

	public void refreshMessages() {
		LinearLayout msg_view_container = (LinearLayout) findViewById(R.id.msg_container);
		msg_view_container.removeAllViews();
		for (Message m : MessageContainer.getContainer().getMessages(chatroom))
			msg_view_container.addView(getMessageView(m));
		chat.post(new Runnable() {
			public void run() {
				chat.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	public void enterChatroom(String newChatroom) {
		if (chatroom != null) {
			if (chatroom.equals(newChatroom))
				return;
			Message m = new Message(this.nickname + getString(R.string.leave), this.ID, time.format("%d.%m.%y, %k:%M:%S"), chatroom, ID_color);
			sender.sendMessage(m);
		}
		chatroom = newChatroom;
		getActionBar().setTitle(chatroom);
		// say hello in chatroom
		Message m = new Message(nickname + getString(R.string.new_person), this.ID, time.format("%d.%m.%y, %k:%M:%S"), chatroom, ID_color);
		MessageContainer.getContainer().add(new Message("You are now in the chatroom \"" + chatroom + "\"", this.ID, time.format("%d.%m.%y, %k:%M:%S"), chatroom, ID_color));
		sender.sendMessage(m);
		refreshMessages();
	}
	
	//Create a dialog to change chatrooms
	public void onChatroomDialog(Context c) {
		AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(c);
		aDialogBuilder.setTitle("Creat Chatroom");
		
		final EditText input = new EditText(c);
		aDialogBuilder.setView(input);
		//Accept
		aDialogBuilder.setMessage("Create a new Chatroom or join an exsisting one.").setPositiveButton("Create/Join", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				enterChatroom((input.getText()).toString());
			}
		//Decline
		}).setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog aDialog = aDialogBuilder.create();
		aDialog.show();
	}

	// Main window
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		chatroomList = new ChatroomList(this);
		drawerList.setAdapter(chatroomList);
		drawerList.setOnItemClickListener(this);

		// check updates
		Handler mHandler = new Handler(Looper.getMainLooper());
		UpdateHelper uh = new UpdateHelper(this, getString(R.string.app_name), mHandler);
		uh.startThread();

		Handler handler = new Handler(Looper.getMainLooper());

		// Receiver checks for new incoming messages
		receiver = new MessageReceiver(getApplicationContext(), handler, new Runnable() {
			public void run() {
				Message[] newMessages = receiver.getMessages();
				for (Message m : newMessages) {
					Log.d(Globals.TAG, "Received: " + m.toString());
					if (ID.equals(m.getID())) {
						Log.d(Globals.TAG, "Dropped own message");
						return;
					}
					MessageContainer.getContainer().add(m);
					if (!activity_active && m.getNickname() != null)
						notification.newMessage(m);
					refreshMessages();
				}
				// Should notify the user, when a message arrives.
				if (!activity_active)
					notification.displayToUser(ChatActivity.this);
			}
		});
		receiver.start();
		sender = new MessageSender(getApplicationContext());

		// Toast.makeText(getApplicationContext(), "Multicast IP: " +
		// sender.getMulticastIp(), Toast.LENGTH_LONG).show();
		setTitle(getTitle().toString() + " - " + sender.getMulticastIp());

		// setting up the sharedPreferences
		sPref = this.getSharedPreferences(Globals.SHARED_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sPref.edit();

		if (sPref.getBoolean(Globals.FIRST_STARTUP, true)) {
			// Open the dialog to create a new account
			Intent i = new Intent(this, AccountActivity.class);
			startActivityForResult(i, Globals.FIRST_INIT);
		}
		// Init or restore current profile settings
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
				Message m = new Message(msg.getText().toString().trim(), ID, nickname, time.format("%d.%m.%y, %k:%M:%S"), chatroom, ID_color);
				MessageContainer.getContainer().add(m);
				msg.setText("");
				Log.d(Globals.TAG, "Sending: " + m.toString());
				refreshMessages();
				sender.sendMessage(m);
			}
		});
		enterChatroom(ChatroomList.DEFAULT_CHAT_ROOM);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	// Create menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

	// Menu internals
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		// if (id == R.id.action_settings) {
		// Intent i = new Intent(this, SettingsActivity.class);
		// startActivity(i);
		// return true;
		// } else
		if (id == R.id.action_account) {
			Intent i = new Intent(this, AccountActivity.class);
			startActivityForResult(i, Globals.RE_INIT);
			return true;
		} else if (id == R.id.action_leave) {
			finish();
			return true;
		} else if (id == R.id.enter_chatroom){
			onChatroomDialog(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onResume() {
		activity_active = true;
		// Message m = new Message(this.nickname+" is now in the chat.",
		// time.format("%d.%m.%y, %k:%M:%S"), ID_color);
		// sender.sendMessage(m);
		super.onResume();
		notification.reset();
		runOnUiThread(new Runnable() {
			public void run() {
				refreshMessages();
			}
		});

	}

	public void onPause() {
		activity_active = false;
		// Message m = new Message(this.nickname+getString(R.string.leave),
		// time.format("%d.%m.%y, %k:%M:%S"), ID_color);
		// sender.sendMessage(m);
		super.onPause();
	}

	public void finish() {
		// receiver.stop(); finish calls onDestroy in the process
		if (!sPref.getBoolean(Globals.FIRST_STARTUP, true)) {
			Message m = new Message(this.nickname + getString(R.string.leave), this.ID, time.format("%d.%m.%y, %k:%M:%S"), chatroom, ID_color);
			sender.sendMessage(m);
		}
		super.finish();
	}

	public void onDestroy() {
		MessageContainer.getContainer().save();
		receiver.stop();
		super.onDestroy();
	}

	@Override
	// drawer listener to select chatroom
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		drawerList.setItemChecked(position, true);
		String newChatroom = (String) chatroomList.getItem(position);
		drawerLayout.closeDrawer(drawerList);
		enterChatroom(newChatroom);
	}

}
