package euphoria.psycho.porn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class BottomSheetItemAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<BottomSheetItem> mBottomSheetItems;
    private final LayoutInflater mInflater;

    public BottomSheetItemAdapter(Context context, List<BottomSheetItem> files) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mBottomSheetItems = files;
    }


    @Override
    public int getCount() {
        return mBottomSheetItems.size();
    }

    @Override
    public BottomSheetItem getItem(int position) {
        return mBottomSheetItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VideoItemAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.bottom_sheet_grid_item, parent, false);
            viewHolder = new VideoItemAdapter.ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.thumbnail = convertView.findViewById(R.id.thumbnail);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (VideoItemAdapter.ViewHolder) convertView.getTag();
        }
        BottomSheetItem file = mBottomSheetItems.get(position);
        viewHolder.title.setText(file.title);
        viewHolder.thumbnail.setBackgroundResource(file.icon);
        return convertView;
    }


    public class ViewHolder {
        TextView title;
        ImageView thumbnail;
    }
//    private final List<BottomSheetItem> mBottomSheetItems;
//    private final LayoutInflater mInflater;
//
//    public BottomSheetItemAdapter(Context context, List<BottomSheetItem> bottomSheetItems) {
//        this.mInflater = LayoutInflater.from(context);
//        mBottomSheetItems = bottomSheetItems;
//    }
//
//    @Override
//    public int getItemCount() {
//        return mBottomSheetItems.size();
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        BottomSheetItem bottomSheetItem = mBottomSheetItems.get(position);
//        holder.icon.setBackgroundResource(bottomSheetItem.icon);
//        holder.title.setText(bottomSheetItem.title);
//        holder.setClickListener(bottomSheetItem.listener);
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = mInflater.inflate(R.layout.bottom_sheet_grid_item, parent, false);
//        return new ViewHolder(view);
//    }
//
//    public interface ItemClickListener {
//        void onItemClick(View view, int position);
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        private ItemClickListener mClickListener;
//        TextView title;
//        ImageView icon;
//
//        ViewHolder(View itemView) {
//            super(itemView);
//            itemView.setOnClickListener(this);
//            icon = itemView.findViewById(R.id.icon);
//            title = itemView.findViewById(R.id.title);
//        }
//
//        public void setClickListener(ItemClickListener clickListener) {
//            mClickListener = clickListener;
//        }
//
//        @Override
//        public void onClick(View view) {
//            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
//        }
//    }
}
