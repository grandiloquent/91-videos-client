package euphoria.psycho.porn;

import android.app.Activity;
import android.view.View;

public class PlayerUtils {
    public static void hideSystemUI(Activity activity) {
//        mRoot.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                View.SYSTEM_UI_FLAG_LOW_PROFILE |
//                View.SYSTEM_UI_FLAG_FULLSCREEN |
//                View.SYSTEM_UI_FLAG_IMMERSIVE);
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        activity.getActionBar().hide();
    }
}
