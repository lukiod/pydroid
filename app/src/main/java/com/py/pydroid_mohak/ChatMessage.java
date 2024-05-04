package com.py.pydroid_mohak;

public class ChatMessage {
    private String senderName;
    private String message;
    private String profileImageUrl;

    public ChatMessage(String senderName, String message, String profileImageUrl) {
        this.senderName = senderName;
        this.message = message;
        this.profileImageUrl = profileImageUrl;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
