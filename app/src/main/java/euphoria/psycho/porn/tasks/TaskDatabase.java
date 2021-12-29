package euphoria.psycho.porn.tasks;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class, TaskInfo.class}, version = 1, exportSchema = false)
public abstract class TaskDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract TaskInfoDao taskInfoDao();
}
