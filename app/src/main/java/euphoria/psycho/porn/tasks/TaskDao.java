package euphoria.psycho.porn.tasks;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface TaskDao {
    @Insert
    void insertAll(Task... downloaderTasks);
    @Query("SELECT * FROM task")
    List<Task> getAll();
}

