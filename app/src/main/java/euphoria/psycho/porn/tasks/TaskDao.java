package euphoria.psycho.porn.tasks;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TaskDao {
    @Insert
    void insertAll(Task... downloaderTasks);

    @Query("SELECT * FROM task")
    List<Task> getAll();

    @Query("UPDATE task SET total_size = :totalSize WHERE uid = :uid")
    void updateTotalSize(int uid, long totalSize);
}

