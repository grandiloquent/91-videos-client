package euphoria.psycho.porn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;


import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import euphoria.psycho.porn.VideoDatabase.Video;
import euphoria.psycho.porn.tasks.DownloaderService;

import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static euphoria.psycho.porn.Shared.closeQuietly;
import static euphoria.psycho.porn.Shared.requestStoragePremissions;

public class MainActivity extends Activity {


    private BottomSheetLayout mRoot;

    public boolean onQueryTextSubmit(String query) {
        // https://v.douyin.com/8kSH3tK
        if (Utils.getDouYinVideo(this, query)) {
            return true;
        }
        if (Utils.getKuaiShouVideo(this, query)) {
            return true;
        }
        return true;
    }

    public static void start(Context context, String videoAddress) {
        Intent starter = new Intent(context, DownloaderService.class);
        starter.putExtra(DownloaderService.EXTRA_VIDEO_ADDRESS, videoAddress);
        context.startService(starter);
    }


    private static List<Video> scrap91Porn(int page) throws Exception {
        String home = "http://91porn.com/index.php";
        if (page != 0) {
            home = "http://91porn.com/v.php?page=" + (page + 1);
        }
        HttpURLConnection u = (HttpURLConnection) new URL(home).openConnection();
        u.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        u.addRequestProperty("Accept-Encoding", "gzip, deflate");
        u.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
        u.addRequestProperty("Cache-Control", "no-cache");
        u.addRequestProperty("Cookie", "__utma=50351329.307729816.1658566123.1658566123.1658566123.1; __utmz=50351329.1658566123.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); CLIPSHARE=4vdjcomctii5oojl9roe566umu");
        u.addRequestProperty("Host", "91porn.com");
        u.addRequestProperty("Pragma", "no-cache");
        u.addRequestProperty("Proxy-Connection", "keep-alive");
        u.addRequestProperty("Upgrade-Insecure-Requests", "1");
        u.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
        if (u.getResponseCode() != 200) {
            throw new IllegalStateException(Integer.toString(u.getResponseCode()));
        }
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(u.getInputStream())));
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append('\n');
        }
        Document document = Jsoup.parse(stringBuilder.toString());
        Elements videos = document.select(".videos-text-align");
        ZoneId zoneId = ZoneId.systemDefault();
        Pattern pattern = Pattern.compile("\\s+?(\\d+)\\s+?小*?([时天年])\\s+?前");
        List<Video> vs = new ArrayList<>();
        for (Element v : videos) {
            Video video = new Video();
            video.Title = v.select(".thumb-overlay + span").text();
            video.Thumbnail = v.select(".thumb-overlay img").attr("src");
            try {
                video.Duration = toSeconds(v.select(".thumb-overlay .duration").text());
            } catch (Exception e) {
                video.Duration = 0;
            }
            LocalDate localDate = LocalDate.now();
            Matcher matcher = pattern.matcher(v.html());
            if (matcher.find()) {
                if (matcher.group(2).equals("天")) {
                    localDate = localDate.minusDays(Integer.parseInt(matcher.group(1)));
                } else if (matcher.group(2).equals("年")) {
                    localDate = localDate.minusYears(Integer.parseInt(matcher.group(1)));
                }
            }
            video.CreateAt = localDate.atStartOfDay(zoneId).toEpochSecond();
            video.Url = v.select("a").attr("href");
            video.VideoType = 1;
            vs.add(video);
        }
        return vs;
    }

    private static void startVideoList(Context context) {
        Intent starter = new Intent(context, VideoListActivity.class);
        context.startActivity(starter);
    }

    private static int toSeconds(String duration) {
        String[] pieces = duration.split(":");
        int total = 0;
        int max = pieces.length - 1;
        int j = max;
        for (int i = 0; i < max; i++) {
            total += Integer.parseInt(pieces[i]) * Math.pow(60, j);
            j--;
        }
        return total;
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
        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            HttpsURLConnection c;
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
        }).start();
    }

    private VideoDatabase mVideoDatabase;

    private ListView mListView;
    private VideosAdapter mVideosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(() -> {
            try {
                for (int i = 1; i < 10; i++) {
                    List<Video> videos = scrap91Porn(i);
                    mVideoDatabase.insertVideos(videos);
                }
            } catch (Exception e) {
                Log.e("B5aOx2", String.format("onCreate, %s", e.getMessage()));
            }
        }).start();
        requestStoragePremissions(this, false);
        setContentView(R.layout.main_activity);
        mVideoDatabase = new VideoDatabase(this, new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "videos.db").getAbsolutePath());
        mListView = findViewById(R.id.list_view);
        mVideosAdapter = new VideosAdapter();
        mListView.setAdapter(mVideosAdapter);
        mVideosAdapter.update(mVideoDatabase.queryVideos());
        mRoot = findViewById(R.id.root);
        if (SettingsFragment.getString(this, SettingsFragment.KEY_USER_AGENT, null) == null) {
            String ua = new WebView(this).getSettings().getUserAgentString();
            SettingsFragment.setString(this, SettingsFragment.KEY_USER_AGENT, ua);
        }
        startService(new Intent(this, DownloaderService.class));
        // checkUpdate();
