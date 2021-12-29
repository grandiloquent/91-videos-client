package euphoria.psycho.porn.tasks;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {
        @Index(value = {"sequence"}, unique = true)
})
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo(name = "uri")
    public String uri;
    @ColumnInfo(name = "total_size")
    public long totalSize;
    @ColumnInfo(name = "sequence")
    public int sequence;

}

