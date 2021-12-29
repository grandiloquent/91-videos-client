package euphoria.psycho.porn;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.util.Arrays;


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
        String cookie;
        if (url.contains("vodplay") && (cookie = CookieManager.getInstance().getCookie(url)) != null)
            SettingsFragment.updateCkCookie(mContext, cookie);
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