package euphoria.psycho.porn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class BottomSheetItemAdapter {
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
