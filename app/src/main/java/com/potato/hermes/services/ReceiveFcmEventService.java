package com.potato.hermes.services;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class ReceiveFcmEventService extends FirebaseMessagingService {
    private static final String TAG = "ReceiveFcmEventService";
    public long downloadID;
    private Context context = this;
    private Uri packageUri;
    BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadID) {
                Toast.makeText(ReceiveFcmEventService.this, "Download complete", Toast.LENGTH_SHORT).show();
                installPackage();
            }
        }
    };

    public ReceiveFcmEventService() {
    }

    public void installPackage() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(packageUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getData().toString());
        downloadApkFromLink(remoteMessage.getData().get("Url"));
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void downloadApkFromLink(String url) {
        Uri uri = Uri.parse(url);
        packageUri = uri;
        DownloadManager.Request request = new DownloadManager.Request(uri);
        File direct = new File(Environment.getExternalStorageDirectory() + "/hermes");
        if (!direct.exists()) {
            direct.mkdirs();
        }
        File checkFile = new File(Environment.getExternalStorageDirectory() + "/hermes" + FilenameUtils.getName(uri.getPath()));
        if (checkFile.exists()) {
            checkFile.delete();
        }
        request.setDescription("Downloading seamless app update");
        request.setDestinationInExternalPublicDir("/hermes", FilenameUtils.getName(uri.getPath()));
        request.setTitle(FilenameUtils.getName(uri.getPath()));

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadID = manager.enqueue(request);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
