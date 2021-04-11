package com.example.EnglishApp.models;

public class ChatClass  {
    private Message Messages;
    private String chatWith;
    private String organizer;

    ChatClass(){}

    public ChatClass(String chatWith, String organizer) {
        this.chatWith = chatWith;
        this.organizer = organizer;
    }



    public Message getMessages() {
        return Messages;
    }

    public void setMessages(Message messages) {
        this.Messages = messages;
    }

    public String getChatWith() {
        return chatWith;
    }

    public void setChatWith(String chatWith) {
        this.chatWith = chatWith;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }
}
