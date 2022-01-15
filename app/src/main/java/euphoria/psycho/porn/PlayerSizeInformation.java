package euphoria.psycho.porn;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

public class PlayerSizeInformation {
    private final int actionBarHeight;
    private final int navigationBarLanscapeHeight;
    private final int navigationBarPortraitHeight;

    private int videoHeight;
    private int videoWidth;
    private int availableWidth;
    private int availableHeight;
    private int bottomBarHeight;
    private int timeBarHeight;


    public PlayerSizeInformation(Context context, View root, View bottomBar, View timeBar) {
        navigationBarPortraitHeight = Shared.getNavigationBarHeight(context, Configuration.ORIENTATION_PORTRAIT);
        navigationBarLanscapeHeight = Shared.getNavigationBarHeight(context, 2);

        actionBarHeight = Shared.getActionBarHeight(context);
        timeBarHeight = timeBar.getMeasuredHeight();
        bottomBarHeight = bottomBar.getMeasuredHeight();
        availableHeight = root.getMeasuredHeight();
        availableWidth = root.getMeasuredWidth();
        // context.getResources().getConfiguration().orientation
    }

    public int getNavigationBarLanscapeHeight() {
        return navigationBarLanscapeHeight;
    }

    public int getPortraitHeight() {
        return availableHeight - navigationBarPortraitHeight - actionBarHeight - bottomBarHeight
                - timeBarHeight;
    }


    public int getLandscapeHeight() {
        return availableWidth - actionBarHeight;// - bottomBarHeight - timeBarHeight;
    }

    public int getActionBarHeight() {
        return actionBarHeight;
    }


    public int getAvailableHeight() {
        return availableHeight;
    }


    public int getAvailableWidth() {
        return availableWidth;
    }


    public int getBottomBarHeight() {
        return bottomBarHeight;
    }

    public PlayerSizeInformation setBottomBarHeight(int bottomBarHeight) {
        this.bottomBarHeight = bottomBarHeight;
        return this;
    }

    public int getNavigationBarPortraitHeight() {
        return navigationBarPortraitHeight;
    }


    public int getTimeBarHeight() {
        return timeBarHeight;
    }

    public PlayerSizeInformation setTimeBarHeight(int timeBarHeight) {
        this.timeBarHeight = timeBarHeight;
        return this;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public PlayerSizeInformation setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
        return this;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public PlayerSizeInformation setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
        return this;
    }

    @Override
    public String toString() {
        return "PlayerSizeInformation{" +
                "actionBarHeight=" + actionBarHeight +
                ", navigationBarHeight=" + navigationBarPortraitHeight +
                ", videoHeight=" + videoHeight +
                ", videoWidth=" + videoWidth +
                ", availableWidth=" + availableWidth +
                ", availableHeight=" + availableHeight +
                ", bottomBarHeight=" + bottomBarHeight +
                ", timeBarHeight=" + timeBarHeight +
                '}';
    }
}
				                    