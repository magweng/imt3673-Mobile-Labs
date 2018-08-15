package com.example.magnus.chatapplication;

/**
 * Class that holds a message
 */

class Message {
    private final String date;
    private final String userNickName;
    private final String message;


    public Message(String date, String userNick, String message) {
        this.date = date;
        this.userNickName = userNick;
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public String getMessage() {
        return message;
    }

}
