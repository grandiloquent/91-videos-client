package euphoria.psycho.porn.tasks;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import androidx.annotation.Nullable;
import androidx.room.Room;
import euphoria.psycho.porn.Shared;

public class DownloaderService extends Service {
    public static final String KEY_DOWNLOAD_LINK = "DOWNLOAD_LINK";
    public static final String KEY_DOWNLOAD_TITLE = "DOWNLOAD_TITLE";

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        createDatabase(downloadLink, response, dir);
    }


    private void createDatabase(String downloadLink, String response, File dir) {
        String[] segments = response.split("\n");
        int sequence = 0;
        List<DownloaderTask> downloaderTasks = new ArrayList<>();
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].startsWith("#EXTINF:")) {
                String segment = segments[i + 1];
                DownloaderTask downloaderTask = new DownloaderTask();
                downloaderTask.uri = segment;
                downloaderTask.sequence = sequence++;
                downloaderTasks.add(downloaderTask);
                i++;
            }
        }
        String dbName = new File(dir, "data.db").getAbsolutePath();
        DownloaderTaskDatabase db = Room.databaseBuilder(getApplicationContext(), DownloaderTaskDatabase.class, dbName).build();
        db.downloaderTaskDao().insertAll(downloaderTasks.toArray(new DownloaderTask[0]));
        DownloaderTaskInfo downloaderTaskInfo = new DownloaderTaskInfo();
        downloaderTaskInfo.segmentSize = downloaderTasks.size();
        downloaderTaskInfo.uri = downloadLink;
        db.downloaderTaskInfoDao().insertAll(downloaderTaskInfo);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String downloadLink = intent.getStringExtra(KEY_DOWNLOAD_LINK);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    createTask(downloadLink);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

}
