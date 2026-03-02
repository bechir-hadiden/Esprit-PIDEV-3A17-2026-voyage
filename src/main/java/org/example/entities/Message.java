package org.example.entities;

import java.sql.Timestamp;

public class Message {
    private int id;
    private int sender_id;
    private int receiver_id;
    private String content;
    private Timestamp sent_at;

    public Message() {}

    public Message(int id, int sender_id, int receiver_id, String content, Timestamp sent_at) {
        this.id = id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.content = content;
        this.sent_at = sent_at;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSender_id() { return sender_id; }
    public void setSender_id(int sender_id) { this.sender_id = sender_id; }

    public int getReceiver_id() { return receiver_id; }
    public void setReceiver_id(int receiver_id) { this.receiver_id = receiver_id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getSent_at() { return sent_at; }
    public void setSent_at(Timestamp sent_at) { this.sent_at = sent_at; }
}
