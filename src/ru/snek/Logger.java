package ru.snek;

import java.util.Stack;

public class Logger {
    public static void print(Object obj) { System.out.print(obj); }
    public static void println(Object obj) { System.out.println(obj); }
    public static void errprint(Object obj) { System.err.print(obj); }
    public static void errprintln(Object obj) { System.err.println(obj); }
    public static void log(Object obj) { System.out.println("LOG: " + obj); }

    private static class ExceptionInfo {
        private StackTraceElement[] stackTrace;
        private String message;

        public ExceptionInfo(StackTraceElement[] arr, String message) {
            stackTrace = arr;
            this.message = message;
        }

        public String toString() {
            String out = "";
            out += message + '\n';
            for(StackTraceElement el : stackTrace) out += el.toString() +'\n';
            out += "---------------";
            return out;
        }
    }

    private static Stack<ExceptionInfo> logs = new Stack<>();

    public static void addToLogs(StackTraceElement[] el, String message) {
        logs.push(new ExceptionInfo(el, message));
    }
    public static void printLogs() {
        if(logs.empty()) {
            errprintln("Пусто");
            return;
        }
        for(ExceptionInfo e : logs) errprintln(e);
        logs.clear();
    }

    public static void handleException(Exception exception) {
        try {
            addToLogs(exception.getStackTrace(), exception.getClass().getName());
            throw exception;
        }
        catch (ClassNotFoundException e) {
            errprintln("Ошибка при десериализации.\n"+e.getMessage());
            System.exit(1);
        }
        catch (Exception e) {
            //errprintln("Произошла ошибка: " + e.getMessage());
            //e.printStackTrace();
            //e.printStackTrace();
        }
    }
}
