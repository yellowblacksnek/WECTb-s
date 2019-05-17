package ru.snek;

import ru.snek.Collection.MapValuesComparator;

import static ru.snek.Utils.FileInteractor.*;
import static ru.snek.Utils.Logger.*;
import static ru.snek.Utils.Utils.getConsoleInput;

public class Main {
    public static Type type;
    public static Realisation real;
    public static MapValuesComparator.Sorting sorting;
    private static Server server;

    public enum Type { TCP, UDP }
    public enum Realisation {STD, CHANNEL }


    public static void main(String[] args)  {
        checkInitialArgs(args);
        configure();
        String collectionFile = args[0];
        int port = Integer.valueOf(args[1]);
        try {
            if (type == Type.TCP) server = new TCPServer(collectionFile, port);
            else server = new UDPServer(collectionFile, port);
        } catch(Exception e) {
            errprintln("Не удалось запустить сервер.\n" + e.getMessage());
            System.exit(1);
        }
        createConsoleListener();
        server.loop();
    }

    private static void checkInitialArgs(String[] args) {
        if(args.length != 2) {
            errprintln("Необходимо указать файл и порт.");
            System.exit(1);
        }
        int port = Integer.valueOf(args[1]);
        if(port < 0 || port > 65535) {
            errprintln("Неправильный порт.");
            System.exit(1);
        }
    }

    private static void configure() {
        String config = null;
        try {
            config = getFileString(openFile("serverConfig"));
        }
        catch (Exception e) {
            errprintln(e.getMessage());
            System.exit(1);
        }
        config = config.replaceAll("\n", "");
        config = config.replaceAll("\\s+", " ");
        try {
            type = Type.valueOf(config.split(" ")[0]);
            real = Realisation.valueOf(config.split(" ")[1]);
            sorting = MapValuesComparator.Sorting.valueOf(config.split(" ")[2]);
        } catch (Exception e) {
            errprintln("Неверный формат конфиг-файла.\n" + e.getMessage());
            System.exit(1);
        }
    }

    private static void createConsoleListener() {
        Thread consoleListener =
        new Thread(() -> {
            while(server.isAlive()) {
                String input = getConsoleInput();
                if (input.trim().equals("log")) printLogs();
                else if (input.trim().equals("exit") || input.trim().equals("quit")) server.close();
            }
        });
        consoleListener.start();
    }
}

