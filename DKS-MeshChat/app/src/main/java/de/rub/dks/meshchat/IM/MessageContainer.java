package de.rub.dks.meshchat.IM;

import java.util.ArrayList;

public class MessageContainer {
	private static MessageContainer inst = null;

	private ArrayList<Message> messages;

	public static MessageContainer getContainer() {
		if (inst == null)
			inst = new MessageContainer();
		return inst;
	}

	private MessageContainer() {
		messages = new ArrayList<Message>();
		load();
	}

	private void load() {
		// TODO load messages from android device
		
		// boolean check_read=false, check_write=false;
		// StorageHelper<Message> storage = new
		// StorageHelper<Message>(getApplicationContext());
		//
		// try {
		// check_read = storage.read(Globals.CHAT_HISTORY_FN);
		// } catch (StreamCorruptedException e) {
		// e.printStackTrace();
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// try {
		// check_write = storage.write(messages);
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		//
		// if(check_read && check_write){
		// Log.d(Globals.TAG, "messages!");
		// messages = storage.getList();
		// }

	}
	
	public void save(){
		// TODO save messages to android device
	}

	public void add(Message m) {
		messages.add(m);
		// Clocks can be desynchronized...
		// if (messages.size() >= 2 && (messages.get(messages.size() -
		// 1).compareTo(messages.get(messages.size() - 2)) < 0))
		// Collections.sort(messages);
	}

	public ArrayList<Message> getMessages(String chatroom) {
		ArrayList<Message> res = new ArrayList<Message>();
		for (Message m : messages)
			if (m.getChatroom().equals(chatroom))
				res.add(m);
		return res;
	}

	public ArrayList<Message> getAllMessages() {
		return new ArrayList<Message>(messages);
	}

}
