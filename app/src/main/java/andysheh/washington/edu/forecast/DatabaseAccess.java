package andysheh.washington.edu.forecast;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


// Class that interacts with
// Music.db once it has been accessed
// by DatabaseOpenHelper
public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;
    Cursor c = null;


    // Constructor is made private to avoid
    // object creation outside of class
    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }


    // Returns a single instance of DatabaseAccess
    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }


    // Opens Music.db
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    // Closes Music.db if it has been opened
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }


    // Passes current weather to Music.db and retrieves a playlist
    // of songs. Each song in the playlist is specifically mapped to the
    //  current weather. The playlist is returned as an array list of string arrays
    // where each song is a string array. The string array for each song stores 4 strings.
    // array[0] = file name which is the name of the song's accompanying mp3 file saved under
    // res/raw. array[1] = title of the song. array[2] = name of the song's artist. array[3] =
    // name of album song appears under.
    public ArrayList<String[]> getSongsBasedOnWeather(String currWeather) {
        ArrayList<String[]> listOfSongs = new ArrayList<>();
        String[] queryArgs = {currWeather};
        c = database.rawQuery("SELECT FileName, SongTitle, Artist, Album FROM MUSIC WHERE Weather = ?", queryArgs);
        c.moveToFirst();
        while(!c.isAfterLast()) {
            String fileName = c.getString(0);
            String songTitle = c.getString(1);
            String artist = c.getString(2);
            String album = c.getString(3);
            String[] songInfo = {fileName, songTitle, artist, album};
            listOfSongs.add(songInfo);
            c.moveToNext();
        }
        c.close();
        return listOfSongs;
    }

}
