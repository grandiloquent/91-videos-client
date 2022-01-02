package euphoria.psycho.porn;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.io.File;
import java.util.Objects;

public class FileListActivity extends Activity {
    private static final String KEY_LAST_VISITED_FOLDER = "key_last_visited_folder";
    private String mDirectory;
    private FileAdapter mFileAdapter;

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getActionBar()).setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.file_list_activity);
        mListView = findViewById(R.id.recycler_view);
        mFileAdapter = new FileAdapter(this);
        mListView.setAdapter(mFileAdapter);
        mDirectory = SettingsFragment.getString(this,
                SettingsFragment.KEY_VIDEO_FOLDER, getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        if (mDirectory == null) {
            mFileAdapter.setDirectory(null);
        } else
            mFileAdapter.setDirectory(new File(mDirectory));
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            File file = mFileAdapter.getItem(position);
            if (file.isDirectory())
                mFileAdapter.setDirectory(file);
            else
                PlayerActivity.launchActivity(FileListActivity.this, file);
        });
    }


    @Override
    protected void onPause() {
        String dir = mFileAdapter.getDirectory() != null ? mFileAdapter.getDirectory().getAbsolutePath() : null;
        SettingsFragment.setString(this, KEY_LAST_VISITED_FOLDER, dir);
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
        menu.findItem(R.id.action_check).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_check) {
            SettingsFragment
                    .setString(this, SettingsFragment.KEY_VIDEO_FOLDER,
                            mFileAdapter.getDirectory().getAbsolutePath());
            finish();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
