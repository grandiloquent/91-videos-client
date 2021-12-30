package euphoria.psycho.porn.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.List;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Process;

import euphoria.psycho.porn.Shared;

public class DownloaderRequest implements Runnable {

    public static final int STATUS_CONTENT_LENGTH = 2;
    public static final int STATUS_ERROR = -2;
    public static final int STATUS_ERROR_IO_INPUT = -3;
    public static final int STATUS_ERROR_IO_OUTPUT = -4;
    public static final int STATUS_FATAL_ERROR = -1;
    public static final int STATUS_FILE_CACHED = 1;
    public static final int STATUS_MERGE_COMPLETED = 5;
    public static final int STATUS_MERGE_FAILED = -5;
    public static final int STATUS_MERGE_VIDEO = 4;
    public static final int STATUS_PAUSED = 3;
    public static final int STATUS_START = 6;

    private final String mBaseUri;
    private final RequestListener mListener;
    private volatile boolean mPaused;
    private TaskInfo mTaskInfo;
    private int mStatus;
    private List<Task> mTasks;
    private String mDirectory;
    private TaskDatabase mTaskDatabase;

    public DownloaderRequest(TaskDatabase taskDatabase, RequestListener listener) {
        mTaskInfo = taskDatabase.taskInfoDao().getAll().get(0);
        mTasks = taskDatabase.taskDao().getAll();
        mListener = listener;
        mBaseUri = Shared.substringBeforeLast(mTaskInfo.uri, "/") + "/";
        mDirectory = new File(mTaskInfo.directory).getAbsolutePath();
        mTaskDatabase = taskDatabase;

    }

    public int getStatus() {
        return mStatus;
    }

    public TaskDatabase getTaskDatabase() {
        return mTaskDatabase;
    }

    public TaskInfo getTaskInfo() {
        return mTaskInfo;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void setPaused(boolean paused) {
        mPaused = paused;
    }

    private File createSegmentFile(Task task) {
        return new File(mDirectory, Shared.substringBefore(task.uri, "?"));
    }

    private void emitSynchronizationEvent(int status) {
        mStatus = status;
        mListener.onProgress(this);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        if (mTaskInfo.status == 5 && new File(mTaskInfo.fileName).exists()) {
            emitSynchronizationEvent(STATUS_MERGE_COMPLETED);
            return;
        }
        emitSynchronizationEvent(STATUS_START);
        for (Task task : mTasks) {
            if (mPaused) {
                emitSynchronizationEvent(STATUS_PAUSED);
                return;
            }
            mTaskInfo.sequence = task.sequence;
            File file = createSegmentFile(task);
            if (file.exists()) {
                if (task.totalSize > 0 && file.length() == task.totalSize) {
                    emitSynchronizationEvent(STATUS_FILE_CACHED);
                    continue;
                }
            }
            try {
                URL url = new URL(mBaseUri + task.uri);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                int status = c.getResponseCode();
                if (status < 200 || status >= 400) {
                    emitSynchronizationEvent(STATUS_FATAL_ERROR);
                    return;
                }
                if (VERSION.SDK_INT >= VERSION_CODES.N) {
                    task.totalSize = c.getContentLengthLong();
                } else {
                    try {
                        task.totalSize = Long.parseLong(c.getHeaderField("Content-Length"));
                        mTaskDatabase.taskDao().updateTotalSize(task.uid, task.totalSize);
                    } catch (Exception e) {
                    }
                }
                emitSynchronizationEvent(STATUS_CONTENT_LENGTH);
                if (file.exists()) {
                    if (task.totalSize > 0 && file.length() == task.totalSize) {
                        emitSynchronizationEvent(STATUS_FILE_CACHED);
                        continue;
                    }
                }
                InputStream in = c.getInputStream();
                FileOutputStream out = new FileOutputStream(file);
                final byte[] buffer = new byte[8192];
                while (true) {
                    if (mPaused) {
                        emitSynchronizationEvent(STATUS_PAUSED);
                        return;
                    }
                    int len;
                    try {
                        len = in.read(buffer);
                    } catch (IOException e) {
                        emitSynchronizationEvent(STATUS_ERROR_IO_INPUT);
                        return;
                    }
                    if (len == -1) {
                        break;
                    }
                    try {
                        out.write(buffer, 0, len);
                        //mVideoTask.DownloadedSize += len;
                        //updateProgress(fileName);
                    } catch (IOException e) {
                        emitSynchronizationEvent(STATUS_ERROR_IO_OUTPUT);
                        return;
                    }
                }

            } catch (Exception exc) {
                emitSynchronizationEvent(STATUS_ERROR);
                return;
            }
        }
        emitSynchronizationEvent(STATUS_MERGE_VIDEO);
        try {
            if (mPaused) {
                emitSynchronizationEvent(STATUS_PAUSED);
                return;
            }
            try (FileChannel fc = new FileOutputStream(mTaskInfo.fileName).getChannel()) {
                for (Task task : mTasks) {
                    if (mPaused) {
                        emitSynchronizationEvent(STATUS_PAUSED);
                        return;
                    }
                    try (FileChannel fci = new FileInputStream(createSegmentFile(task)).getChannel()) {
                        fci.transferTo(0, fci.size(), fc);
                    }
                }
                fc.force(true);
            }
            emitSynchronizationEvent(STATUS_MERGE_COMPLETED);
        } catch (IOException e) {
            emitSynchronizationEvent(STATUS_MERGE_FAILED);
        }


    }

}
