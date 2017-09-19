import com.google.gson.*;
import com.sun.istack.internal.Nullable;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.*;

public class DataHandler {

    private String FILENAME; //Название файла
    private String SERVER_URL; //Ссылка на сервер
    private String FILE_DATA_STRING; //Огромная строка со всеми курсами

    private long dateDifference; //Разница во времени между последним изменением файла и текущим моментом

    private Gson gson; //джэйсон

    static ArrayList<ApiResponse> listOfApiRespones; //Лист с экземплярами ApiResponse
    //Лист со всеми валютами, нужен для получения данных с сервера и их кэширования
    static List<String> listOfCurrencies = Arrays.asList("RUB", "AUD", "GBP", "KRW", "SEK", "BGN", "HKD", "MXN", "SGD", "BRL", "HRK", "MYR", "THB", "CAD" +
            "", "HUF", "NOK", "TRY", "CHF", "IDR", "NZD", "USD", "CNY", "ILS", "PHP", "ZAR", "CZK", "INR", "PLN", "EUR", "DKK", "JPY", "RON");

    //Планарные классы
    public class ApiResponse {
        private String base;
        private RateObject rates;
    }

    public static class RateObject {
        private String name;
        private double rate;
        public RateObject(String name, double rate) {
            this.name = name;
            this.rate = rate;
        }
    }

    //Кастомный десериалайзер <3
    public static class RatesDeserializer implements JsonDeserializer<RateObject> {
        @Nullable
        public RateObject deserialize(JsonElement json, Type typeOfT,
                                      JsonDeserializationContext context) throws JsonParseException {
            RateObject rate = null;
            if (json.isJsonObject()) {
                Set<Map.Entry<String, JsonElement>> entries =
                        json.getAsJsonObject().entrySet();
                if (entries.size() > 0) {
                    Map.Entry<String, JsonElement> entry = entries.iterator().next();
                    rate = new RateObject(entry.getKey(), entry.getValue().getAsDouble());
                }
            }
            return rate;
        }
    }

    //Конструктор
    public DataHandler(String FILENAME, String SERVER_URL){
        this.FILENAME = FILENAME;
        this.SERVER_URL = SERVER_URL;
        listOfApiRespones = new ArrayList<ApiResponse>();
        gson = new GsonBuilder()
                .registerTypeAdapter(RateObject.class, new RatesDeserializer())
                .create();
    }

    //Получение информации с сервера
    public void getDataFromServer(){
        String fileDateString = "";
        for (String BASE : listOfCurrencies){
            for (String SUBBASE : listOfCurrencies){
                if(BASE != SUBBASE){ //Проверяем что-бы валюта не проверяла курс по отношению к самой себе
                    String jsonData = ServerWorker.getJSON("http://api.fixer.io/latest?base=" + BASE + "&symbols=" + SUBBASE); //Делаем запрос
                    if(jsonData != null){
                        fileDateString = fileDateString + jsonData; //Сохраняем Json строчку в огромную строку, которую в послествии будет легко записать в файл
                        listOfApiRespones.add(gson.fromJson(jsonData, ApiResponse.class)); //Парсим и сохраняем объект в лист
                    }
                }
            }
        }
        FILE_DATA_STRING = fileDateString;
        System.out.println("INFORAMITION WAS SUCCESSFULLY RECEIVED FROM THE SERVER!");
    }

    //Преобразование файла в лист ApiResponce
    public void getDataFromFile() throws FileNotFoundException {
        List<String> listOfJasonStrings = FileWorker.readData(FILENAME);
        for (String str : listOfJasonStrings) {
            listOfApiRespones.add(gson.fromJson(str, ApiResponse.class)); //Парсим строчки из файла проще говоря
        }
        System.out.println("DATA IS READY FOR USE");
    }

    //Записываем полученную с сервера информацию в файл, через огромный String
    public void writeDataToFile(){
        FileWorker.writeData(FILENAME, FILE_DATA_STRING);
        System.out.println("WRITING DATA TO THE LOCAL FILE");
    }

    //Проверка существует ли файла, что-бы знать по какому пути идти
    public boolean fileExists() throws FileNotFoundException {
        System.out.println("SEARCHING FOR THE LOCAL FILE WITH THE DATA!");
        if(FileWorker.dataExists(FILENAME)){ //Файл существует
            System.out.println("THE LOCAL FILE WAS FOUND!");
            return true;
        }else{ //Файл не существует
            System.out.println("THE LOCAL FILE WAS NOT FOUND!");
            return false;
        }
    }

    //Проверка устарел ли файл, если файл был последний раз изменён больше чем 24 часа назад, то обновляем его
    public boolean isFileOld(){
        Date fileLastModifiedDate; //время последнего изменения файла
        Date currentDate = java.util.Calendar.getInstance().getTime(); //текущее время
        fileLastModifiedDate = FileWorker.lastModifiedDate(FILENAME); //получаем время файла
        dateDifference = currentDate.getTime() - fileLastModifiedDate.getTime(); //высчитываем разнизу
        long seconds = dateDifference / 1000 % 60; //переводим разницу в секунды
        if (seconds > 86400) { //если секунд > 86400 то значит прошло больше дня и пора обновить файл, не указано когда обновляется сервер, поэтому использую этот вариант
            System.out.println("THE LOCAL FILE IS OUT OF DATE INFORMATION!");
            return true;
        }else{ //в противном случае файл остаётся актуальным
            System.out.println("THE LOCAL FILE IS UP TO DATE INFORMATION!");
            return false;
        }
    }

    //Проверка существует ли курс для полученного набора валют
    public boolean rateExists(String BASE, String SUBBASE){
        int i = 0;
        for(ApiResponse apiResponse : listOfApiRespones){
            if(apiResponse.base.equals(BASE) && apiResponse.rates.name.equals(SUBBASE)){
                i++;
            }
        }
        if(i == 0){
            return false;
        }else{
            return true;
        }
    }

    //Получение курса из локального хранилища
    public double getRate(String BASE, String SUBBASE){
        //System.out.println("Using offline method");
        for(ApiResponse apiResponse : listOfApiRespones){ //Проходимся по списку
            if(apiResponse.base.equals(BASE) && apiResponse.rates.name.equals(SUBBASE)){ //Находим курс для наших валют
                return  apiResponse.rates.rate;
            }
        }
        return 0;
    }
}
