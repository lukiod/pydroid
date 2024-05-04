package com.py.pydroid_mohak;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    private Context mContext;
    private int mResource;

    public ChatMessageAdapter(Context context, int resource, ArrayList<ChatMessage> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
        }

        ImageView profileImageView = convertView.findViewById(R.id.profileImageView);
        TextView senderNameTextView = convertView.findViewById(R.id.senderNameTextView);
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);

        ChatMessage chatMessage = getItem(position);

        if (chatMessage != null) {
            senderNameTextView.setText(chatMessage.getSenderName());
            messageTextView.setText(chatMessage.getMessage());

            // Load profile image using Glide library
            if (chatMessage.getProfileImageUrl() != null) {
                Glide.with(mContext)
                        .load(chatMessage.getProfileImageUrl())
                        .into(profileImageView);
            }
        }

        return convertView;
    }
}
