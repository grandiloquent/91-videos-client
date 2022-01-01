package euphoria.psycho.porn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoItemAdapter extends BaseAdapter {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final List<VideoItem> mVideoItems = new ArrayList<>();
    private LruCache<String, BitmapDrawable> mLruCache;
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(3);
    private Handler mHandler = new Handler();

    public VideoItemAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mLruCache = new LruCache<>(1000);
    }

    public void updateVideos(List<VideoItem> videoItems) {
        mVideoItems.clear();
        mVideoItems.addAll(videoItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mVideoItems.size();
    }

    @Override
    public VideoItem getItem(int position) {
        return mVideoItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.video_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.thumbnail = convertView.findViewById(R.id.thumbnail);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.title.setText(Shared.substringAfterLast(mVideoItems.get(position).path, "/"));
        viewHolder.thumbnail.setTag(mVideoItems.get(position).path);
        mExecutorService.submit(new Loader(mContext, viewHolder, mLruCache, mHandler));
        return convertView;
    }

    private static class Loader implements Runnable {
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
                Bitmap source = Shared.createVideoThumbnail(mPath);
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

    public static class ViewHolder {
        public TextView title;
        public ImageView thumbnail;
    }
}
