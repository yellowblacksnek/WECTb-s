package ru.snek;

public abstract class Server {
    protected CommandHandler com;
    protected boolean std;
    protected boolean alive;

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
