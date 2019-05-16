package marat;

import java.io.*;

import static marat.Logger.*;

public abstract class Server {
    protected MapWrapper wrapper;
    protected CommandHandler com;
    protected int port;
    protected boolean std;
    protected boolean alive;

    protected Server(String file, int port) {
        wrapper = new MapWrapper(file);
        com = new CommandHandler(wrapper);
        this.port = port;
        std = Main.real == Main.Realisation.STD;
        alive = true;
        Runtime.getRuntime().addShutdownHook(new Thread(com::save));
    }

    abstract void loop();

    public void close() {
        alive = false;
    }
    public boolean isAlive() {return alive;}
}
