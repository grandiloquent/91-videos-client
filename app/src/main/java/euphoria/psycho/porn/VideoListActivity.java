package euphoria.psycho.porn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import euphoria.psycho.porn.Shared.Listener;

// FileListActivity
public class VideoListActivity extends Activity {
    private static final String KEY_FAVORITES_LIST = "key_favorites_list";
    private GridView mGridView;
    private VideoItemAdapter mVideoItemAdapter;
    private String mDirectory;

    private void initialize() {
        mDirectory = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsFragment.KEY_VIDEO_FOLDER, getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        loadFolder();
    }

    // PlayerActivity
    private void loadFolder() {
        File dir = new File(mDirectory);
        File[] videos = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".mp4");
            }
        });
        if (videos == null) {
            return;
        }
        Arrays.sort(videos, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                final long result = o2.lastModified() - o1.lastModified();
                if (result < 0) {
                    return -1;
                }
                if (result > 0) {
                    return 1;
                }
                return 0;
            }
        });
        List<VideoItem> videoItems = new ArrayList<>();
        for (File video : videos) {
            VideoItem videoItem = new VideoItem();
            videoItem.path = video.getAbsolutePath();
            videoItems.add(videoItem);
        }
        mVideoItemAdapter.updateVideos(videoItems);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initialize();
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_list_activity);
        mGridView = findViewById(R.id.recycler_view);
        mGridView.setNumColumns(2);
        registerForContextMenu(mGridView);
        mVideoItemAdapter = new VideoItemAdapter(this);
        mGridView.setAdapter(mVideoItemAdapter);
       // getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().show();
        getActionBar().setDisplayHomeAsUpEnabled(true);

        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDirectory != null)
            loadFolder();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        if (item.getItemId() == R.id.action_selector) {
            Intent starter = new Intent(this, FileListActivity.class);
            startActivityForResult(starter, 0);
        } else if (item.getItemId() == R.id.action_fav) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            Set<String> strings = preferences.getStringSet(KEY_FAVORITES_LIST, new HashSet<>());
            Set<String> newStrings = new HashSet<>(strings);
            newStrings.add(mDirectory);
            preferences.edit().putStringSet(KEY_FAVORITES_LIST, newStrings).apply();
        } else if (item.getItemId() == R.id.action_favorite_border) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            Set<String> strings = preferences.getStringSet(KEY_FAVORITES_LIST, new HashSet<>());
            Set<String> newStrings = new HashSet<>(strings);
            newStrings.remove(mDirectory);
            preferences.edit().putStringSet(KEY_FAVORITES_LIST, newStrings).apply();
        } else if (item.getItemId() == R.id.action_bookmark) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String[] strings = preferences.getStringSet(KEY_FAVORITES_LIST, new HashSet<>()).toArray(new String[0]);
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(strings, (dialog, which) -> {
                mDirectory = strings[which];
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putString(SettingsFragment.KEY_VIDEO_FOLDER, mDirectory)
                        .apply();
                loadFolder();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_video) {
            mDirectory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString(SettingsFragment.KEY_VIDEO_FOLDER, mDirectory)
                    .apply();
            loadFolder();
        } else if (item.getItemId() == R.id.action_create_directory) {
            Shared.openTextContentDialog(this, "创建目录", new Listener() {
                @Override
                public void onSuccess(String value) {
                    File dir = new File(mDirectory, value.trim());
                    if (!dir.isDirectory())
                        dir.mkdir();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }



}
