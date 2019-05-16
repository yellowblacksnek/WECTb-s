package marat;

import java.io.File;
import java.io.IOException;

import static marat.FileInteractor.*;
import static marat.Logger.*;
import static marat.Utils.getConsoleInput;

public class Main {
    public static Type type;
    public static Realisation real;
    public static MapValuesComparator.Sorting sorting;


    public static void main(String []args)  {
        if(args.length != 2) {
            errprintln("Необходимо указать файл и порт.");
            System.exit(1);
        }
        int port = Integer.valueOf(args[1]);
        if(port < 0 || port > 65535) {
            errprintln("Неправильный порт.");
            System.exit(1);
        }
        String config = null;
        try {
            config = getFileString(openFile("serverConfig"));
            config = config.replaceAll("\n", "");
            config = config.replaceAll("\\s+", " ");
        }
        catch (Exception e) {
            errprintln(e.getMessage());
            System.exit(1);
        }
        String collectionFile = args[0];

        type = getType(config);
        real = getRealisation(config);
        sorting = getSorting(config);
        Server server;
        if(type == Type.TCP) server = new TCPServer(collectionFile, port);
        else server = new UDPServer(collectionFile, port);
        new Thread(() -> {
            while(server.isAlive()) {
                String input = getConsoleInput();
                if (input.trim().equals("log")) printLogs();
                else if (input.trim().equals("exit") || input.trim().equals("quit")) server.close();
            }
        }).start();
        server.loop();
    }

    public enum Type { TCP, UDP }
    public enum Realisation {STD, CHANNEL }

    public static Type getType(String file) {
        return Type.valueOf(file.split(" ")[0]);
    }

    public static Realisation getRealisation(String file) {
        return Realisation.valueOf(file.split(" ")[1]);
    }

    public static MapValuesComparator.Sorting getSorting(String file) {
        return MapValuesComparator.Sorting.valueOf(file.split(" ")[2]);
    }
}

