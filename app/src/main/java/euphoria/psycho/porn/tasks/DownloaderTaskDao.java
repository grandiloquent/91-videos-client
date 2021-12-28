package euphoria.psycho.porn.tasks;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface DownloaderTaskDao {
    @Insert
    void insertAll(DownloaderTask... downloaderTasks);
}

