package ru.snek.Connectivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerAccepterWrapper {
    private ServerSocket serverSocket;
    private ServerSocketChannel serverChannel;
    private boolean std;

    public ServerAccepterWrapper(boolean std, int port) throws IOException{
        this.std = std;
        if(std) serverSocket = new ServerSocket(port);
        else {
            SocketAddress a = new InetSocketAddress(port);
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(a);
        }
    }

    public ConnectionWrapper accept() throws IOException{
        if(std) {
            Socket soc = serverSocket.accept();
            return new ConnectionWrapper(soc);
        } else {
            SocketChannel chan = serverChannel.accept();
            return new ConnectionWrapper(chan);
        }
    }

    public boolean isOpen() {
        if(std) return !serverSocket.isClosed();
        else return serverChannel.isOpen();
    }

    public void close() throws IOException {
        if(std) serverSocket.close();
        else serverChannel.close();;
    }
}