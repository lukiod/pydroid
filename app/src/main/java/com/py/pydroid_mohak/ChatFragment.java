package com.py.pydroid_mohak;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;

public class ChatFragment extends Fragment {

    private ListView chatListView;
    private EditText messageEditText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference usersCollection, chatCollection;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> chatList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("Users");
        chatCollection = db.collection("Rooms").document("0").collection("chat"); // Fixed room ID of 0

        chatListView = view.findViewById(R.id.chatListView);
        messageEditText = view.findViewById(R.id.messageEditText);

        chatList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, chatList);
        chatListView.setAdapter(adapter);

        loadChatMessages();

        view.findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage());

        return view;
    }

    private void loadChatMessages() {
        chatCollection.orderBy("timestamp", Query.Direction.ASCENDING) // Assuming you have a timestamp field for ordering
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            String senderId = change.getDocument().getString("sender");
                            String message = change.getDocument().getString("message");

                            usersCollection.document(senderId).get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String username = task.getResult().getString("FullName"); // Accessing "FullName" field
                                    chatList.add(username + ": " + message);
                                    adapter.notifyDataSetChanged();
                                    chatListView.smoothScrollToPosition(chatList.size() - 1);
                                } else {
                                    Toast.makeText(getContext(), "Failed to load username", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
    }


    public void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String senderId = currentUser.getUid();
                Map<String, Object> chatMessage = new HashMap<>();
                chatMessage.put("sender", senderId);
                chatMessage.put("message", message);
                chatMessage.put("timestamp", new Date());
                chatCollection.add(chatMessage)
                        .addOnSuccessListener(documentReference -> {
                            // Message sent successfully
                            messageEditText.setText("");
                        })
                        .addOnFailureListener(e -> {
                            // Handle error
                            Toast.makeText(getContext(), "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        }
    }
}