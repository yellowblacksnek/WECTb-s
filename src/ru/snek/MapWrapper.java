package ru.snek;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

import static ru.snek.Logger.*;
import static ru.snek.FileInteractor.*;

public class MapWrapper {
    private static ConcurrentSkipListMap<String, Malefactor> map = new ConcurrentSkipListMap<>();
    private static final long maxMapSize = 50000;
    private File file;

    public MapWrapper(String path) {
        try { file = openFile(path); }
        catch(Exception e) {
            errprintln(e.getMessage());
            System.exit(1);
        }
        print("Выполняется загрузка коллекции из файла: " + path +".");
        Pair<Boolean, Exception> result = addElements(getFileLines(file));
        String resultString;
        if(result.getSecond() == null) {
            if(result.getFirst()) resultString = "Коллекция загружена из файла: ";
            else resultString = "Ой ну хз: ";
        } else {
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
            throw new Exception("Пустая строка.");
        }
        Malefactor mf = elementFromString(str);
        String key = str.substring(1, str.indexOf(' ') - 2);
        if(map.size() >= maxMapSize) {
            throw new Exception("Коллекция достигла максимального размера: " + maxMapSize + ".");
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
                addElementNoCheck(each);
                ++counter;
            }
        } catch (Exception e) {
            boolean someAdded = counter > 0;
            return new Pair<>(someAdded, e);
        }
        return new Pair<>(true, null);
    }

    private static Malefactor elementFromString(String str) throws Exception {
        String[] arr = str.split(";");
        if (arr.length != 10) {
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
        mf.setPocketContent(parseArr(arr[9], mf));
        return mf;
    }

    private static String elementToString(String key) {
        Malefactor mf = map.get(key);
        String arr[] = new String[10];
        arr[0] = "\""+key+"\"";
        arr[1] = '\"'+mf.getName()+'\"';
        arr[2] = Integer.toString(mf.getAge());
        arr[3] = Integer.toString(mf.getX());
        arr[4] = Integer.toString(mf.getY());
        arr[5] = Long.toString(mf.getBirthDate().getTime());
        arr[6] = mf.getCondition().name();
        arr[7] = mf.getAbilityToLift().name();
        arr[8] = '\"'+Boolean.toString(mf.isCanSleep())+'\"';
        arr[9] = parseThingable(mf.getPocketContent());
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < arr.length; ++i) {
            str.append(arr[i]);
            if(i < arr.length-1) str.append("; ");
        }
        return str.toString();
    }

    private static String parseThingable(ArrayList<Thingable> arr) {
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        for(Thingable t : arr) {
            String[] locArr = t.toString().split("\\{");
            builder.append(locArr[0]);
            if(locArr.length > 1) {
                builder.append("{");
                locArr = locArr[1].split(",");
                for(int i = 0; i < locArr.length; ++i) {
                    builder.append(locArr[i].split("=")[1]);
                    if(i < locArr.length-1) builder.append(", ");
                }
            }
            if(arr.indexOf(t) < arr.size()-1) builder.append(", ");
        }
        builder.append("\"");
        return builder.toString();
    }

    private static ArrayList<Thingable> parseArr(String str, Malefactor mf) throws IllegalArgumentException {
        ArrayList<Thingable> result = new ArrayList<>();
        str = str.substring(1, str.length()-1);
        if(str.equals("")) return  result;
        ArrayList<Integer> commas = Utils.getExtCommas(str);
        ArrayList<String> strArr = Utils.splitStrByIndex(str, commas);
        if(strArr.size() == 0) strArr.add(str);

        for(String s : strArr) {
            String[] sArr = s.split("\\{");
            switch (sArr[0]) {
                case "Knife" :
                    Malefactor.Knife knife =  mf.new Knife();
                    result.add(knife);
                    break;
                case "Lamp" :
                    Malefactor.Lamp lamp = mf.new Lamp();
                    sArr[1] = sArr[1].substring(0, sArr[1].length()-1);
                    String[] fields = sArr[1].split(",");
                    if((fields[0].trim().equals("true") || fields[0].trim().equals("false"))
                    && (fields[1].trim().equals("true") || fields[1].trim().equals("false"))) {
                        lamp.setHidden(Boolean.valueOf(fields[0].trim()));
                        lamp.setCond(Boolean.valueOf(fields[1].trim()));
                        result.add(lamp);
                    }
                    else throw new IllegalArgumentException();
                    break;
                case "Box" :
                    sArr[1] = sArr[1].substring(0, sArr[1].length()-1);
                    Malefactor.Box box = new Malefactor.Box(Malefactor.Box.Weight.valueOf(sArr[1].trim()));
                    result.add(box);
                    break;
            }
        }
        return result;
    }

    public static ConcurrentSkipListMap<String, Malefactor> getMap() {return map;}

    public HashMap<String, Malefactor> show() {
        //ConcurrentSkipListMap<String, Malefactor> cloned = map.clone();
        MapValuesComparator comp = new MapValuesComparator(map, Main.sorting);
        ConcurrentSkipListMap<String, Malefactor> sorted = new ConcurrentSkipListMap<>(comp);
        sorted.putAll(map);
        new LinkedHashMap<String, Malefactor>(map);
        return new LinkedHashMap<String, Malefactor>(map);
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
        if (result.getSecond() == null) message = "Коллекция загружена из файла.";
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
