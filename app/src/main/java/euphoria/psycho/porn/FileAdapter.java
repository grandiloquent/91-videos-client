package euphoria.psycho.porn;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.porn.FileAdapter.ViewHolder;

public class FileAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final Context mContext;
    private final List<File> mFiles = new ArrayList<>();
    private final LayoutInflater mInflater;
    private File mDirectory;

    public FileAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
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
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.isDirectory() && o2.isFile()) {
                            return 1;
                        } else if (o1.isFile() && o2.isDirectory()) {
                            return 0;
                        } else {
                            return o1.getName().compareTo(o2.getName());
                        }
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
    public int getItemCount() {
        return mFiles.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = mFiles.get(position);
        holder.title.setText(file.getName());
        if (file.isDirectory())
            holder.thumbnail.setBackgroundResource(R.drawable.ic_action_folder);
        else
            holder.thumbnail.setBackgroundResource(R.drawable.ic_action_videocam);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.file_item, parent, false);
        return new ViewHolder(view);
    }

    public File getDirectory() {
        return mDirectory;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemClickListener mClickListener;
        TextView title;
        ImageView thumbnail;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.title);

        }


        @Override
        public void onClick(View view) {
            File file = mFiles.get(getAdapterPosition());
            if (file.isDirectory())
                setDirectory(file);
            else
                PlayerActivity.launchActivity(mContext, file);
        }
    }


}
