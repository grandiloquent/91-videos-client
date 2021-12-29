package euphoria.psycho.porn;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
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
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.porn.VideoItemAdapter.ViewHolder;

public class VideoItemAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final List<VideoItem> mVideoItems = new ArrayList<>();
    private final LayoutInflater mInflater;
    private final Context mContext;
    private String mDirectory;

    public void setDirectory(String directory) {
        mDirectory = directory;
    }

    public VideoItemAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return mVideoItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoItem videoItem = mVideoItems.get(position);
        holder.title.setText(Shared.substringAfterLast(videoItem.path, "/"));
        Glide.with(mContext)
                .load(videoItem.path)
                .centerInside()
                .into(holder.thumbnail);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    public void updateVideos(List<VideoItem> videoItems) {
        mVideoItems.clear();
        mVideoItems.addAll(videoItems);
        notifyDataSetChanged();
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
            itemView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(mContext, title);
                    popupMenu.inflate(R.menu.video_item);
                    addDirectories(popupMenu);
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_send) {
                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(builder.build());

                            VideoItem videoItem = mVideoItems.get(getAdapterPosition());
                            File file = new File(videoItem.path);
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                            shareIntent.setType("video/*");
                            mContext.startActivity(Intent.createChooser(shareIntent, "发送视频"));
                        } else {
                            performMoveFile(item);
                        }
                        return false;
                    });
                    return true;
                }
            });
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.title);
        }

        private void performMoveFile(MenuItem item) {
            File dir = new File(mDirectory, item.getTitle().toString());
            VideoItem videoItem = mVideoItems.get(getAdapterPosition());
            File file = new File(videoItem.path);
            file.renameTo(new File(dir, Shared.substringAfterLast(videoItem.path, "/")));
            int position = getAdapterPosition();
            mVideoItems.remove(position);
            notifyItemRemoved(position);
        }

        private void addDirectories(PopupMenu popupMenu) {
            if (mDirectory == null) return;
            File dir = new File(mDirectory);
            File[] directories = dir.listFiles(File::isDirectory);
            if (directories == null) {
                return;
            }
            for (File r : directories) {
                popupMenu.getMenu().add(r.getName());
            }
        }

        @Override
        public void onClick(View view) {
            PlayerActivity.launchActivity(mContext, new File(
                    mVideoItems.get(getAdapterPosition()).path
            ));
        }
    }
}
