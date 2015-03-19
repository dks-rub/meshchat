package de.rub.dks.meshchat.IM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
* Basic message class, messages will be sent from device to device.
*/
public class Message implements Serializable, Comparable<Message> {
	private static final long serialVersionUID = 957528886230338376L;

	private String text;
	private String ID;
	private String nickname;
	private String date;
	private String chatroom;
	private int ID_color;
	private boolean broadcast = false;

	/**
	* Constructor.
	* Creates a new message and sets all members
	* @param text the messages content
	* @param ID the account id
	* @param nickname the account's nickname
	* @param date a creation date
	* @param chatroom the room this message was sent to
	* @param color the account's color
	*/
	public Message(String text, String ID, String nickname, String date, String chatroom, int color) {
		this.text = text;
		this.ID = ID;
		this.nickname = nickname;
		this.date = date;
		this.ID_color = color;
		this.chatroom = chatroom;
	}

	/**
	* Constructor.
	* Creates a new message and sets some members.
	* The message will be marked as a broadcasted message, thus lacking detailed account information.
	* @param text the messages content
	* @param ID the account id
	* @param chatroom the room this message was sent to
	* @param color the account's color
	*/
	public Message(String text, String ID, String chatroom, int color) {
		this.ID = ID;
		this.ID_color = color;
		this.broadcast = true;
		this.text = text;
		this.chatroom = chatroom;
		nickname = null;
	}
	
	/**
	* Returns the object's bytes
	* @return data bytes of the object
	*/
	public byte[] serialize() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream os = new ObjectOutputStream(out);
			os.writeObject(this);
		} catch (IOException e) {
		}
		return out.toByteArray();
	}

	/**
	* Creates a new object from data bytes
	* @return the object or null if data was corrupted
	*/
	public static Message deserialize(byte[] data) {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is;
		try {
			is = new ObjectInputStream(in);
			return (Message) is.readObject();
		} catch (Exception e) {
		}
		return null;
	}

	public String toString() {
		return nickname + "[" + ID + "] at \"" + chatroom + "\": " + text + " (" + date + ")";
	}

	// comparable for sorting by date
	public int compareTo(Message another) {
		return date.compareTo(another.date);
	}
	
	// Getters and setters

	public String getText() {
		return this.text;
	}

	public String getID() {
		return this.ID;
	}

	public String getNickname() {
		return this.nickname;
	}

	public String getDate() {
		return this.date;
	}

	public String getChatroom() {
		return this.chatroom;
	}

	public int getColor() {
		return this.ID_color;
	}

	public boolean isBroadcast() {
		return this.broadcast;
	}

	public void setBroadcast() {
		this.broadcast = true;
	}
}
