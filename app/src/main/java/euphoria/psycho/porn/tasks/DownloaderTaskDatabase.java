package euphoria.psycho.porn.tasks;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DownloaderTask.class, DownloaderTaskInfo.class}, version = 1, exportSchema = false)
public abstract class DownloaderTaskDatabase extends RoomDatabase {
    public abstract DownloaderTaskDao downloaderTaskDao();
    public abstract DownloaderTaskInfoDao downloaderTaskInfoDao();
}
