package euphoria.psycho.porn.tasks;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.room.Room;
import euphoria.psycho.porn.Native;
import euphoria.psycho.porn.R;
import euphoria.psycho.porn.Shared;
import euphoria.psycho.porn.WebActivity;

import android.app.Notification.Builder;
import android.app.Notification.Builder;
import android.telephony.mbms.DownloadRequest;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import static euphoria.psycho.porn.Shared.USER_AGENT;
import static euphoria.psycho.porn.tasks.DownloadUtils.background;
import static euphoria.psycho.porn.tasks.DownloadUtils.createNotificationChannel;

public class DownloaderService extends Service implements RequestListener {
    public static final String DOWNLOAD_VIDEO = "DOWNLOAD_VIDEO";
    public static final String EXTRA_VIDEO_ADDRESS = "video_address";
    private final Handler mHandler = new Handler();
    private NotificationManager mNotificationManager;
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(3);
    private List<DownloaderRequest> mDownloaderRequests = new ArrayList<>();


    private void checkTask() {
        File[] directories = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).listFiles(File::isDirectory);
        if (directories == null) return;
        for (File dir : directories) {
            File database = new File(dir, "data.db");
            String dbName = database.getAbsolutePath();
            TaskDatabase db = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, dbName).build();
            TaskInfo taskInfo = db.taskInfoDao().getAll().get(0);
            if (taskInfo.status == 5 || new File(taskInfo.fileName).exists()) {
                Native.removeDirectory(taskInfo.directory);
                continue;
            }
            DownloaderRequest downloaderRequest = new DownloaderRequest(db, this);
            for (int i = 0; i < mDownloaderRequests.size(); i++) {
                if (mDownloaderRequests.get(i).getTaskInfo().fileName.equals(taskInfo.fileName)) {
                    return;
                }
            }
            synchronized (this) {
                mDownloaderRequests.add(downloaderRequest);
            }
            mExecutorService.submit(downloaderRequest);
        }
        if (mDownloaderRequests.size() == 0) {
            mNotificationManager.cancel(1);
            stopSelf();
            return;
        }
        showNotification(getString(R.string.downloading_video, 1, mDownloaderRequests.size()));

    }

    private void checkUncompletedTasks() {
        background(() -> {
            checkTask();
            return null;
        });
    }

    private void createDatabase(DatabaseParameter databaseParameter) {
        File database = new File(databaseParameter.dir, "data.db");
        if (database.exists()) {
            return;
        }
        List<Task> tasks = createTasks(databaseParameter.response);
        String dbName = database.getAbsolutePath();
        TaskDatabase db = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, dbName).build();
        db.taskDao().insertAll(tasks.toArray(new Task[0]));
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.segmentSize = tasks.size();
        taskInfo.uri = databaseParameter.downloadLink;
        taskInfo.fileName = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                Shared.getValidFileName(databaseParameter.title) + ".mp4").getAbsolutePath();
        taskInfo.directory = databaseParameter.dir.getAbsolutePath();
        db.taskInfoDao().insertAll(taskInfo);
    }

    private File createDirectory(String contents) {
        String directoryName = Shared.md5(contents);
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), directoryName);
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        return dir;
    }

    private void createTask(Pair<String, String> info) throws IOException {
        String response = DownloadUtils.getString(info.second);
        if (response == null) {
            throw new NullPointerException();
        }
        File dir = createDirectory(response);
        createDatabase(new DatabaseParameter(info.first, info.second, response, dir));
    }

    private List<Task> createTasks(String response) {
        String[] segments = response.split("\n");
        int sequence = 0;
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].startsWith("#EXTINF:")) {
                String segment = segments[i + 1];
                Task task = new Task();
                task.uri = segment;
                task.sequence = sequence++;
                tasks.add(task);
                i++;
            }
        }
        return tasks;
    }

    private Pair<String, String> getVideoInformation(String videoAddress) {
        Pair<String, String> results;
        if (videoAddress.contains("91porn.com")) {
            results = WebActivity.process91Porn(videoAddress);
        } else {
            results = WebActivity.processCk(this, videoAddress);
        }
        return results;
    }

    private void showNotification(String title) {
        Builder builder;
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            builder = new Builder(this, DOWNLOAD_VIDEO);
        } else {
            builder = new Builder(this);
        }
        Intent i = new Intent(this, DownloaderActivity.class);
        builder.setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(title)
                .setContentIntent(PendingIntent.getActivity(this, 1, i, 0));
        mNotificationManager.notify(1, builder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            createNotificationChannel(this, DOWNLOAD_VIDEO, getString(R.string.download_video_channel));
        }
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification(getString(R.string.download));
    }

    @Override
    public void onProgress(DownloaderRequest downloaderRequest) {
        int status = downloaderRequest.getStatus();
        if (status == 5 || status < 0) {
            downloaderRequest
                    .getTaskDatabase()
                    .taskInfoDao().updateStatus(downloaderRequest.getTaskInfo().uid, downloaderRequest.getStatus());
        }
        Log.e("B5aOx2", String.format("onProgress,%s %s",
                downloaderRequest.getTaskInfo().fileName
                , downloaderRequest.getStatus()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String videoAddress = intent.getStringExtra(EXTRA_VIDEO_ADDRESS);
        if (videoAddress == null) {
            checkUncompletedTasks();
            return super.onStartCommand(intent, flags, startId);
        }
        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Pair<String, String> info = getVideoInformation(videoAddress);
            if (info != null) {
                if (info.second.contains(".mp4")) {
                    Shared.downloadFile(this,
                            (info.first == null ? Shared.toHex(info.second.getBytes(StandardCharsets.UTF_8)) : info.first) + ".mp4", info.second, USER_AGENT);
                    return;
                }
                try {
                    createTask(info);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                checkTask();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }


    private static class DatabaseParameter {
        public String title;
        public String downloadLink;
        public String response;
        public File dir;

        public DatabaseParameter(String title, String downloadLink, String response, File dir) {
            this.title = title;
            this.downloadLink = downloadLink;
            this.response = response;
            this.dir = dir;
        }
    }
}
