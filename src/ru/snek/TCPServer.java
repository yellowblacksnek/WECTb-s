package ru.snek;

import ru.snek.Connectivity.*;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Vector;

import static ru.snek.Logger.*;
import static ru.snek.Utils.*;

public class TCPServer extends Server{
    private ServerAccepterWrapper saw;
    private Vector<ConnectionWrapper> connections;

    public TCPServer(String file, int port) throws IOException{
        super(file);
        connections = new Vector<>();
        saw = new ServerAccepterWrapper(std, port);
    }

    public void loop() {
        try {
            while(saw.isOpen() && alive) {
                ConnectionWrapper con = saw.accept();
                connections.add(con);
                println("Новый клиент. (Всего: "+ connections.size()+ ")");
                new Thread(() -> {
                    try {
                        while (con.isOpen()) {
                            if(!alive) {
                                con.close();
                                return;
                            }
                            Message command = receiveCommand(con);
                            if(command.getCommand().equals("quit")) {
                                con.close();
                                break;
                            }
                            Message response = com.handleCommand(command);
                            sendResponse(response, con);
                        }
                        connections.remove(con);
                        println("Клиент отключился.");
                    } catch(AsynchronousCloseException e) {
                        if(alive) handleException(e);
                    } catch (IOException e) {
                        if(alive) println("Клиент отключился. (Выброшено IO-исключение)");
                        handleException(e);
                    } finally {
                        try {
                            if (con.isOpen()) con.close();
                        } catch (Exception e) {}
                        if(connections.contains(con)) connections.remove(con);
                    }
                }).start();
            }
        } catch(AsynchronousCloseException e) {
            if(alive) handleException(e);
        } catch(IOException e) {
            handleException(e);
        }
    }

    private Message receiveCommand(ConnectionWrapper con) throws IOException {
        con.setTimeout(0);
        ByteBuffer buf = ByteBuffer.allocate(2048);
        int size = 0;
        int total = 0;
        do {
            int read = std ?
                    con.getSocket().getInputStream().
                            read(buf.array(), total, buf.array().length - total)
                    : con.getChannel().read(buf);
            if (read == -1) throw new EOFException();
            con.setTimeout(1000);
            if (size == 0) {
                size = getSizeFromArr(buf.array());
                byte[] cut = Arrays.copyOf(buf.array(), read);
                byte[] extra = containsMore(cut);
                buf = ByteBuffer.allocate(size);
                if (extra != null) {
                    total += extra.length;
                    buf.put(extra);
                }
            } else total += read;
        } while (total < size);
        Message command = (Message) objectFromByteArray(buf.array());
        return command;
    }

    private void sendResponse(Message mes, ConnectionWrapper con) throws IOException {
            Socket soc = null;
            SocketChannel chan = null;
            if (std) soc = con.getSocket();
            else chan = con.getChannel();
            byte[] responseArr = objectAsByteArray(mes);
            byte[] sizeMsgArr = getSizeArr(responseArr);
            if (std) {
                soc.getOutputStream().write(sizeMsgArr);
                soc.getOutputStream().write(responseArr);
            } else {
                chan.write(ByteBuffer.wrap(sizeMsgArr));
                chan.write(ByteBuffer.wrap(responseArr));
            }
    }

    @Override
    public void close() {
        super.close();
        try {
            saw.close();
            for(ConnectionWrapper c : connections) c.close();
        } catch (Exception e) { }
    }
}