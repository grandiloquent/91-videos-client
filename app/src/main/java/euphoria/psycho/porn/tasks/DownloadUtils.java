package euphoria.psycho.porn.tasks;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Process;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import androidx.annotation.RequiresApi;
import euphoria.psycho.porn.Shared;

public class DownloadUtils {
    @RequiresApi(api = VERSION_CODES.O)
    public static void createNotificationChannel(Context context, String id, CharSequence name) {
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
        context.getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);
    }

    public static String getString(String uri) throws IOException {
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

    public static void background(Callable<Void> callable) {
        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
