package euphoria.psycho.porn;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.android.material.dialog.MaterialDialogs;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// FileListActivity
public class VideoListActivity extends AppCompatActivity {
    private static final String KEY_FAVORITES_LIST = "key_favorites_list";
    private RecyclerView mRecyclerView;
    private VideoItemAdapter mVideoItemAdapter;
    private String mDirectory;

    private void initialize() {
        mDirectory = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsFragment.KEY_VIDEO_FOLDER, getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        mVideoItemAdapter.setDirectory(mDirectory);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initialize();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_list_activity);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mVideoItemAdapter = new VideoItemAdapter(this);
        mRecyclerView.setAdapter(mVideoItemAdapter);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(Shared.dpToPx(this, 12)));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
                mVideoItemAdapter.setDirectory(mDirectory);
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
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildPosition(view) == 0)
                outRect.top = space;
        }
    }

}