//        if (VERSION.SDK_INT >= VERSION_CODES.O) {
//            try {
//                PlayerActivity.launchActivity(this,
//                        Files.list(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toPath())
//                                .findFirst().get().toFile(),1
//                );
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new Thread(() -> {
                    Video video = mVideosAdapter.getItem(position);
                    String source = null;
                    Video old = mVideoDatabase.queryVideoSource(video.Id);
                    if (old.Source == null) {
                        Pair<String, String> videos = WebActivity.process91Porn(view.getContext(), video.Url);
                        source = videos.second;
                        if (source != null) {
                            mVideoDatabase.updateVideoSource(video.Id, source);
                        }
                    } else {
                        source = old.Source;
                    }
                    if (source != null)
                        PlayerActivity.launchActivity(view.getContext(), source);
                }).start();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_menu).setShowAsAction(SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.action_refresh).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        MenuItem menuItem = menu.add(0, 0, 0, "搜索");
        Drawable drawable = menuItem.getIcon();
        if (drawable != null) {
            // If we don't mutate the drawable, then all drawable's with this id will have a color
            // filter applied to it.
            drawable.mutate();
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            drawable.setAlpha(255);
        }
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        SearchView searchView = new SearchView(this);
        searchView.setIconified(true);
        LinearLayout linearLayout = ((LinearLayout) searchView.getChildAt(0));
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            if (linearLayout.getChildAt(i) instanceof ImageView) {
                ((ImageView) linearLayout.getChildAt(i))
                        .setColorFilter(Color.WHITE,
                                android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        });
        try {
            Field d = SearchView.class.getDeclaredField("mSearchHintIcon");
            d.setAccessible(true);
            Drawable db = (Drawable) d.get(searchView);
            db.setColorFilter(Color.WHITE,
                    android.graphics.PorterDuff.Mode.SRC_IN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ImageView searchClose = ((ImageView) searchView.findViewById(searchView.getContext().getResources().getIdentifier(
                "android:id/search_close_btn",
                null, null
        )));
        if (searchClose != null)
            searchClose.setColorFilter(Color.WHITE,
                    android.graphics.PorterDuff.Mode.SRC_IN);
        menuItem.setActionView(searchView);
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
                } //else {
                // mWebView.loadUrl("https://hxz315.com");
                //}
                mRoot.dismissSheet();
            });
            mRoot.showWithSheetView(gridView);
        } else if (item.getItemId() == R.id.action_refresh) {
        }
        return super.onOptionsItemSelected(item);
    }

    private class VideosAdapter extends BaseAdapter {
        private List<Video> mVideos = new ArrayList<>();
        private LruCache<String, BitmapDrawable> mLruCache = new LruCache<>(1000);
        ;
        private ExecutorService mExecutorService = Executors.newFixedThreadPool(3);
        private Handler mHandler = new Handler();

        @Override
        public int getCount() {
            return mVideos.size();
        }

        public void update(List<Video> videos) {
            mVideos.clear();
            mVideos.addAll(videos);
            notifyDataSetChanged();
        }

        @Override
        public Video getItem(int position) {
            return mVideos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.thumbnail = convertView.findViewById(R.id.thumbnail);
                viewHolder.title = convertView.findViewById(R.id.title);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.title.setText(mVideos.get(position).Title);
            viewHolder.thumbnail.setTag(mVideos.get(position).Thumbnail);
            mExecutorService.submit(new Loader(MainActivity.this, viewHolder, mLruCache, mHandler));
            return convertView;
        }
    }

    public class Loader implements Runnable {
        private ViewHolder mViewHolder;
        private String mPath;
        private int mSize;
        private File mDirectory;
        private LruCache<String, BitmapDrawable> mLruCache;
        private final Handler mHandler;

        public Loader(Context context, ViewHolder viewHolder, LruCache<String, BitmapDrawable> lruCache, Handler handler) {
            mViewHolder = viewHolder;
            mPath = viewHolder.thumbnail.getTag().toString();
            mSize = context.getResources().getDisplayMetrics().widthPixels / 2;
            mDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            mLruCache = lruCache;
            mHandler = handler;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (mLruCache.get(mPath) != null) {
                mHandler.post(() -> mViewHolder.thumbnail.setBackground(mLruCache.get(mPath)));
                return;
            }
            Bitmap bitmap = null;
            File image = new File(mDirectory, Shared.md5(mPath));
            if (image.exists()) {
                bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            }
            if (bitmap == null) {
                Bitmap source = null;
                try {
                    source=BitmapFactory.decodeStream(new URL(mPath).openConnection().getInputStream());
                }catch (Exception e){

                }
                if (source == null) return;
                bitmap = Shared.resizeAndCropCenter(source, mSize, true);
                try {
                    FileOutputStream fos = new FileOutputStream(image);
                    bitmap.compress(CompressFormat.JPEG, 80, fos);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mViewHolder.thumbnail.getTag().toString().equals(mPath)) {
                BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                mLruCache.put(mPath, bitmapDrawable);
                mHandler.post(() -> mViewHolder.thumbnail.setBackground(bitmapDrawable));
            }

        }
    }

    private class ViewHolder {
        ImageView thumbnail;
        TextView title;
    }
}