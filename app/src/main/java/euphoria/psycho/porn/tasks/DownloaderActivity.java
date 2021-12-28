package euphoria.psycho.porn.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.porn.R;
import euphoria.psycho.porn.Shared;

public class DownloaderActivity extends AppCompatActivity {
    private RecyclerView mRoot;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRoot = findViewById(R.id.root);
        start(this, "https://la.killcovid2021.com//m3u8/565321/565321.m3u8?st=xT7fMkFrXyxtTh0-X7gO8Q&e=1640675341");
    }

    public static void start(Context context, String downloadLink) {
        Intent starter = new Intent(context, DownloaderService.class);
        starter.putExtra(DownloaderService.KEY_DOWNLOAD_LINK, downloadLink);
        context.startService(starter);
    }

    private void loadDirectories() {
        File external = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File[] directories = external.listFiles(File::isDirectory);
    }


}
