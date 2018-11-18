package andysheh.washington.edu.forecast;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


// Class designed to access SQLiteAssetHelper
// makes it easier to open Music.db
public class DatabaseOpenHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "Music.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
