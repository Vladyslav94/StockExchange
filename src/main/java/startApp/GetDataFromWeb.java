package startApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class GetDataFromWeb extends Thread {
    static LinkedBlockingQueue<String> storageForEnabledStock =  new LinkedBlockingQueue<String>();



    public void getStockThatEnabled() throws IOException, JSONException, InterruptedException {
        //getting json data about stock from remote server, parsing and putting to shared queue
        URL url = new URL("https://sandbox.iexapis.com/stable/ref-data/symbols?token=Tpk_ee567917a6b640bb8602834c9d30e571");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();

        int response = httpURLConnection.getResponseCode();
        String inline = "";

        if (response != 200)
            throw new RuntimeException("Error while parsing" + response);
        else {
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline += scanner.nextLine();

            }
            scanner.close();
        }

        JSONArray jsonArray = new JSONArray(inline);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("symbol");
            boolean isTrue = jsonObject.getBoolean("isEnabled");
            if (isTrue) {
                storageForEnabledStock.offer(name);
            }
        }

    }


    public void getDataAbout() throws InterruptedException, IOException, JSONException {
        while (true) {
            String stockToGet = storageForEnabledStock.take();
            String linkForTakingDataAboutStock =
                    String.format("https://sandbox.iexapis.com/stable/stock/%s/quote?token=Tpk_ee567917a6b640bb8602834c9d30e571", stockToGet);
            URL url = new URL(linkForTakingDataAboutStock);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            int response = httpURLConnection.getResponseCode();
            String inline = "";

            if (response != 200)
                throw new RuntimeException("Error while parsing" + response);
            else {
                Scanner scanner = new Scanner(url.openStream());
                while (scanner.hasNext()) {
                    inline += scanner.nextLine();

                }
                scanner.close();
            }

            JSONObject jsonObject = new JSONObject(inline);
            String infoAboutStock = jsonObject.getString("companyName");
            System.out.println(infoAboutStock);

        }
    }


}
