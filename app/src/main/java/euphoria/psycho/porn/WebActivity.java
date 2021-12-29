package euphoria.psycho.porn;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import android.os.Process;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WebActivity extends AppCompatActivity {
    public static final String EXTRA_VIDEO_URL = "extra_video_url";
    private WebView mWebView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity);
        mWebView = findViewById(R.id.web_view);
        mWebView.clearCache(true);
        JavaInterface javaInterface = new JavaInterface();
        mWebView.addJavascriptInterface(javaInterface, "JInterface");
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String videoUri = getIntent().getStringExtra(EXTRA_VIDEO_URL);
                if (videoUri == null) {
                    return;
                }
                javaInterface.parse(videoUri);
            }

            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
//                    Date date = new Date();
//                    final String dateString = FORMATTER.format(date);
//                    mHeaders.put("Date", dateString + " GMT");
//                    return new WebResourceResponse("text/plain", "UTF-8", 200, "OK", mHeaders, null);
//                }
                // request.getRequestHeaders().put("Access-Control-Allow-Origin", "*");
                return null;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            private View mCustomView;
            private WebChromeClient.CustomViewCallback mCustomViewCallback;
            private int mOriginalOrientation;
            private int mOriginalSystemUiVisibility;

            @Override
            public Bitmap getDefaultVideoPoster() {
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return super.onConsoleMessage(consoleMessage);
            }

            public void onHideCustomView() {
                ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
                this.mCustomView = null;
                getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
                setRequestedOrientation(this.mOriginalOrientation);
                this.mCustomViewCallback.onCustomViewHidden();
                this.mCustomViewCallback = null;
            }

            @Override
            public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
                if (this.mCustomView != null) {
                    onHideCustomView();
                    return;
                }
                this.mCustomView = paramView;
                this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                this.mOriginalOrientation = getRequestedOrientation();
                this.mCustomViewCallback = paramCustomViewCallback;
                ((FrameLayout) getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            }
        });
        mWebView.loadUrl("http://47.106.105.122/video.html");
    }

    public static Pair<String, String> process91Porn(String videoAddress) {
        String response = Native.fetch91Porn(Uri.parse(videoAddress).getQueryParameter("viewkey"));
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            String title = jsonObject.getString("title");
            String src = jsonObject.getString("videoUri");
            return Pair.create(title, src);
        } catch (JSONException ignored) {
        }
        return null;
    }

    public static Pair<String, String> processCk(Context context, String videoAddress) {
        String response = Native.fetchCk(videoAddress, SettingsFragment.getCkCookie(context, null));
        if (response == null) {
            return null;
        }
        String title = Shared.substringBefore(response, "\n").trim();
        String src = Shared.substringAfter(response, "\n")
                .replaceAll("\\\\", "");
        return Pair.create(title, src);
    }

    private class JavaInterface {

        @JavascriptInterface
        public void parse(String uri) {
            new Thread(() -> {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                String[] videoUris = null;
                Pair<String, String> results;
                if (uri.contains("91porn.com")) {
                    results = process91Porn(uri);
                } else {
                    results = process91Porn(uri);
                }
                if (results != null) {
                    videoUris = new String[]{results.first, results.second};
                }
//                else if (uri.contains("xvideos.com")) {
//                    videoUris = Native.fetchXVideos(uri);
//                } else {
//                    videoUris = Native.fetch57Ck(uri);
//                }
                String[] finalVideoUris = videoUris;
                runOnUiThread(() -> {
                    if (finalVideoUris == null) {
                        Toast.makeText(WebActivity.this, "无法解析视频", Toast.LENGTH_LONG).show();
                        return;
                    }
                    JSONObject obj = new JSONObject();
                    try {
                        JSONArray jsonArray = new JSONArray();
                        jsonArray.put(finalVideoUris[1]);
                        obj.put("title", finalVideoUris[0]);
                        obj.put("videos", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mWebView.evaluateJavascript("start('" + obj.toString() + "')", null);
                });
            }).start();
        }
    }
}
