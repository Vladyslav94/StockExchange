package startApp;


import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;
import java.sql.SQLException;


@SpringBootApplication
public class StartStockApp implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(StartStockApp.class, args);
    }



    @Autowired
    private GetDataFromWeb getDataFromWeb;


    public void run(String... args) throws Exception {
//        getDataFromWeb.connectToDB();
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                try {
                    getDataFromWeb.getStockThatEnabled();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread1 = new Thread(runnable1);

        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                try {
                    getDataFromWeb.getDataAbout();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread2 = new Thread(runnable2);

        thread1.start();
        thread2.start();


    }


}
