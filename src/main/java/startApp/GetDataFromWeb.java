package startApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.propertyeditors.CurrencyEditor;
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
    private final Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock_exchange?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "dbadmin", "Theaternimda1");
    final Statement statement = connection.createStatement();
    Statement statementForSelectingData = connection.createStatement();
    private static final LinkedBlockingQueue<String> storageForEnabledStock = new LinkedBlockingQueue<>();

    public GetDataFromWeb() throws SQLException {
    }

    public void connectToDB() throws SQLException {
        statement.execute("create table stock_quote\n" +
                "(\n" +
                "\tsymbol varchar(20) not null primary key,\n" +
                "\tcompanyName varchar(255) null,\n" +
                "\tcalculationPrice varchar(20) null,\n" +
                "\topen int null,\n" +
                "\topenTime int null,\n" +
                "\tclose int null,\n" +
                "\tcloseTime int null,\n" +
                "\thigh double null,\n" +
                "\tlow double null,\n" +
                "\tlatestPrice double null,\n" +
                "\tlatestSource varchar(255) null,\n" +
                "\tlatestTime varchar(255) null,\n" +
                "\tlatestUpdate long null,\n" +
                "\tlatestVolume int null,\n" +
                "\tvolume int null,\n" +
                "\tiexRealtimePrice double null,\n" +
                "\tiexLastUpdated long null,\n" +
                "\tdelayedPrice double null,\n" +
                "\tdelayedPriceTime int null,\n" +
                "\toddLotDelayedPrice double null,\n" +
                "\toddLotDelayedPriceTime int null,\n" +
                "\textendedPrice double null,\n" +
                "\textendedChange double null,\n" +
                "\textendedChangePercent double null,\n" +
                "\textendedPriceTime int null,\n" +
                "\tpreviousClose double null,\n" +
                "\tpreviousVolume int null,\n" +
                "\t`change` double null,\n" +
                "\tchangePercent double null,\n" +
                "\tiexMarketPercent double null,\n" +
                "\tiexVolume int null,\n" +
                "\tavgTotalVolume int null,\n" +
                "\tiexBidPrice double null,\n" +
                "\tiexBidSize int null,\n" +
                "\tiexAskPrice double null,\n" +
                "\tiexAskSize int null,\n" +
                "\tmarketCap long null,\n" +
                "\tweek52High double null,\n" +
                "\tweek52Low double null,\n" +
                "\tytdChange double null,\n" +
                "\tpeRatio double null,\n" +
                "\tlastTradeTime long null,\n" +
                "\tisUSMarketOpen BIT null\n" +
                ");\n" +
                "\n");
    }

    public void getStockThatEnabled() throws IOException, JSONException {
        boolean isCompaniesAvailable = true;
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

        //if JSON is not recognized - reattempt
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(inline.toString());
        } catch (JSONException e) {
            getStockThatEnabled();
            return;
        }


                while (isCompaniesAvailable){
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String name = jsonObject.getString("symbol");
                        boolean isTrue = jsonObject.getBoolean("isEnabled");
                        if (isTrue) {
                            storageForEnabledStock.offer(name);
                        }
                        if(i == jsonArray.length() -1)
                           isCompaniesAvailable = false;
                    }
                }

        getStockThatEnabled();


    }

    public void getDataAbout() throws InterruptedException, IOException {
        while (true) {
            //getting company that is enabled from head of shared queue
            String stockToGet = storageForEnabledStock.take();
            //send for taking data about current stock
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
                    //getting JSON about stock
                    inline.append(scanner.nextLine());

                }
                scanner.close();
            }
            //stock JSON process
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(inline.toString().replaceAll("null", "0"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //setting variables for JSON queries
            String symbol = null;
            String companyName = null;
            String calculationPrice = null;
            int open = 0;
            int openTime = 0;
            int close = 0;
            int closeTime = 0;
            double high = 0;
            double low = 0;
            double latestPrice = 0;
            String latestSource = null;
            String latestTime = null;
            long latestUpdate = 0;
            int latestVolume = 0;
            int volume = 0;
            double iexRealtimePrice = 0;
            long iexLastUpdated = 0;
            double delayedPrice = 0;
            int delayedPriceTime = 0;
            double oddLotDelayedPrice = 0;
            int oddLotDelayedPriceTime = 0;
            double extendedPrice = 0;
            double extendedChange = 0;
            double extendedChangePercent = 0;
            int extendedPriceTime = 0;
            double previousClose = 0;
            int previousVolume = 0;
            double change = 0;
            double changePercent = 0;
            double iexMarketPercent = 0;
            int iexVolume = 0;
            int avgTotalVolume = 0;
            double iexBidPrice = 0;
            int iexBidSize = 0;
            double iexAskPrice = 0;
            int iexAskSize = 0;
            long marketCap = 0;
            double week52High = 0;
            double week52Low = 0;
            double ytdChange = 0;
            double peRatio = 0;
            long lastTradeTime = 0;
            boolean isUSMarketOpen = false;

            try {
                assert jsonObject != null;
                symbol = jsonObject.getString(CurrentStockInformation.symbol);
                companyName = jsonObject.getString(CurrentStockInformation.companyName);
                calculationPrice = jsonObject.getString(CurrentStockInformation.calculationPrice);
                open = jsonObject.getInt(CurrentStockInformation.open);
                openTime = jsonObject.getInt(CurrentStockInformation.openTime);
                close = jsonObject.getInt(CurrentStockInformation.close);
                closeTime = jsonObject.getInt(CurrentStockInformation.closeTime);
                high = jsonObject.getDouble(CurrentStockInformation.high);
                low = jsonObject.getDouble(CurrentStockInformation.low);
                latestPrice = jsonObject.getDouble(CurrentStockInformation.latestPrice);
                latestSource = jsonObject.getString(CurrentStockInformation.latestSource);
                latestTime = String.valueOf(jsonObject.get(CurrentStockInformation.latestTime));
                latestUpdate = jsonObject.getLong(CurrentStockInformation.latestUpdate);
                latestVolume = jsonObject.getInt(CurrentStockInformation.latestVolume);
                volume = jsonObject.getInt(CurrentStockInformation.volume);
                iexRealtimePrice = jsonObject.getDouble(CurrentStockInformation.iexRealtimePrice);
                iexLastUpdated = jsonObject.getLong(CurrentStockInformation.iexLastUpdated);
                delayedPrice = jsonObject.getDouble(CurrentStockInformation.delayedPrice);
                delayedPriceTime = jsonObject.getInt(CurrentStockInformation.delayedPriceTime);
                oddLotDelayedPrice = jsonObject.getDouble(CurrentStockInformation.oddLotDelayedPrice);
                oddLotDelayedPriceTime = jsonObject.getInt(CurrentStockInformation.oddLotDelayedPriceTime);
                extendedPrice = jsonObject.getDouble(CurrentStockInformation.extendedPrice);
                extendedChange = jsonObject.getDouble(CurrentStockInformation.extendedChange);
                extendedChangePercent = jsonObject.getDouble(CurrentStockInformation.extendedChangePercent);
                extendedPriceTime = jsonObject.getInt(CurrentStockInformation.extendedPriceTime);
                previousClose = jsonObject.getDouble(CurrentStockInformation.previousClose);
                previousVolume = jsonObject.getInt(CurrentStockInformation.previousVolume);
                change = jsonObject.getDouble(CurrentStockInformation.change);
                changePercent = jsonObject.getDouble(CurrentStockInformation.changePercent);
                iexMarketPercent = jsonObject.getDouble(CurrentStockInformation.iexMarketPercent);
                iexVolume = jsonObject.getInt(CurrentStockInformation.iexVolume);
                avgTotalVolume = jsonObject.getInt(CurrentStockInformation.avgTotalVolume);
                iexBidPrice = jsonObject.getDouble(CurrentStockInformation.iexBidPrice);
                iexBidSize = jsonObject.getInt(CurrentStockInformation.iexBidSize);
                iexAskPrice = jsonObject.getDouble(CurrentStockInformation.iexAskPrice);
                iexAskSize = jsonObject.getInt(CurrentStockInformation.iexAskSize);
                marketCap = jsonObject.getLong(CurrentStockInformation.marketCap);
                week52High = jsonObject.getDouble(CurrentStockInformation.week52High);
                week52Low = jsonObject.getDouble(CurrentStockInformation.week52Low);
                ytdChange = jsonObject.getDouble(CurrentStockInformation.ytdChange);
                peRatio = jsonObject.getDouble(CurrentStockInformation.peRatio);
                lastTradeTime = jsonObject.getLong(CurrentStockInformation.lastTradeTime);
                isUSMarketOpen = jsonObject.getBoolean(CurrentStockInformation.isUSMarketOpen);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            //as a symbol is unique, the next iteration the stock data will be updated
            String symbolQuery = String.format("INSERT INTO stock_exchange.stock_quote (symbol, companyName, calculationPrice, open, openTime, close, " +
                            "closeTime, high, low, " +
                            "latestPrice, latestSource, latestTime, latestUpdate, latestVolume, volume, iexRealtimePrice, " +
                            "iexLastUpdated, delayedPrice, delayedPriceTime, oddLotDelayedPrice, oddLotDelayedPriceTime, extendedPrice, " +
                            "extendedChange, extendedChangePercent, extendedPriceTime, previousClose, previousVolume, `change`, " +
                            "changePercent, iexMarketPercent, iexVolume, avgTotalVolume, iexBidPrice, iexBidSize, iexAskPrice, " +
                            "iexAskSize, marketCap, week52High, week52Low, ytdChange, peRatio, lastTradeTime, isUSMarketOpen) VALUES ('%s', '%s', '%s', %d, %d, %d, %d, %f, %f, %f, '%s', '%s', %d, %d, %d, " +
                            "%f, %d, %f, %d,  %f, %d, %f, %f, %f, %d, %f, %d, %f, %f, %f, %d, %d, %f, %d, %f, %d, %d, %f, %f, %f, %f, %d, %b) ON DUPLICATE KEY UPDATE companyName = '%s', calculationPrice = '%s', open = %d, openTime = %d," +
                            " close = %d, closeTime = %d, high = %f, low = %f, latestPrice = %f, latestSource = '%s', latestTime = '%s', latestUpdate = %d, latestVolume = %d, volume = %d, iexRealtimePrice = %f, " +
                            "iexLastUpdated = %d, delayedPrice = %f, delayedPriceTime = %d, oddLotDelayedPrice = %f, oddLotDelayedPriceTime = %d, extendedPrice = %f, extendedChange = %f, extendedChangePercent = %f, extendedPriceTime = %d, previousClose = %f, " +
                            "previousVolume = %d, `change` = %f, changePercent = %f, iexMarketPercent = %f, iexVolume = %d, avgTotalVolume = %d, iexBidPrice = %f, iexBidSize = %d, iexAskPrice = %f, " +
                            "iexAskSize = %d, marketCap = %d, week52High = %f, week52Low = %f, ytdChange = %f, peRatio = %f, lastTradeTime = %d, isUSMarketOpen = %b", symbol,
                    companyName, calculationPrice, open, openTime, close, closeTime, high, low, latestPrice, latestSource, latestTime,
                    latestUpdate, latestVolume, volume, iexRealtimePrice, iexLastUpdated, delayedPrice, delayedPriceTime, oddLotDelayedPrice, oddLotDelayedPriceTime, extendedPrice,
                    extendedChange, extendedChangePercent, extendedPriceTime, previousClose, previousVolume, change, changePercent, iexMarketPercent, iexVolume, avgTotalVolume, iexBidPrice,
                    iexBidSize, iexAskPrice, iexAskSize, marketCap, week52High, week52Low, ytdChange, peRatio, lastTradeTime, isUSMarketOpen, companyName, calculationPrice, open, openTime, close, closeTime, high, low,
                    latestPrice, latestSource, latestTime, latestUpdate, latestVolume, volume, iexRealtimePrice, iexLastUpdated, delayedPrice, delayedPriceTime, oddLotDelayedPrice, oddLotDelayedPriceTime, extendedPrice,
                    extendedChange, extendedChangePercent, extendedPriceTime, previousClose, previousVolume, change, changePercent, iexMarketPercent, iexVolume, avgTotalVolume, iexBidPrice,
                    iexBidSize, iexAskPrice, iexAskSize, marketCap, week52High, week52Low, ytdChange, peRatio, lastTradeTime, isUSMarketOpen);

            try {
                statement.executeUpdate(symbolQuery);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }


}
