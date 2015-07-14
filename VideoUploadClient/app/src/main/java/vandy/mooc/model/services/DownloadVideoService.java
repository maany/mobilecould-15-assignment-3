package vandy.mooc.model.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import vandy.mooc.model.mediator.VideoDataMediator;
import vandy.mooc.view.VideoDetailsActivity;
import vandy.mooc.view.VideoListActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadVideoService extends IntentService {

    public  static final String ACTION_DOWNLOAD_SERVICE_RESPONSE = "vandy.mooc.services.DownloadVideoService.RESPONSE";
    /**
     * It is used by Notification Manager to send Notifications.
     */
    private static final int NOTIFICATION_ID = 2;
    private VideoDataMediator mVideoMediator;

    /**
     * Manages the Notification displayed in System UI.
     */
    private NotificationManager mNotifyManager;

    /**
     * Builder used to build the Notification.
     */
    private NotificationCompat.Builder mBuilder;

    public DownloadVideoService() {
        super("DownloadVideoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        startNotification();

        mVideoMediator =
                new VideoDataMediator();
        String response = mVideoMediator.downloadVideoData(this,intent.getStringExtra(VideoListActivity.VIDEO_SELECTION_NAME_KEY),intent.getLongExtra(VideoListActivity.VIDEO_SELECTION_ID_KEY,-1));
        // Check if Video Download is successful.

        finishNotification(response);

        // Send the Broadcast to VideoListActivity that the Video
        // download is completed.
        sendBroadcast();

    }

    private void startNotification() {
        // Gets access to the Android Notification Service.
        mNotifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the Notification and set a progress indicator for an
        // operation of indeterminate length.
        mBuilder = new NotificationCompat
                .Builder(this)
                .setContentTitle("Video Download")
                .setContentText("Download in progress")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setTicker("Downloading video")
                .setProgress(0,
                        0,
                        true);

        // Build and issue the notification.
        mNotifyManager.notify(NOTIFICATION_ID,
                mBuilder.build());
    }
    private void finishNotification(String status){
        // When the loop is finished, updates the notification.
        mBuilder.setContentTitle(status)
                // Removes the progress bar.
                .setProgress (0,
                        0,
                        false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentText("")
                .setTicker(status);

        // Build the Notification with the given
        // Notification Id.
        mNotifyManager.notify(NOTIFICATION_ID,
                mBuilder.build());
    }
    private void sendBroadcast(){
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(ACTION_DOWNLOAD_SERVICE_RESPONSE)
                        .addCategory(Intent.CATEGORY_DEFAULT));
    }
    // TODO return an intent to video ops
    public static Intent makeIntent(Context applicationContext,String videoName,long id) {
        Intent intent = new Intent(applicationContext,DownloadVideoService.class);
        intent.putExtra(VideoListActivity.VIDEO_SELECTION_ID_KEY,id).putExtra(VideoListActivity.VIDEO_SELECTION_NAME_KEY,videoName);
        return intent;
    }


}
