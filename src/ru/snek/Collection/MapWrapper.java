package ru.snek.Collection;

import ru.snek.Main;
import ru.snek.Utils.Pair;
import ru.snek.Utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;

import static ru.snek.Utils.Logger.*;
import static ru.snek.Utils.FileInteractor.*;

public class MapWrapper {
    private static ConcurrentSkipListMap<String, Malefactor> map = new ConcurrentSkipListMap<>();;
    private static final long maxMapSize = 50000;
    private File file;

    private static class EmptyFileException extends Exception {}
    private static class CollectionOverflowException extends Exception {
        @Override
        public String getMessage() {
            return "Коллекция достигла максимального размера: " + maxMapSize +".";
        }
    }

    public MapWrapper(String path) {
        try { file = openFile(path); }
        catch(Exception e) {
            errprintln(e.getMessage());
            System.exit(1);
        }
        boolean failed = false;
        print("Выполняется загрузка коллекции из файла: " + path +".");
        Pair<Boolean, Exception> result = addElements(getFileLines(file));
        String resultString;
        if(result.getSecond() == null) {
            if(result.getFirst()) resultString = "Коллекция загружена из файла: ";
            else resultString = "Файл пуст, коллекция пуста. Файл: ";
        } else if(result.getSecond() instanceof CollectionOverflowException) {
            resultString = "Коллекция загружена, но достигла максимального размера: " + maxMapSize + ".\n" +
            "Файл: ";
        } else {
            failed = true;
            resultString = "При загрузке коллекции произошла ошибка: \n" +
                        result.getSecond().getMessage() +"\nФайл: " ;
        }
        println("\033[2K\r" + resultString + path +".");
        if(result.getSecond() != null && result.getFirst()) println("Часть элементов была загружена.");
    }

    public static String addElement(String key, Malefactor element) throws Exception {
        if(element == null) {
            throw new Exception("Элемент почему-то null.");
        }
        String message = "Элемент добавлен!";
        if (map.containsKey(key)) message = "Этот ключ уже содержится. Значение переписано.";
        else if(map.size() >= maxMapSize) {
            throw new Exception("Коллекция достигла максимального размера: " + maxMapSize + ".");
        }
        map.put(key, element);
        return message;
    }

    private static void addElementNoCheck (String str) throws Exception {
        if (str.equals("")) {
            throw new EmptyFileException();
        }
        Malefactor mf = elementFromString(str);
        String key = str.substring(1, str.indexOf(' ') - 2);
        if(map.size() >= maxMapSize) {
            throw new CollectionOverflowException();
        }
        if(mf == null) {
            throw new Exception("Элемент почему-то null.");
        }
        map.put(key, mf);
    }

    public synchronized static Pair<Boolean, Exception> addElements(ArrayList<String> list) {
        int counter = 0;
        try {
            for (String each : list) {
                try {
                    addElementNoCheck(each);
                    ++counter;
                } catch (EmptyFileException e) {}
            }
        } catch (Exception e) {
            return new Pair<>(counter > 0, e);
        }
        return new Pair<>(counter > 0, null);
    }

    private static Malefactor elementFromString(String str) throws Exception {
        String[] arr = str.split(";");
        if (arr.length != 9) {
            throw new Exception("Неверный формат данных!");
        }
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = arr[i].trim();
        }
        Malefactor mf = new Malefactor(arr[1].substring(1, arr[1].length() - 1));
        mf.setAge(Integer.valueOf(arr[2]));
        mf.setX(Integer.valueOf(arr[3]));
        mf.setY(Integer.valueOf(arr[4]));
        mf.setBirthDate(new Date(Long.valueOf(arr[5])));
        mf.setCondition(Malefactor.Condition.valueOf(arr[6]));
        mf.setAbilityToLift(Malefactor.Box.Weight.valueOf(arr[7]));
        mf.setCanSleep(Boolean.valueOf(arr[8]));
        return mf;
    }

    private static String elementToString(String key) {
        Malefactor mf = map.get(key);
        String arr[] = new String[9];
        arr[0] = "\""+key+"\"";
        arr[1] = '\"'+mf.getName()+'\"';
        arr[2] = Integer.toString(mf.getAge());
        arr[3] = Integer.toString(mf.getX());
        arr[4] = Integer.toString(mf.getY());
        arr[5] = Long.toString(mf.getBirthDate().getTime());
        arr[6] = mf.getCondition().name();
        arr[7] = mf.getAbilityToLift().name();
        arr[8] = '\"'+Boolean.toString(mf.isCanSleep())+'\"';
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < arr.length; ++i) {
            str.append(arr[i]);
            if(i < arr.length-1) str.append("; ");
        }
        return str.toString();
    }

    public static ConcurrentSkipListMap<String, Malefactor> getMap() {return map;}

    public ConcurrentSkipListMap<String, Malefactor> show() {
        MapValuesComparator comp = new MapValuesComparator(map, Main.sorting);
        ConcurrentSkipListMap<String, Malefactor> sorted = new ConcurrentSkipListMap<>(comp);
        sorted.putAll(map);
        return sorted;
    }

    public synchronized static String clear() {
        map.clear();
        return "Коллекция очищена.";
    }

    public synchronized String save() {
        if (file == null) return "";
        ArrayList<String> arr = new ArrayList<>();
        map.keySet().stream().map(MapWrapper::elementToString).forEach(arr::add);
        boolean success = toFile(arr, file);
        if (success) return "Данные сохранены.";
        else return "Данные не сохранены!";
    }

    public synchronized String load() {
        String message;
        ConcurrentSkipListMap<String, Malefactor> copy = map.clone();
        map.clear();
        ArrayList<String> lines = getFileLines(file);
        Pair<Boolean, Exception> result = addElements(lines);
        if (result.getSecond() == null) {
            message = "Коллекция загружена из файла.";
            if(!result.getFirst()) message += " Файл был пуст.";
        }
        else {
            message = "Произошла ошибка: \n" + result.getSecond().getMessage() + "\n" +
                    "Коллекция не изменилась.";
        }
        return message;
    }

    public static String info() {
        String message = "Тип коллекции: " + map.getClass().toString().substring(16) + ".\n" +
        "Колличество элементов в коллекции: " + map.size() + ".";
		return message;
    }

    public synchronized static String removeByKey(String key){
        if(map.remove(key) == null) return "Нет элемента с таким ключом.";
        return "Элемент удалён.";
    }

    public synchronized static String removeGreaterKey(String keyToCompare) {
        ArrayList<String> keysToRemove = new ArrayList<>();
        map.keySet().stream().filter(i -> i.compareTo(keyToCompare) > 0).forEach(keysToRemove::add);
        keysToRemove.stream().forEach(map::remove);
        if(keysToRemove.size() == 0) return "Ни один элемент не был удален.";
        //StringBuilder strb = new StringBuilder();
        //keysToRemove.stream().forEach( i -> strb.append(i + ", "));
        //return "Удалены элементы: " + strb.toString().substring(0, strb.length()-2) + ".";
        return "Какие-то элементы удалены.";
    }
}
