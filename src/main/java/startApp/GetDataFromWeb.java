package startApp;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class GetDataFromWeb extends Thread {
    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock_exchange?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "dbadmin", "Theaternimda1");
    Statement statement = connection.createStatement();
    static LinkedBlockingQueue<String> storageForEnabledStock =  new LinkedBlockingQueue<String>();

    public GetDataFromWeb() throws SQLException {
    }

    public void connectToDB() throws SQLException {
        statement.execute("create table stock_quote\n" +
                "(\n" +
                "\tsymbol varchar(20) null,\n" +
                "\tcompanyName varchar(255) null,\n" +
                "\tcalculationPrice varchar(20) null,\n" +
                "\topen int null,\n" +
                "\topenTime int null,\n" +
                "\tclose int null,\n" +
                "\tcloseTime int null,\n" +
                "\thigh float null,\n" +
                "\tlow float null,\n" +
                "\tlatestPrice float null,\n" +
                "\tlatestSource varchar(255) null,\n" +
                "\tlatestTime varchar(255) null,\n" +
                "\tlatestUpdate int null,\n" +
                "\tlatestVolume int null,\n" +
                "\tvolume int null,\n" +
                "\tiexRealtimePrice float null,\n" +
                "\tiexLastUpdate int null,\n" +
                "\tdelayedPrice float null,\n" +
                "\tdelayedPriceTime int null,\n" +
                "\toddLotDelayedPrice float null,\n" +
                "\toddLotDelayedPriceTime int null,\n" +
                "\textendedPrice float null,\n" +
                "\textendedChange float null,\n" +
                "\textendedChangePercent float null,\n" +
                "\textendedPriceTime int null,\n" +
                "\tpreviousClose float null,\n" +
                "\tpreviousVolume int null,\n" +
                "\t`change` float null,\n" +
                "\tchangePercent float null,\n" +
                "\tiexMarketPercent float null,\n" +
                "\tiexVolume int null,\n" +
                "\tavgTotalVolume int null,\n" +
                "\tiexBidPrice float null,\n" +
                "\tiexBidSize int null,\n" +
                "\tiexAskPrice float null,\n" +
                "\tiexAskSize int null,\n" +
                "\tmarketCap int null,\n" +
                "\tweek52high float null,\n" +
                "\tweek52Low float null,\n" +
                "\tytdChange float null,\n" +
                "\tpeRatio float null,\n" +
                "\tlastTradeTime int null,\n" +
                "\tisUsMarketOpen varchar(20) null\n" +
                ");\n" +
                "\n");
    }

    public void getStockThatEnabled() throws IOException, JSONException, InterruptedException {
        //getting json data about stock from remote server, parsing and putting to shared queue
        URL url = new URL("https://sandbox.iexapis.com/stable/ref-data/symbols?token=Tpk_ee567917a6b640bb8602834c9d30e571");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();

        int response = httpURLConnection.getResponseCode();
        StringBuilder inline = new StringBuilder();

        if (response != 200)
            throw new RuntimeException("Error while parsing" + response);
        else {
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());

            }
            scanner.close();
        }

        JSONArray jsonArray = new JSONArray(inline.toString());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("symbol");
            boolean isTrue = jsonObject.getBoolean("isEnabled");
            if (isTrue) {
                storageForEnabledStock.offer(name);
            }
        }

    }

    public void getDataAbout() throws InterruptedException, IOException {
        while (true) {
            String stockToGet = storageForEnabledStock.take();
            String linkForTakingDataAboutStock =
                    String.format("https://sandbox.iexapis.com/stable/stock/%s/quote?token=Tpk_ee567917a6b640bb8602834c9d30e571", stockToGet);
            URL url = new URL(linkForTakingDataAboutStock);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            int response = httpURLConnection.getResponseCode();
            StringBuilder inline = new StringBuilder();

            if (response != 200)
                throw new RuntimeException("Error while parsing" + response);
            else {
                Scanner scanner = new Scanner(url.openStream());
                while (scanner.hasNext()) {
                    inline.append(scanner.nextLine());

                }
                scanner.close();
            }

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(inline.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String infoAboutStock = null;
            try {
                assert jsonObject != null;
                infoAboutStock = jsonObject.getString("companyName");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String s = String.format("insert into stock_quote (companyName) values ('%s')", infoAboutStock);
            try {
                statement.executeUpdate(s);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println(infoAboutStock);

        }
    }


}
