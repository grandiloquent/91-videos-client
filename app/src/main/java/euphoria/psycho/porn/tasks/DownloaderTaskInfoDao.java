package euphoria.psycho.porn.tasks;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface DownloaderTaskInfoDao {
    @Insert
    void insertAll(DownloaderTaskInfo... downloaderTaskInfos);
}


