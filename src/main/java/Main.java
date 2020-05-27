import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


public class Main {

    public static void main(String[] args) throws IOException, JSONException {

        URL url = new URL("https://sandbox.iexapis.com/stable/ref-data/symbols?token=Tpk_ee567917a6b640bb8602834c9d30e571");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();

        int responce = httpURLConnection.getResponseCode();
        String inline = "";

        if (responce != 200)
            throw new RuntimeException("as" + responce);
        else {
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline += scanner.nextLine();

            }
            scanner.close();
        }

        JSONArray jsonArray = new JSONArray(inline);

        List<String> stockName = new ArrayList<String>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("symbol");
            boolean isTrue = jsonObject.getBoolean("isEnabled");
            if (isTrue) {
                stockName.add(name);
            }
        }
        Collections.sort(stockName);
        System.out.println(stockName);

    }
}
