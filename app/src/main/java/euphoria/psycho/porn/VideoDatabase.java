package euphoria.psycho.porn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore.Video;

import java.util.ArrayList;
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
                values.put("create_at", video.CreateAt);
                values.put("update_at", System.currentTimeMillis());
                db.insertWithOnConflict("videos", null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Video> queryVideos() {
        Cursor cursor = getReadableDatabase().rawQuery("select * from videos ORDER by create_at DESC", null);
        List<Video> videos = new ArrayList<>();
        while (cursor.moveToNext()) {
            Video video = new Video();
            video.Id = cursor.getInt(0);
            video.Title = cursor.getString(1);
            video.Url = cursor.getString(2);
            video.Thumbnail = cursor.getString(3);
            video.Source = cursor.getString(4);
            video.Width = cursor.getInt(5);
            video.Height = cursor.getInt(6);
            video.Duration = cursor.getInt(7);
            video.Hidden = cursor.getInt(8);
            video.VideoType = cursor.getInt(9);
            video.CreateAt = cursor.getLong(10);
            video.UpdateAt = cursor.getLong(11);
            videos.add(video);
        }
        return videos;
    }

    public Video queryVideoSource(int id) {
        Cursor cursor = getReadableDatabase().rawQuery("select id, title,source from videos where id = ? limit 1", new String[]{Integer.toString(id)});
        Video video = new Video();
        if (cursor.moveToNext()) {
            video.Id = cursor.getInt(0);
            video.Title = cursor.getString(1);
            video.Source = cursor.getString(2);
        }
        return video;
    }

    public void updateVideoSource(int id, String source) {
        getWritableDatabase().execSQL("update videos set source = ?,update_at = ? where id = ?", new String[]{
                source,
                Long.toString(System.currentTimeMillis()),
                Integer.toString(id)
        });
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static class Video {
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

        @Override
        public String toString() {
            return "Video{" +
                    "Id=" + Id +
                    ", Title='" + Title + '\'' +
                    ", Url='" + Url + '\'' +
                    ", Thumbnail='" + Thumbnail + '\'' +
                    ", Source='" + Source + '\'' +
                    ", Width=" + Width +
                    ", Height=" + Height +
                    ", Duration=" + Duration +
                    ", Hidden=" + Hidden +
                    ", VideoType=" + VideoType +
                    ", CreateAt=" + CreateAt +
                    ", UpdateAt=" + UpdateAt +
                    '}';
        }
    }
}