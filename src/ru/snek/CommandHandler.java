package ru.snek;

import java.util.ArrayList;

import static ru.snek.Logger.log;

public class CommandHandler {

    private MapWrapper wrapper;

    public CommandHandler(MapWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public Message handleCommand(Message command) {
        if(command == null) log("null");
        String com = command.getCommand();
        switch (com) {
            case "insert":
                return insert(command.getStringExtraData(),
                        (Malefactor) command.getData());
            case "show":
                return show();
            case "clear":
                return clear();
            case "save":
                return save();
            case "info":
                return info();
            case "remove":
                return remove(((String) command.getData()));
            case "remove_greater_key":
                return removeGreaterKey((String) command.getData());
            case "help":
                if (command.getData() != null) return help(true);
                else return help(false);
            case "test":
                return test();
            case "import":
                return _import((String) command.getData());
            case "load":
                return load();
            default:
        }
        return null;
    }

    private Message help(boolean insert) {
        String data;
        if(!insert)
            data =  "Список комманд:\n" +
                    "help: список всех комманд\n" +
                    "help insert: информация про команду insert\n" +
                    "insert \"String key\" {element}: добавить новый элемент с заданным ключом\n" +
                    "import path: импорт из файла со стороны клиента\n" +
                    "show: вывести все элементы коллекции в строковом представлении\n" +
                    "clear: очистить коллекцию\n" +
                    "save: сохранить коллекцию в файл\n" +
                    "load: загрузить коллекцию из файла на сервере\n" +
                    "info: вывести информацию о коллекции (тип, количество элементов)\n" +
                    "remove \"String key\": удалить элемент из коллекции по его ключу\n" +
                    "remove_greater_key \"String key\": удалить из коллекции все элементы, ключ которых превышает заданный\n" +
                    "quit, exit: завершение";
        else data = "insert \"String key\" {element}: добавить новый элемент с заданным ключом.\n" +
                "Элемент вводить в формате json. При отсутсвии значения того или иного поля \nбудет использовано значение по умолчанию или случайно сгенерированные данные\n"+
                "Далее приведены значения полей по умолчанию, если их не прописывать:\n"+
                "name : Безымянный\n" +
                "age : случайно\n"+
                "x : случайно\n" +
                "y : случайно\n" +
                "birthDate : текущие дата и время\n"+
                "condition : AWAKEN\n" +
                "canSleep : false\n" +
                "abToLift : HEAVY\n" +
                "pocket : Пусто\n"+
                "Для заполнения списка (pocket) элементы вводить в фигурных скобках ({, }) через запятую.\n" +
                "Возможные элементы списков: Knife, Box, Lamp. Тип элемента списка указывается в поле \"ключ\", свойства в поле \"значение\".\n"+
                "Для указания свойств Lamp использовать массив (квадратные скобки), куда занести пару булевых значений.";
        return new Message<>("help", data);
    }

    public Message insert(String key, Malefactor element) {
        String messageString;
        try {
            messageString = MapWrapper.addElement(key, element);
        } catch (Exception e) {
            messageString = "Элемент не добавлен:\n" + e.getMessage();
        }
        return new Message<>("insert", messageString);
    }

    public Message show() {
        Message message = new Message<>("show", wrapper.show());
        return message;
    }

    public Message clear() {
        Message message = new Message<>("clear", wrapper.clear());
        return message;
    }

    public Message save() {
        Message message = new Message<>("save", wrapper.save());
        return message;
    }

    public Message info() {
        Message message = new Message<>("info", wrapper.info());
        return message;
    }

    public Message remove(String key) {
        key = key.substring(1,key.length()-1);
        String data = wrapper.removeByKey(key);
        return new Message<>("remove", data);
    }

    public Message removeGreaterKey(String key) {
        key = key.substring(1,key.length()-1);
        Message message = new Message<>("removeGreaterKey", wrapper.removeGreaterKey(key));
        return message;
    }

	private Message  test() {
        Message ok = new Message<>("test", "ok!");
        return ok;
    }

    private Message _import(String fileStr) {
        String data = null;
        //TreeMap<String, Malefactor> map = new TreeMap<>();
        boolean correct = false;
        int counter = 0;
        ArrayList<String> lines = new ArrayList<>();
        for(String str : fileStr.split("\n")) {
            lines.add(str);
        }
            Pair<Boolean, Exception> result = MapWrapper.addElements(lines);
        if(result.getSecond() == null) {
            //wrapper.getMap().putAll(map);
            data = "Данные импортированы.";
        } else {
            data = "Произошла ошибка: \n" +
                    result.getSecond().getMessage() +"\n";
            if(result.getFirst()) {
                data += "Часть данных импортирована.";
            } else data += "Данные не импортированы.";
        }
        return new Message<>("import", data);
    }

    private Message load() {
        Message message = new Message<>("load", wrapper.load());
        return message;
    }
}
