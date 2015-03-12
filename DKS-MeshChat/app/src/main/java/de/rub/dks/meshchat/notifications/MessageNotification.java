package de.rub.dks.meshchat.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import de.rub.dks.meshchat.ChatActivity;
import de.rub.dks.meshchat.R;
import de.rub.dks.meshchat.IM.Message;

public class MessageNotification {
	private String title, text;
	private int count;
	private Context context;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder builder;
	private static final int NOTIFICATION_ID = 1;

	public MessageNotification(ChatActivity chatActivity) {
		context = chatActivity;
		notificationManager = (NotificationManager) context.getSystemService(ChatActivity.NOTIFICATION_SERVICE);
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		builder = new NotificationCompat.Builder(context);
		builder.setSound(alarmSound);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setAutoCancel(true);
		builder.setLights(Color.BLUE, 500, 500);
	}

	public void newMessage(Message m) {
		count++;
		if (count == 1) {
			title = "Neue Nachricht von " + m.getNickname();
			text = m.getText();
		} else {
			title = count + " neue Nachrichten";
			text = "Neuste Nachricht von " + m.getNickname() + ":" + m.getText();
		}
	}

	public void reset() {
		count = 0;
	}

	public void displayToUser(Context context) {
		if (count == 0)
			return;
		// Context context = ChatActivity.this;
		Intent intent = new Intent(context, ChatActivity.class);
		// set "i want to return, dont create activity new" flags
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
		// build notification
		builder.setContentTitle(title);
		builder.setContentText(text);
		builder.setContentIntent(contentIntent);
		// notify
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

}
