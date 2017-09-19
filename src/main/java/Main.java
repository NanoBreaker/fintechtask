import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    final static String FILENAME = "exchange_rate_data.txt";
    final static String SERVER_URL = "http://api.fixer.io/";

    private static String BASE;
    private static String SUBBASE;
    private static boolean dataExists = true;

    public static void main(String[] args) throws IOException {

        Scanner systemInput = new Scanner(System.in);
        DataHandler dataHandler = new DataHandler(FILENAME, SERVER_URL); //Класс для управления данными

        if(dataHandler.fileExists()){ //Проверяем для начала, существует ли локальный файл
            if(dataHandler.isFileOld()){ //Если существует, то проверяем не устарел ли он, если устарел то обновляем
                if(ServerWorker.checkInternetConnection(SERVER_URL)) {
                    System.out.println("CONNECTION IS OK, GETTING DATA FROM THE SERVER!");
                    dataHandler.getDataFromServer(); //Закачиваем данные с сервера
                    dataHandler.writeDataToFile(); //Записываем данные в файл
                }else {
                    //нет интернета, предоставим пользователю устарелые данные\
                    System.out.println("NO CONNECTION TO THE SERVER! USING OLD DATA!");
                }
            }else{
                dataHandler.getDataFromFile(); //Файл актуален, берем данные из него
            }
        }else{
            if(ServerWorker.checkInternetConnection(SERVER_URL)){
                System.out.println("CONNECTION IS OK, GETTING DATA FROM THE SERVER!");
                dataHandler.getDataFromServer(); //Если файла нет, то сначала закачиваем данные с сервера
                dataHandler.writeDataToFile(); //А потом создаём файл, и записываем в него эти данные
            }else{
                dataExists = false; //Если нет файла и интренета то работать невозможно!
            }
        }

        if(dataExists)
        {
            //Выводим список доступных валютных курсов!
            System.out.print("LIST OF AVAILABLE RATES : ");
            for (String rate : DataHandler.listOfCurrencies) System.out.print(rate + " ");
            System.out.println();

            while(true){
                System.out.println("Enter from currency:");
                BASE = systemInput.nextLine(); //Какая валюта
                System.out.println("Enter to currency:");
                SUBBASE = systemInput.nextLine(); //В какую валюту
                if(dataHandler.rateExists(BASE, SUBBASE)){ //Проверяем сущесвует ли курс с данными валютами
                    System.out.println(BASE + " => " + SUBBASE + " : " + dataHandler.getRate(BASE, SUBBASE));
                }else{
                    System.out.println("RATE DOES NOT EXIST OR INPUT MISTAKE WAS MADE!");
                }
            }
        }else{
            System.out.println("NO LOCAL DATA, NO CONNECTION TO THE SERVER! APPLICATION CAN NOT RUN!");
            System.exit(0);
        }

    }

}
