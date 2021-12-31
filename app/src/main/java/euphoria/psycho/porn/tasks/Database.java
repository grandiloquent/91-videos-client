package euphoria.psycho.porn.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    public Database(@Nullable Context context, @Nullable String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `Task` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uri` TEXT, `total_size` INTEGER NOT NULL, `sequence` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Task_sequence` ON `Task` (`sequence`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `TaskInfo` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uri` TEXT, `file_name` TEXT, `segment_size` INTEGER NOT NULL, `status` INTEGER NOT NULL, `sequence` INTEGER NOT NULL, `directory` TEXT)");
    }

    public void insertTaskInfo(TaskInfo taskInfo) {
        ContentValues values = new ContentValues();
        values.put("uri", taskInfo.uri);
        values.put("file_name", taskInfo.fileName);
        values.put("segment_size", taskInfo.segmentSize);
        values.put("status", taskInfo.status);
        values.put("sequence", taskInfo.sequence);
        values.put("directory", taskInfo.directory);
        getWritableDatabase().insert("TaskInfo", null, values);
    }


    public void insertTasks(List<Task> tasks) {
        getWritableDatabase().beginTransaction();
        for (Task task : tasks) {
            ContentValues values = new ContentValues();
            values.put("uri", task.uri);
            values.put("total_size", task.totalSize);
            values.put("sequence", task.sequence);
            getWritableDatabase().insert("Task", null, values);
        }
        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();
    }

    public void updateTaskStatus(int id, int status) {
        ContentValues values = new ContentValues();
        values.put("status", status);
        getWritableDatabase().update("TaskInfo", values, "uid = ?", new String[]{
                Integer.toString(id)
        });
    }

    public void updateTaskTotalSize(int id, long totalSize) {
        ContentValues values = new ContentValues();
        values.put("total_size", totalSize);
        getWritableDatabase().update("Task", values, "uid = ?", new String[]{
                Integer.toString(id)
        });
    }

    public TaskInfo getTaskInfo() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM TaskInfo LIMIT 1", null);
        if (cursor.moveToNext()) {
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.uid = cursor.getInt(0);
            taskInfo.uri = cursor.getString(1);
            taskInfo.fileName = cursor.getString(2);
            taskInfo.segmentSize = cursor.getInt(3);
            taskInfo.status = cursor.getInt(4);
            taskInfo.sequence = cursor.getInt(5);
            taskInfo.directory = cursor.getString(6);
            return taskInfo;
        }
        cursor.close();
        return null;
    }

    public List<Task> getTasks() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM Task", null);
        List<Task> tasks = new ArrayList<>();
        while (cursor.moveToNext()) {
            Task task = new Task();
            task.uid = cursor.getInt(0);
            task.uri = cursor.getString(1);
            task.totalSize = cursor.getLong(2);
            task.sequence = cursor.getInt(3);
            tasks.add(task);
        }
        cursor.close();
        return tasks;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
