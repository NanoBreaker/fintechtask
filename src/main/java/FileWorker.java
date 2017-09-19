import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileWorker {

    //Запись данных в файл
    public static void writeData(String fileName, String text) {
        File file = new File(fileName);

        try {
            if(!file.exists()){ //проверяем, что если файл не существует то создаем его
                file.createNewFile();
            }

            PrintWriter out = new PrintWriter(file.getAbsoluteFile()); //PrintWriter обеспечит возможности записи в файл

            try {
                out.print(text); //Записываем текст у файл
            } finally {
                out.close();  //После чего мы должны закрыть файл, иначе файл не запишется
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Получение данных из файла в виде коллекции строк
    public static List<String> readData(String fileName) throws FileNotFoundException {

        List<String> listOfJsonStrings = new ArrayList<String>();
        File file = new File(fileName);
        StringBuilder sb = new StringBuilder();

        dataExists(fileName);

        try {
            BufferedReader in = new BufferedReader(new FileReader( file.getAbsoluteFile()));  //Объект для чтения файла в буфер
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    listOfJsonStrings.add(s);
                    sb.append(s);
                    sb.append("\n");
                }
            } finally {
                in.close();
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        //Возвращаем полученный текст с файла
        return listOfJsonStrings;
    }

    //Проверяем существует ли файл
    public static boolean dataExists(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        if (!file.exists()){
            return false;
        }else {
            return true;
        }
    }

    //Получение последней даты модификации файла, для определения его актуальности
    public static Date lastModifiedDate(String fileName){
        File file = new File(fileName);
        return new Date(file.lastModified());
    }

}
