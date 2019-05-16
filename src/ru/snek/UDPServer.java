package ru.snek;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static ru.snek.Logger.*;
import static ru.snek.Utils.*;

public class UDPServer extends Server{
    private DatagramSocket socket;
    private DatagramChannel channel;
    private static final int maxBufferSize = 65507;

    public UDPServer(String file, int port){
        super(file, port);
        SocketAddress a = new InetSocketAddress(port);
        try {
            if (std) {
                socket = new DatagramSocket(a);
            } else {
                channel = DatagramChannel.open();
                channel.bind(a);
            }
        } catch(IOException e) {
            e.printStackTrace();
            handleException(e);
            System.exit(1);
        }
    }

    public void loop(){
        try {
            ByteBuffer buf = ByteBuffer.allocate(maxBufferSize);
            if (std) {
                while (!socket.isClosed()) {
                    DatagramPacket i = new DatagramPacket(buf.array(), buf.array().length);
                    socket.receive(i);
                    new Thread(() -> {
                        handle(buf.array().clone(), new InetSocketAddress(i.getAddress(), i.getPort()));
                    }).start();
                }
            } else {
                while (channel.isOpen()) {
                    buf.clear();
                    SocketAddress client = channel.receive(buf);
                    new Thread(() -> {
                        handle(buf.array().clone(), client);
                    }).start();
                }
            }
        }catch (IOException e) {
            handleException(e);
        }
    }

    private void handle(byte[] data, SocketAddress client) {
        try {
            int bufferSize = 2048;
            Message command;
            command = (Message) objectFromByteArray(data);
            Message response = com.handleCommand(command);
            byte[] objAsArr = objectAsByteArray(response);
            int size = objAsArr.length;
            int amount = size <= maxBufferSize ? 1 : (size / maxBufferSize + 1);
            ByteBuffer bb = ByteBuffer.allocate(bufferSize);
            bb.put(getSizeArr(objAsArr));
            bb.rewind();
            if(std) {
                DatagramPacket o = new DatagramPacket(bb.array(), bb.array().length, client);
                socket.send(o);
            } else {
                channel.send(bb, client);
            }
            if(amount == 1) bb = ByteBuffer.allocate(objAsArr.length);
            else bb = ByteBuffer.allocate(maxBufferSize);
            for (int i = 0; i < amount; ++i) {
                bb.clear();
                int j;
                for (j = 0; j < (i < (amount - 1) ? maxBufferSize : size % maxBufferSize); ++j) {
                    bb.put(objAsArr[(i * maxBufferSize) + j]);
                }
                while (j < bb.array().length) {
                    bb.put((byte) 0);
                    ++j;
                }
                bb.rewind();
                if (std) {
                    DatagramPacket o = new DatagramPacket(bb.array(), bb.array().length, client);
                    socket.send(o);
                } else {
                    channel.send(bb, client);
                }
                if(i % 50 == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) { }
                }
            }
        } catch (IOException e) {
            handleException(e);
        }
    }

    public void close() {
        super.close();
        try {
            if (std) socket.close();
            else channel.close();
        } catch (Exception e) {}
    }

}


