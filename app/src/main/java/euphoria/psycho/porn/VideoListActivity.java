package euphoria.psycho.porn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import euphoria.psycho.porn.Shared.Listener;

public class VideoListActivity extends Activity {
    private static final String KEY_FAVORITES_LIST = "key_favorites_list";
    private GridView mGridView;
    private VideoItemAdapter mVideoItemAdapter;
    private String mDirectory;

    private void addBookmark() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> strings = preferences.getStringSet(KEY_FAVORITES_LIST, new HashSet<>());
        Set<String> newStrings = new HashSet<>(strings);
        newStrings.add(mDirectory);
        preferences.edit().putStringSet(KEY_FAVORITES_LIST, newStrings).apply();
    }

    private void createFileDirectory() {
        Shared.openTextContentDialog(this, getString(R.string.create_a_directory), new Listener() {
            @Override
            public void onSuccess(String value) {
                File dir = new File(mDirectory, value.trim());
                if (!dir.isDirectory())
                    dir.mkdir();
            }
        });
    }

    private void deleteBookmark() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> strings = preferences.getStringSet(KEY_FAVORITES_LIST, new HashSet<>());
        Set<String> newStrings = new HashSet<>(strings);
        newStrings.remove(mDirectory);
        preferences.edit().putStringSet(KEY_FAVORITES_LIST, newStrings).apply();
    }

    private String getDefaultPath() {
        return getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    private void initialize() {
        mDirectory = SettingsFragment.getString(this, SettingsFragment.KEY_VIDEO_FOLDER, getDefaultPath());
        loadFolder();
    }

    private void loadDirectory() {
        SettingsFragment.setString(this, SettingsFragment.KEY_VIDEO_FOLDER, mDirectory);
        loadFolder();
    }

    private void loadFolder() {
        File dir = new File(mDirectory);
        File[] videos = dir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".mp4"));
        if (videos == null) {
            return;
        }
        Arrays.sort(videos, (o1, o2) -> {
            final long result = o2.lastModified() - o1.lastModified();
            if (result < 0) {
                return -1;
            }
            if (result > 0) {
                return 1;
            }
            return 0;
        });
        List<VideoItem> videoItems = new ArrayList<>();
        for (File video : videos) {
            VideoItem videoItem = new VideoItem();
            videoItem.path = video.getAbsolutePath();
            videoItems.add(videoItem);
        }
        mVideoItemAdapter.updateVideos(videoItems);
    }

    private void showBookmarks() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String[] strings = preferences.getStringSet(KEY_FAVORITES_LIST, new HashSet<>()).toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(strings, (dialog, which) -> {
            mDirectory = strings[which];
            loadDirectory();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initialize();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_list_activity);
        mGridView = findViewById(R.id.recycler_view);
        mGridView.setNumColumns(2);
        registerForContextMenu(mGridView);
        mVideoItemAdapter = new VideoItemAdapter(this);
        mGridView.setAdapter(mVideoItemAdapter);
        mGridView.setOnItemClickListener((parent, view, position, id) -> PlayerActivity.launchActivity(view.getContext(), new File(
                mVideoItemAdapter.getItem(position).path
        )));
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
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo contextMenuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        VideoItem videoItem = mVideoItemAdapter.getItem(contextMenuInfo.position);
        if (item.getItemId() == 0) {
            File file = new File(videoItem.path);
            Shared.shareFile(this, file, getString(R.string.send_video));
        } else {
            File dir = new File(mDirectory, item.getTitle().toString());
            File f = new File(videoItem.path);
            f.renameTo(new File(dir, f.getName()));
            loadFolder();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(0, 0, 0, R.string.share);
        File[] directories = new File(mDirectory).listFiles(File::isDirectory);
        if (directories != null) {
            for (int i = 0; i < directories.length; i++) {
                menu.add(0, i + 1, 0, directories[i].getName());
            }
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_list, menu);
        menu.findItem(R.id.action_selector).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.findItem(R.id.action_bookmark).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_selector) {
            Intent starter = new Intent(this, FileListActivity.class);
            startActivityForResult(starter, 0);
        } else if (item.getItemId() == R.id.action_fav) {
            addBookmark();
        } else if (item.getItemId() == R.id.action_favorite_border) {
            deleteBookmark();
        } else if (item.getItemId() == R.id.action_bookmark) {
            showBookmarks();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_video) {
            mDirectory = getDefaultPath();
            loadDirectory();
        } else if (item.getItemId() == R.id.action_create_directory) {
            createFileDirectory();
        }
        return super.onOptionsItemSelected(item);
    }


}
