package euphoria.psycho.porn;

import android.content.Context;
import android.content.Loader;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import euphoria.psycho.porn.VideoItemAdapter.ViewHolder;

public class FileAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<File> mFiles = new ArrayList<>();
    private final LayoutInflater mInflater;
    private File mDirectory;


    public FileAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public File getDirectory() {
        return mDirectory;
    }

    public void setDirectory(File directory) {
        mDirectory = directory;
        mFiles.clear();
        if (directory == null) {
            mFiles.add(Environment.getExternalStorageDirectory());
            String storage = Shared.getExternalStoragePath(mContext);
            if (storage != null) {
                mFiles.add(new File(storage));
            }
        } else {
            File[] files = mDirectory.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith(".mp4"));
            if (files != null) {
                Arrays.sort(files, (o1, o2) -> {
                    if (o1.isDirectory() && o2.isFile()) {
                        return 1;
                    } else if (o1.isFile() && o2.isDirectory()) {
                        return 0;
                    } else {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                for (File f : files) {
                    mFiles.add(f);
                }
            }

        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFiles.size();
    }

    @Override
    public File getItem(int position) {
        return mFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VideoItemAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.file_item, parent, false);
            viewHolder = new VideoItemAdapter.ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.thumbnail = convertView.findViewById(R.id.thumbnail);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (VideoItemAdapter.ViewHolder) convertView.getTag();
        }
        File file = mFiles.get(position);
        viewHolder.title.setText(file.getName());
        if (file.isDirectory())
            viewHolder.thumbnail.setBackgroundResource(R.drawable.ic_action_folder);
        else
            viewHolder.thumbnail.setBackgroundResource(R.drawable.ic_action_videocam);
        return convertView;
    }


    public class ViewHolder {
        TextView title;
        ImageView thumbnail;
    }


}
