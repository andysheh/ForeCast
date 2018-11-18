package andysheh.washington.edu.forecast;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


// Asynchronous task that pulls weather data from
// internet and passes it to MainActivity
public class DownloadTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... urls) {
        String result = "";
        URL url;
        HttpURLConnection httpURLConnection;
        try {
            // Reads current weather data for given coordinates
            // by accessing OpenWeatherMap API.
           url = new URL(urls[0]);
           httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            int data = inputStreamReader.read();
            // Continues reading data from site until -1 is returned
            // which signifies end
            while(data != -1) {
                char current = (char) data;
                result += current;
                data = inputStreamReader.read();
            }
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }


    // method is left blank in this case because
    // AsyncTask is only ever executed with the get function
    // attached at end, so result will return to main function and
    // does not appear here. Cannot be deleted because it is
    // required by AsyncTask
    @Override
    protected void onPostExecute(String result) {
    }
}
