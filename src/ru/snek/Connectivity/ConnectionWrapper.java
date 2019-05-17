package ru.snek.Connectivity;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

public class ConnectionWrapper {
    private Socket soc;
    private SocketChannel chan;
    private boolean std;

    public ConnectionWrapper(Socket soc) {
        std = true;
        this.soc = soc;
    }

    public ConnectionWrapper(SocketChannel chan) {
        std = false;
        this.chan = chan;
    }

    public boolean isOpen() {
        if (std) return !soc.isClosed();
        else return chan.isOpen();
    }

    public void close() throws IOException {

        if (std) soc.close();
        else chan.close();
    }

    public Socket getSocket() {return soc;}
    public SocketChannel getChannel() {return chan;}

    public void setTimeout(int timeout) {
        try {
            if (std) soc.setSoTimeout(timeout);
            else chan.socket().setSoTimeout(timeout);
        } catch (IOException e) { }
    }
}