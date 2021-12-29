package euphoria.psycho.porn.tasks;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface TaskInfoDao {
    @Insert
    void insertAll(TaskInfo... downloaderTaskInfos);

    @Query("SELECT * FROM taskinfo")
    List<TaskInfo> getAll();
}


