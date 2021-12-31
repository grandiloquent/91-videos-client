package euphoria.psycho.porn;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FileListActivity extends Activity {
    private static final String KEY_LAST_VISITED_FOLDER = "key_last_visited_folder";
    private RecyclerView mRecyclerView;
    private String mDirectory;
    private FileAdapter mFileAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.file_list_activity);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFileAdapter = new FileAdapter(this);
        mRecyclerView.setAdapter(mFileAdapter);
        mDirectory = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsFragment.KEY_VIDEO_FOLDER, getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        if (mDirectory == null) {
            mFileAdapter.setDirectory(null);
        } else
            mFileAdapter.setDirectory(new File(mDirectory));
    }

    @Override
    protected void onPause() {
        String dir = mFileAdapter.getDirectory() != null ? mFileAdapter.getDirectory().getAbsolutePath() : null;
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(KEY_LAST_VISITED_FOLDER, dir)
                .apply();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mFileAdapter.getDirectory() == null) {
            super.onBackPressed();

        } else {
            if (mFileAdapter.getDirectory().getParentFile()
                    .equals(Shared.getExternalStoragePath(this))
                    || mFileAdapter.getDirectory().getParentFile()
                    .equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                mFileAdapter.setDirectory(mFileAdapter.getDirectory().getParentFile());
            } else {
                mFileAdapter.setDirectory(null);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_check) {
            PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .edit()
                    .putString(SettingsFragment.KEY_VIDEO_FOLDER,
                            mFileAdapter.getDirectory().getAbsolutePath())
                    .apply();
            finish();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
