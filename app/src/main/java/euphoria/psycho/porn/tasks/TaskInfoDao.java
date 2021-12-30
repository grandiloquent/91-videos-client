package euphoria.psycho.porn.tasks;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TaskInfoDao {
    @Insert
    void insertAll(TaskInfo... downloaderTaskInfos);

    @Query("SELECT * FROM taskinfo")
    List<TaskInfo> getAll();

    @Query("UPDATE TaskInfo SET  status = :status WHERE uid = :uid")
    void updateStatus(int uid, int status);
}


