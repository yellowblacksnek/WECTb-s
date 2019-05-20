package ru.snek.Utils;

import java.util.Stack;

public class Logger {
    public static void print(Object obj) { System.out.print(obj); }
    public static void println(Object obj) { System.out.println(obj); }
    public static void errprint(Object obj) { System.err.print(obj); }
    public static void errprintln(Object obj) { System.err.println(obj); }
    public static void log(Object obj) { System.out.println("LOG: " + obj); }

    private static Stack<Exception> logs = new Stack<>();

    public static void addToLogs(Exception e) {
        logs.push(e);
    }
    public static void printLogs() {
        if(logs.empty()) {
            errprintln("Пусто");
            return;
        }
        for(Exception e : logs) {
            e.printStackTrace();
        }
        logs.clear();
    }

    public static void handleException(Exception exception) {
        try {
            addToLogs(exception);
            throw exception;
        } catch (Exception e) {
            //errprintln("Произошла ошибка: " + e.getMessage());
        }
    }
}
