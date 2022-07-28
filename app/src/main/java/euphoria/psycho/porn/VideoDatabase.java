package euphoria.psycho.porn;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore.Video;

import java.util.List;

public class VideoDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    public VideoDatabase(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("    CREATE TABLE \"videos\" (\n" +
                "            \"id\"            INTEGER PRIMARY KEY,\n" +
                "            \"title\"            TEXT NOT NULL,\n" +
                "            \"url\"            TEXT UNIQUE,\n" +
                "            \"thumbnail\"            TEXT,\n" +
                "            \"source\"            TEXT,\n" +
                "              \"width\"            INTEGER,\n" +
                "            \"height\"            INTEGER,\n" +
                "            \"duration\"            INTEGER,\n" +
                "            \"hidden\"            INTEGER,\n" +
                "            \"video_type\"            INTEGER,\n" +
                "            \"create_at\"            INTEGER,\n" +
                "            \"update_at\"            INTEGER );");
    }

    public void insertVideos(List<Video> videos) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Video video : videos) {
                ContentValues values = new ContentValues();
                values.put("title", video.Title);
                values.put("url", video.Url);
                values.put("thumbnail", video.Thumbnail);
                values.put("source", video.Source);
                values.put("width", video.Width);
                values.put("height", video.Height);
                values.put("duration", video.Duration);
                values.put("hidden", video.Hidden);
                values.put("video_type", video.VideoType);
                values.put("create_at", System.currentTimeMillis());
                values.put("update_at", System.currentTimeMillis());
                db.insert("videos", null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public class Video {
        public int Id;
        public String Title;
        public String Url;
        public String Thumbnail;
        public String Source;
        public int Width;
        public int Height;
        public int Duration;
        public int Hidden;
        public int VideoType;
        public long CreateAt;
        public long UpdateAt;
    }
}