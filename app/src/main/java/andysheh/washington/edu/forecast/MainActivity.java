package andysheh.washington.edu.forecast;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private String currWeather;
    private MediaPlayer mediaPlayer;
    private ArrayList<String[]> playList;
    // Play button plays song when playOrPause = true
    // Play button pauses song when playOrPause = false
    private boolean playOrPause;
    // Checks if user has ever hit play button
    // since app has been started. Include this
    // so we know when to first call play method. W/o
    // we couldn't tell if we should resume mediaPlayer or create new one
    private boolean hasPlayEverBeenHit;
    // Checks current track number of playlist.
    // Allows to loop back to beginning of playlist when we reach the end.
    private int currSongID;
    // Checks if current song should be repeated
    private boolean repeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set-up objects from layout
        final Button playButton = (Button) findViewById(R.id.playButton);
        Button repeatButton = (Button) findViewById(R.id.repeatButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);
        TextView currWeatherTextView = (TextView) findViewById(R.id.currentWeatherTextView);
        ImageView currWeatherSymbol = (ImageView) findViewById(R.id.weatherImageView);

        currWeatherTextView.setGravity(Gravity.CENTER_HORIZONTAL);

        // Set-up variables for activity
        playOrPause = true;
        hasPlayEverBeenHit = false;
        currSongID = 0;
        repeat = false;

        // Get current location
        //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //String provider = locationManager.NETWORK_PROVIDER;
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        //Location location = locationManager.getLastKnownLocation(provider);
        // double lat = location.getLatitude();
        // double lon = location.getLongitude();

        // For now app only gets current weather of Seattle
        // Otherwise app won't work unless location permission turned on
        double lat = 47.60357;
        double lon = -122.3295;



        // Get weather of location based on user coordinates
        DownloadTask task = new DownloadTask();
        String asyncTaskResult = "";
        try {
           asyncTaskResult = task.execute("http://api.openweathermap.org/data/2.5/weather?lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(lon) + "&appid=a0a0cb05483066a9f52e39a95f646d05").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // Gets current weather from JSON Object
        // returned by 'task' and places it on Activity
        this.currWeather = getWeather(asyncTaskResult);
        currWeatherTextView.setText(this.currWeather);

        // Change curr weather icon to match weather
        if(this.currWeather.equals("Mist") | this.currWeather.equals("Fog")) {
            currWeatherSymbol.setImageResource(R.drawable.haze);
        } else if (this.currWeather.equals("Clear")) {
            currWeatherSymbol.setImageResource(R.drawable.sunny);
        } else if (this.currWeather.equals("Rain") | this.currWeather.equals("Drizzle")) {
            currWeatherSymbol.setImageResource(R.drawable.rainy);
        } else {
            currWeatherSymbol.setImageResource(R.drawable.cloudy);
        }

        // Set playlist that matches user's current weather
        // to local variable
        this.playList = getPlayList(this.currWeather);

        // Checks if repeat has been tapped
        // Switches value of repeat
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repeat = !repeat;
            }
        });

        // Checks if play has been tapped.
        // If it's never been tapped -> create new media player
        // If mediaPlayer is paused -> resume song
        // If mediaPlayer is playing -> pause song
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasPlayEverBeenHit) {
                    play();
                    playButton.setText("Pause");
                    hasPlayEverBeenHit = true;
                } else if (playOrPause) {
                    mediaPlayer.start();
                    playButton.setText("Pause");
                } else {
                    mediaPlayer.pause();
                    playButton.setText("Play");
                }
                playOrPause = !playOrPause;
            }
        });

        // Changes to next song if Next button is tapped
        // user has already hit play at least once
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer != null)
                    changeSong();
            }
        });
    }


    // Gets song from playlist indexed at currSongID value
    // Changes song, artist, album text view to match info for song
    // plays song and then next one when current song is done
    public void play () {
            String[] currSongInfo = this.playList.get(this.currSongID);
            String fileName = currSongInfo[0];
            String songName = currSongInfo[1];
            String artist = currSongInfo[2];
            String album = currSongInfo[3];
            TextView songTextView = (TextView) findViewById(R.id.songTextView);
            TextView artistTextView = (TextView) findViewById(R.id.artistTextView);
            TextView albumTextView = (TextView) findViewById(R.id.albumTextView);
            songTextView.setText(songName);
            artistTextView.setText(artist);
            albumTextView.setText(album);
            int resid = songNameToResid(fileName);
            if(this.mediaPlayer == null) {
                this.mediaPlayer = MediaPlayer.create(this, resid);
            }
            this.mediaPlayer.start();
            this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    changeSong();
                }
            });
    }

    // stops current song and plays
    // next song in playlist
    public void changeSong() {
        this.mediaPlayer.stop();
        this.mediaPlayer.release();
        this.mediaPlayer = null;
        if(repeat) {
        } else if (this.currSongID == this.playList.size() - 1) {
            this.currSongID = 0;
        } else {
            this.currSongID++;
        }
        play();
    }

    // Retrieves playlist of songs that match current weather
    // by inputting current weather retrieved from OpenWeatherMap API
    // and inputting it into Music.db
    public ArrayList<String[]> getPlayList(String currWeather) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        ArrayList<String[]> playList = databaseAccess.getSongsBasedOnWeather(currWeather);
        databaseAccess.close();
        return playList;
    }

    // Converts fileName of song to an resID value
    // so mediaPlayer knows which song to access from
    // res/raw folder
    public int songNameToResid(String songName) {
        songName = songName.toLowerCase();
        int resid = getResources().getIdentifier(songName, "raw", getPackageName());
        return resid;
    }

    // Parse JSON data from OpenWeatherMap API and
    // returns current weather. Options for current weather are
    // 'Clear', 'Rain', 'Clouds', 'Mist', 'Fog', 'Drizzle',
    // 'Snow', and 'Thunderstorm'
    // As of right now no songs map to 'Snow'
    public String getWeather(String asyncTaskResult ) {
        String weatherDescription = "";
        try {
            JSONObject jsonObject = new JSONObject(asyncTaskResult);
            JSONArray weatherData = new JSONArray(jsonObject.getString("weather"));
            JSONObject primaryWeather = weatherData.getJSONObject(0);
            weatherDescription = primaryWeather.getString("main");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weatherDescription;
    }
}
