package startApp;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;


@SpringBootApplication
public class StartStockApp implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(StartStockApp.class, args);
    }

    @Autowired
    private GetDataFromWeb getDataFromWeb;


    public void run(String... args) throws SQLException {
        getDataFromWeb.connectToDB();
        Runnable runnable1 = () -> {
            try {
                getDataFromWeb.getStockThatEnabled();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        };
        Thread thread1 = new Thread(runnable1);

        Runnable runnable2 = () -> {
            try {
                getDataFromWeb.getDataAbout();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };
        Thread thread2 = new Thread(runnable2);

        Runnable runnable3 = new Runnable() {
            @Override
            public void run() {
                ResultSet resultSet = null;
                while (true) {
                    try {
                        resultSet = getDataFromWeb.statement.executeQuery("select companyName from stock_exchange.stock_quote order by changePercent DESC limit 5;");
                        while (resultSet.next()) {
                            String companyName = resultSet.getString("companyName");
                            System.out.println(companyName);
                        }
                        System.out.println();

                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        Thread thread3 = new Thread(runnable3);

        thread1.start();
        thread2.start();
        thread3.start();


    }


}
