package euphoria.psycho.porn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.SearchView;


import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;

import euphoria.psycho.porn.Shared.Listener;
import euphoria.psycho.porn.tasks.DownloaderService;

import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static euphoria.psycho.porn.Shared.closeQuietly;
import static euphoria.psycho.porn.Shared.getExternalStoragePath;
import static euphoria.psycho.porn.Shared.requestStoragePremissions;

public class MainActivity extends Activity {

    private WebView mWebView;
    private BottomSheetLayout mRoot;

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

    public static void start(Context context, String videoAddress) {
        Intent starter = new Intent(context, DownloaderService.class);
        starter.putExtra(DownloaderService.EXTRA_VIDEO_ADDRESS, videoAddress);
        context.startService(starter);
    }

    private static void startVideoList(Context context) {
        Intent starter = new Intent(context, VideoListActivity.class);
        context.startActivity(starter);
    }

    private void askUpdate(VersionInfo versionInfo) {
        AlertDialog dialog = new Builder(this)
                .setTitle("询问")
                .setMessage("程序有新版本是否更新？")
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    performUpdate(versionInfo);
                }).setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void checkUpdate() {
        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            VersionInfo versionInfo = getVersionInformation();
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                int version = pInfo.versionCode;
                if (versionInfo.versionCode > version) {
                    runOnUiThread(() -> askUpdate(versionInfo));
                }
            } catch (Exception e) {
                Log.e("B5aOx2", String.format("checkUpdate, %s", e.getMessage()));
            }
        }).start();

    }

    private VersionInfo getVersionInformation() {
        VersionInfo versionInfo = new VersionInfo();
        try {
            String response = Shared.getString("https://www.hxz315.com/version.json", null, false, false).contents;
            JSONObject object = new JSONObject(response);
            object = object.getJSONObject("Secret Garden");
            versionInfo.versionCode = object.getInt("VersionCode");
            versionInfo.downloadLink = object.getString("DownloadLink");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionInfo;
    }

    private void performUpdate(VersionInfo versionInfo) {
        File f = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "HuaYuan.apk");
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("下载中...");
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                HttpsURLConnection c = null;
                try {
                    c = (HttpsURLConnection) new URL(versionInfo.downloadLink).openConnection();
                    FileOutputStream fos = new FileOutputStream(
                            f
                    );
                    Shared.copy(c.getInputStream(), fos);
                    closeQuietly(fos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Shared.installPackage(MainActivity.this, f);
                    }
                });
            }
        }).start();
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
        requestStoragePremissions(this, false);
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
        checkUpdate();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_menu).setShowAsAction(SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.action_refresh).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_menu) {
            GridView gridView = (GridView) LayoutInflater.from(this).inflate(R.layout.modal_bottom_sheet_content, null);
            gridView.setNumColumns(3);
            List<BottomSheetItem> bottomSheetItems = new ArrayList<>();
            int[][] items = new int[][]{
                    new int[]{R.drawable.ic_action_search, R.string.search},
                    new int[]{R.drawable.ic_action_playlist_play, R.string.video},
                    new int[]{R.drawable.ic_action_settings, R.string.set_up},
                    new int[]{R.drawable.ic_action_help_outline, R.string.help}
            };
            for (int[] ints : items) {
                BottomSheetItem bottomSheetItem = new BottomSheetItem();
                bottomSheetItem.title = getString(ints[1]);
                bottomSheetItem.icon = ints[0];
                bottomSheetItems.add(bottomSheetItem);
            }
            BottomSheetItemAdapter ba = new BottomSheetItemAdapter(this, bottomSheetItems);
            gridView.setAdapter(ba);
            gridView.setOnItemClickListener((parent, view, position, id) -> {
                if (position == 0) {
                    Shared.openTextContentDialog(MainActivity.this,
                            getString(R.string.search),
                            this::onQueryTextSubmit
                    );
                }
                if (position == 1) {
                    startVideoList(MainActivity.this);
                } else if (position == 2) {
                    Intent starter = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(starter);
                } else {
                    mWebView.loadUrl("https://lucidu.cn/article/hhoplt");
                }
                mRoot.dismissSheet();
            });
            mRoot.showWithSheetView(gridView);
        } else if (item.getItemId() == R.id.action_refresh) {
            mWebView.clearCache(true);
            mWebView.reload();
        }
        return super.onOptionsItemSelected(item);
    }
}