package euphoria.psycho.porn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.PopupWindow;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.porn.tasks.DownloaderService;

import static euphoria.psycho.porn.Shared.requestStoragePremissions;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private WebView mWebView;
    private BottomSheetLayout mRoot;


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
        mRoot = findViewById(R.id.root);
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
        if (SettingsFragment.getString(this, SettingsFragment.KEY_USER_AGENT, null) == null) {
            String ua = new WebView(this).getSettings().getUserAgentString();
            SettingsFragment.setString(this, SettingsFragment.KEY_USER_AGENT, ua);
        }
        mWebView.loadUrl("http://47.106.105.122");
        //start(this, "http://937ck.us/vodplay/16302-1-1.html");
        startService(new Intent(this, DownloaderService.class));

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
            RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(this).inflate(R.layout.modal_bottom_sheet_content, null);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            List<BottomSheetItem> bottomSheetItems = new ArrayList<>();
            bottomSheetItems.add(getVideoListItem());
            bottomSheetItems.add(getSettingsItem());
            BottomSheetItemAdapter ba = new BottomSheetItemAdapter(this, bottomSheetItems);
            recyclerView.setAdapter(ba);
            mRoot.showWithSheetView(recyclerView);

        } else if (item.getItemId() == R.id.action_refresh) {
            mWebView.clearCache(true);
            mWebView.reload();
        }
        return super.onOptionsItemSelected(item);
    }

    private BottomSheetItem getVideoListItem() {
        BottomSheetItem bottomSheetItem = new BottomSheetItem();
        bottomSheetItem.icon = R.drawable.ic_action_playlist_play;
        bottomSheetItem.title = "视频";
        bottomSheetItem.listener = (view1, position) -> {
            //startVideoList(getContext());
        };
        return bottomSheetItem;
    }

    private BottomSheetItem getSettingsItem() {
        BottomSheetItem bottomSheetItem = new BottomSheetItem();
        bottomSheetItem.icon = R.drawable.ic_action_settings;
        bottomSheetItem.title = "设置";
        bottomSheetItem.listener = (view1, position) -> {
            Intent starter = new Intent(this, SettingsActivity.class);
            startActivity(starter);

        };
        return bottomSheetItem;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // https://v.douyin.com/8kSH3tK
        if (Utils.getDouYinVideo(this, query)) {
            return true;
        }
        if (Utils.getKuaiShouVideo(this, query)) {
            return true;
        }
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