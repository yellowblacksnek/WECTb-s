package ru.snek;

import ru.snek.Collection.MapWrapper;

public abstract class Server {
    protected CommandHandler com;
    protected boolean std;
    private boolean alive;

    protected Server(String file) {
        com = new CommandHandler(new MapWrapper(file));
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
