package euphoria.psycho.porn.tasks;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.room.Room;
import euphoria.psycho.porn.Native;
import euphoria.psycho.porn.Shared;
import euphoria.psycho.porn.WebActivity;

import android.app.Notification.Builder;
import android.app.Notification.Builder;
import android.telephony.mbms.DownloadRequest;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

public class DownloaderService extends Service {
    public static final String DOWNLOAD_VIDEO = "DOWNLOAD_VIDEO";
    public static final String EXTRA_VIDEO_ADDRESS = "video_address";
    public static final String KEY_DOWNLOAD_LINK = "DOWNLOAD_LINK";
    public static final String KEY_DOWNLOAD_TITLE = "DOWNLOAD_TITLE";
    private NotificationManager mNotificationManager;
    private Handler mHandler = new Handler();

    @RequiresApi(api = VERSION_CODES.O)
    public static void createNotificationChannel(Context context, String id, CharSequence name) {
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
        context.getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);
    }

    private static String getString(String uri) throws IOException {
        URL url = new URL(uri);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, null, new java.security.SecureRandom());
            sc.createSSLEngine();
            urlConnection.setSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
        int code = urlConnection.getResponseCode();
        if (code < 400 && code >= 200) {
            return Shared.readString(urlConnection);
        } else {
            return null;
        }
    }

    private void createDatabase(String downloadLink, String response, File dir) {
        File database = new File(dir, "data.db");
        if (database.exists()) {
            return;
        }
        List<Task> tasks = createTasks(response);
        String dbName = database.getAbsolutePath();
        TaskDatabase db = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, dbName).build();
        db.taskDao().insertAll(tasks.toArray(new Task[0]));
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.segmentSize = tasks.size();
        taskInfo.uri = downloadLink;
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

    private void createTask(String downloadLink) throws IOException {
        String response = getString(downloadLink);
        if (response == null) {
            throw new NullPointerException();
        }
        File dir = createDirectory(response);
        File database = new File(dir, "data.db");
        if (database.exists()) {
            return;
        }
        createDatabase(downloadLink, response, dir);
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
            createNotificationChannel(this, DOWNLOAD_VIDEO, "下载视频频道");
        }
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String videoAddress = intent.getStringExtra(EXTRA_VIDEO_ADDRESS);
        showNotification("添加新任务：" + videoAddress);
        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Pair<String, String> info = getVideoInformation(videoAddress);
            if (info != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showNotification("准备下载：" + info.first);
                    }
                });
            }


        }).start();
        return super.onStartCommand(intent, flags, startId);
    }
}
