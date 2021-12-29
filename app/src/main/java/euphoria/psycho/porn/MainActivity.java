package euphoria.psycho.porn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import euphoria.psycho.porn.tasks.DownloaderService;

import static euphoria.psycho.porn.Shared.requestStoragePremissions;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private WebView mWebView;


    private void launchBottomSheet() {
        ModalBottomSheet modalBottomSheet = new ModalBottomSheet();
        modalBottomSheet.show(getSupportFragmentManager(), ModalBottomSheet.TAG);
    }

    private void setUpCookie() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(mWebView, true);
    }

    @SuppressLint("JavascriptInterface")
    private void setUpJavascriptInterface() {
        JavaScriptInterface javaScriptInterface = new JavaScriptInterface(this);
        mWebView.addJavascriptInterface(javaScriptInterface, "JInterface");
    }

    private void setUpWebComponents() {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setAppCacheEnabled(true);
        settings.setAppCachePath(getCacheDir().getAbsolutePath());
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestStoragePremissions(this);
        setContentView(R.layout.main_activity);
        mWebView = findViewById(R.id.web_view);
//        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
//            String fileName = URLUtil.guessFileName(url, contentDisposition, WebViewShare.getFileType(context, url));
//            WebViewShare.downloadFile(context, fileName, url, userAgent);
//        });
//        WebViewShare.setWebView(webView, Helper.createCacheDirectory(context).getAbsolutePath());
        mWebView.setWebViewClient(new CustomWebViewClient(this));
//        webView.setWebChromeClient(new CustomWebChromeClient(context));
//        webView.setDownloadListener(Helper.getDownloadListener(context));
        setUpWebComponents();
        setUpJavascriptInterface();
        setUpCookie();
        //SettingsFragment.updateCkCookie(this,"UM_distinctid=17e0691173987d-08930e62bd539a-57b1a33-e1000-17e0691173a119a; CNZZDATA1279721426=1544845544-1640786269-null%7C1640786269; 1f46f65ee0066dbf94c675090a7cea91=b9f11456e12e3065d7bc567049fbc2a5");
        mWebView.loadUrl("http://47.106.105.122");
        //start(this, "http://937ck.us/vodplay/16302-1-1.html");
    }

    public static void start(Context context, String videoAddress) {
        Intent starter = new Intent(context, DownloaderService.class);
        starter.putExtra(DownloaderService.EXTRA_VIDEO_ADDRESS, videoAddress);
        context.startService(starter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_menu) {
            launchBottomSheet();
        } else if (item.getItemId() == R.id.action_refresh) {
            mWebView.clearCache(true);
            mWebView.reload();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mWebView.loadUrl(query);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }
}