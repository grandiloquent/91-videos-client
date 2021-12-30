package euphoria.psycho.porn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;

import euphoria.psycho.porn.tasks.DownloaderService;

import static euphoria.psycho.porn.Shared.USER_AGENT;

public class JavaScriptInterface {
    private Activity mActivity;

    public JavaScriptInterface(Activity activity) {
        mActivity = activity;
    }

    @JavascriptInterface
    public void handleRequest(String uri, String id) {
        Intent starter = new Intent(mActivity, WebActivity.class);
        starter.putExtra(WebActivity.EXTRA_VIDEO_URL, uri);
        starter.putExtra("id",id);
        mActivity.startActivity(starter);
    }

    @JavascriptInterface
    public void download(String uri, String title) {
//        if (uri.contains("m3u8")) {
        Intent starter = new Intent(mActivity, DownloaderService.class);
        starter.putExtra(DownloaderService.EXTRA_VIDEO_ADDRESS, uri);
        mActivity.startService(starter);
//        } else {
//            Shared.downloadFile(mActivity, (title == null ? Shared.toHex(uri.getBytes(StandardCharsets.UTF_8)) : title) + ".mp4", uri, USER_AGENT);
//        }
        Toast.makeText(mActivity, "添加新任务：" + title, Toast.LENGTH_SHORT).show();

    }

}
