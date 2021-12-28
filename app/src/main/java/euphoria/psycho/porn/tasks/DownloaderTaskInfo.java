package euphoria.psycho.porn.tasks;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DownloaderTaskInfo {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo(name = "uri")
    public String uri;
    @ColumnInfo(name = "file_name")
    public String fileName;
    @ColumnInfo(name = "segment_size")
    public int segmentSize;
}