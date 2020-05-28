package startApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

@Service
public class GetDataFromWeb {
    public void getData() throws IOException, JSONException {
        URL url = new URL("https://sandbox.iexapis.com/stable/ref-data/symbols?token=Tpk_ee567917a6b640bb8602834c9d30e571");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();

        int response = httpURLConnection.getResponseCode();
        String inline = "";

        if (response != 200)
            throw new RuntimeException("as" + response);
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
