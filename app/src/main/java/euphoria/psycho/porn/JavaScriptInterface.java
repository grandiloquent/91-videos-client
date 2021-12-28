package euphoria.psycho.porn;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;

public class JavaScriptInterface {
    private Activity mActivity;

    public JavaScriptInterface(Activity activity) {
        mActivity = activity;
    }

    @JavascriptInterface
    public void handleRequest(String uri) {
        Intent starter = new Intent(mActivity, WebActivity.class);
        starter.putExtra(WebActivity.EXTRA_VIDEO_URL, uri);
        mActivity.startActivity(starter);
    }
}
