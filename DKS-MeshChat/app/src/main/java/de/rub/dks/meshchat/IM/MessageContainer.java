package de.rub.dks.meshchat.IM;

import java.util.ArrayList;

/**
* Container class to hold and persistently store Message objects.
*/
public class MessageContainer {
	private static MessageContainer inst = null;
	private ArrayList<Message> messages;
	
	// singleton pattern, private constructor
	private MessageContainer() {
		messages = new ArrayList<Message>();
		load();
	}

	/**
	* Instance provider
	* @return the single instance of this class, always the same object
	*/
	public static MessageContainer getContainer() {
		if (inst == null)
			inst = new MessageContainer();
		return inst;
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
	
	/**
	* Saves the contained messages to the device
	* @return indicator for success or failure
	*/
	public boolean save(){
		// TODO save messages to android device
		return false;
	}

	/**
	* Add a new message to the container
	* @param m new message
	*/
	public void add(Message m) {
		messages.add(m);
		// Clocks can be desynchronized, so sorting may go wrong...
		// if (messages.size() >= 2 && (messages.get(messages.size() -
		// 1).compareTo(messages.get(messages.size() - 2)) < 0))
		// Collections.sort(messages);
	}

	/**
	* Get all messages in the container which belong to a certain chatroom
	* @param chatroom the chatroom of interest
	* @return the corresponding messages in a list
	*/
	public ArrayList<Message> getMessages(String chatroom) {
		ArrayList<Message> res = new ArrayList<Message>();
		for (Message m : messages)
			if (m.getChatroom().equals(chatroom))
				res.add(m);
		return res;
	}

	/**
	* Get all messages in the container.
	* @return the messages in a new list
	*/
	public ArrayList<Message> getAllMessages() {
		return new ArrayList<Message>(messages);
	}

}
