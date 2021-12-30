package euphoria.psycho.porn.tasks;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TaskInfo {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo(name = "uri")
    public String uri;
    @ColumnInfo(name = "file_name")
    public String fileName;
    @ColumnInfo(name = "segment_size")
    public int segmentSize;
    @ColumnInfo(name = "status")
    public int status;
    @ColumnInfo(name = "sequence")
    public int sequence;
    @ColumnInfo(name = "directory")
    public String directory;
}