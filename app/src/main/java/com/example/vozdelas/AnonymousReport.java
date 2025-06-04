package com.example.vozdelas;

import com.google.firebase.Timestamp;

public class AnonymousReport {
    private String text;
    private Timestamp timestamp;
    private String senderUid; // O UID do usuário autenticado anonimamente

    public AnonymousReport() {
        // Construtor vazio necessário para Firebase Firestore
    }

    public AnonymousReport(String text, Timestamp timestamp, String senderUid) {
        this.text = text;
        this.timestamp = timestamp;
        this.senderUid = senderUid;
    }

    // Getters
    public String getText() {
        return text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getSenderUid() {
        return senderUid;
    }

    // Setters
    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }
}