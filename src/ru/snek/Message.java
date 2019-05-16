package ru.snek;

import java.io.Serializable;

public class Message<T extends Serializable> implements Serializable {
    private String command;
    private T data;
    private String stringExtraData;

    public Message(String command) {
        this(command, null, null);
    }

    public Message(String command, T data) {
        this(command, data, null);
    }

    public Message(String command, T data, String stringExtraData) {
        this.command = command;
        this.data = data;
        this.stringExtraData = stringExtraData;
    }

    public String getCommand() { return command; }
    public T getData() { return data; }
    public String getStringExtraData() { return stringExtraData; }
}
