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
//        getDataFromWeb.connectToDB();
        Runnable takingDataFromAPI = () -> {
            try {
                getDataFromWeb.getStockThatEnabled();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        };
        Thread thread1 = new Thread(takingDataFromAPI);

        Runnable puttingDataToDB = () -> {
            try {
                getDataFromWeb.getDataAbout();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };
        Thread thread2 = new Thread(puttingDataToDB);

        Runnable gettingDataFromDB = new Runnable() {
            @Override
            public void run() {
                //need this count to avoid printing new line before data appears on the screen
                int count = 0;
                //for printing title before each block of displayed data
                int count2 = 0;

                while (true) {
                    //The most recent 5 companies that have the greatest change percent in stock value
                    try (ResultSet resultSetForGreatestChangePercent = getDataFromWeb.statementForSelectingData.executeQuery("select companyName, changePercent from stock_exchange.stock_quote order by changePercent DESC limit 5;");) {
                        while (resultSetForGreatestChangePercent.next()) {
                            if (count2 == 0) {
                                System.out.println("The most recent 5 companies that have the greatest change percent in stock value:");
                            }
                            String companyName = resultSetForGreatestChangePercent.getString("companyName");
                            double changePercent = resultSetForGreatestChangePercent.getDouble("changePercent");
                            System.out.println(companyName + " - " + changePercent);
                            count++;
                            count2++;
                        }
                        count2 = 0;
                        if (count > 0) {
                            System.out.println();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    //The top 5 highest value stocks (in order – largest first, then order by company name)
                    try (ResultSet resultSetForHighestStockValue = getDataFromWeb.statementForSelectingData.executeQuery("select companyName, high from stock_exchange.stock_quote order by high DESC, companyName DESC limit 5;");) {
                        while (resultSetForHighestStockValue.next()) {
                            if (count2 == 0) {
                                System.out.println("The top 5 highest value stocks:");
                            }
                            String companyName = resultSetForHighestStockValue.getString("companyName");
                            double highestValue = resultSetForHighestStockValue.getDouble("high");
                            System.out.println(companyName + " - " + highestValue);
                            count++;
                            count2++;
                        }
                        count2 = 0;
                        if (count > 0) {
                            System.out.println();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        };
        Thread thread3 = new Thread(gettingDataFromDB);

        thread1.start();
        thread2.start();
        thread3.start();

    }

}
