package ru.snek.Utils;

import java.io.*;
import java.util.ArrayList;

import static ru.snek.Utils.Logger.*;

public class FileInteractor {

    public static File openFile(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) throw new Exception("Файл не существует: " + file.getPath());
        if (!(file.canRead() && file.canWrite())) throw new Exception("Нет нужных прав для работы с файлом!");
        if(file.length() > 5 * 1024 * 1024) throw new Exception("Файл слишком большой! (>5МБ)");
        return file;
    }

    public static String getFileString(File file) {
        StringBuilder strb = new StringBuilder();
        try (BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
             BufferedReader reader = new BufferedReader(new InputStreamReader(buf, "UTF-8"))) {
            int c;
            while ((c = reader.read()) > 0) {
                strb.append((char)c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strb.toString();
    }

    public static ArrayList<String> getFileLines(File file) {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
             BufferedReader reader = new BufferedReader(new InputStreamReader(buf, "UTF-8"))) {
            //int c;
            while (true) {
                String current = reader.readLine();
                if(current == null) break;
                lines.add(current);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static boolean toFile(ArrayList<String> arr, File file) {
        if (!file.exists()) println("Файл куда-то пропал, но ничего страшного, сейчас будет новый.");
        if (!file.canWrite()) {
            errprintln("Нет прав для записи в файл: " + file.getPath());
            return false;
        }
        try (BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(file))) {
            for(String s : arr) {
                buf.write((s + '\n').getBytes());
                buf.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

}
