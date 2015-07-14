package vandy.mooc.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.VideoView;


import android.net.Uri;

import vandy.mooc.R;
import vandy.mooc.common.GenericActivity;
import vandy.mooc.model.mediator.webdata.Video;
import vandy.mooc.model.services.DownloadVideoService;
import vandy.mooc.model.services.UploadVideoService;
import vandy.mooc.presenter.VideoOps;
import vandy.mooc.utils.VideoMediaStoreUtils;
import vandy.mooc.view.ui.VideoAdapter;

public class VideoDetailsActivity extends GenericActivity<VideoOps.View,VideoOps> implements VideoOps.View {
    private VideoView mVideoView;
    private RatingBar mRatingBar;
    private Button mDownloadButton;
    private Video video;
    private DownloadResultReceiver mDownloadResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, VideoOps.class, this);
        setContentView(R.layout.video_details_activity);
        // Initialize Views
        mVideoView = (VideoView)findViewById(R.id.videoView);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mDownloadButton = (Button) findViewById(R.id.downloadButton);
        mDownloadButton.setVisibility(View.INVISIBLE);
        mDownloadResultReceiver = new DownloadResultReceiver();
        // get details of selected video from Intent
        long selectionId = getIntent().getLongExtra(VideoListActivity.VIDEO_SELECTION_ID_KEY, (long) -1);
        if(selectionId == (long)-1){
            // TODO no video to show details for. Create Toast and kill activity
            Toast.makeText(VideoDetailsActivity.this,"No Video could be found on server !!",Toast.LENGTH_LONG).show();
            return;
        }
        video = VideoListActivity.selectedVideo;
        // TODO create retrofit call within an async task.
        Toast.makeText(this, " ID is : " + selectionId, Toast.LENGTH_LONG).show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setAdapter(VideoAdapter videoAdapter) {

    }
    // TODO DownloadVideoService broadcasts an intent.
    @Override
    protected void onResume() {
        // Call up to the superclass.
        super.onResume();

        // Register BroadcastReceiver that receives result from
        // UploadVideoService when a video upload completes.
        registerReceiver();
        if(!videoPresentOnDevice()){
            // Todo show download button
            mDownloadButton.setVisibility(View.VISIBLE);
            mDownloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Todo start downloading via DownloadVideoService
                    getOps().downloadVideo();
                }
            });

        }
    }

    private boolean videoPresentOnDevice() {
        boolean result = true;
        if(video.getLocalURI()==null || VideoMediaStoreUtils.getVideo(this,video.getLocalURI())==null)
            result=false;
        return result;
    }

    //Todo
    private void registerReceiver(){
       // Create an Intent filter that handles Intents from the
        // DownloadVideoService.
        IntentFilter intentFilter =
                new IntentFilter(DownloadVideoService.ACTION_DOWNLOAD_SERVICE_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // Register the BroadcastReceiver.
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mDownloadResultReceiver,
                        intentFilter);
    }
    /**
     * Hook method that gives a final chance to release resources and
     * stop spawned threads.  onDestroy() may not always be
     * called-when system kills hosting process.
     */
    @Override
    protected void onPause() {
        // Call onPause() in superclass.
        super.onPause();

        // Unregister BroadcastReceiver.

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mDownloadResultReceiver);
    }

    private class DownloadResultReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //Todo make toast to show status of downloaded video
            Toast.makeText(VideoDetailsActivity.this,"Reciever fot the result. Do some processing bro",Toast.LENGTH_LONG).show();
            mVideoView.setVideoURI();
        }
    }
}
