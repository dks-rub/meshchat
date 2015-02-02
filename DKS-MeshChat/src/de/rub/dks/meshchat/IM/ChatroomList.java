package de.rub.dks.meshchat.IM;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.rub.dks.meshchat.R;

public class ChatroomList extends BaseAdapter {
	public static final String DEFAULT_CHAT_ROOM = "All";
	private Context context;
	private ArrayList<String> rooms;
	private LayoutInflater inflater;

	public ChatroomList(Context pContext) {
		context = pContext;
		rooms = new ArrayList<String>();
		rooms.add(DEFAULT_CHAT_ROOM);
		rooms.add("Test 2");
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.drawer_list_item, parent, false);

		TextView txtTitle = (TextView) itemView.findViewById(R.id.title);

		txtTitle.setText("    "+rooms.get(position));

		return itemView;
	}

	@Override
	public int getCount() {
		return rooms.size();
	}

	@Override
	public Object getItem(int position) {
		return rooms.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}