package euphoria.psycho.porn;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ModalBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = ModalBottomSheet.class.getSimpleName();

    private RecyclerView mRecyclerView;

    //
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.modal_bottom_sheet_content, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        List<BottomSheetItem> bottomSheetItems = new ArrayList<>();
        bottomSheetItems.add(getVideoListItem());
        bottomSheetItems.add(getSettingsItem());
        BottomSheetItemAdapter ba = new BottomSheetItemAdapter(getContext(), bottomSheetItems);
        mRecyclerView.setAdapter(ba);
        return view;
    }


    private BottomSheetItem getVideoListItem() {
        BottomSheetItem bottomSheetItem = new BottomSheetItem();
        bottomSheetItem.icon = R.drawable.ic_action_playlist_play;
        bottomSheetItem.title = "视频";
        bottomSheetItem.listener = (view1, position) -> {
            startVideoList(getContext());
        };
        return bottomSheetItem;
    }

    private BottomSheetItem getSettingsItem() {
        BottomSheetItem bottomSheetItem = new BottomSheetItem();
        bottomSheetItem.icon = R.drawable.ic_action_settings;
        bottomSheetItem.title = "设置";
        bottomSheetItem.listener = (view1, position) -> {
            Intent starter = new Intent(getContext(), SettingsActivity.class);
            getContext().startActivity(starter);
        };
        return bottomSheetItem;
    }
    private static void startVideoList(Context context) {
        Intent starter = new Intent(context, VideoListActivity.class);
        context.startActivity(starter);
    }

}

