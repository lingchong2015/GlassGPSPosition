package com.curry.stephen.glassgpsposition;

import com.google.android.glass.content.Intents;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;

public class MainActivity extends Activity {

    private CardScrollView mCardScroller;

    private SimpleCardScrollAdapter mSimpleCardScrollAdapter;

    private boolean mIsRecordEnable = false;

    private String mPicturePath;

    private GestureDetector mGestureDetector;

    private Bitmap mBitmap;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

//            double latitude = intent.getDoubleExtra(getString(R.string.latitude), 0);
//            double longitude = intent.getDoubleExtra(getString(R.string.longitude), 0);
//            Log.i(TAG, "Receive GPS info.");
//            insertNewCard(0, getCardBuilder(String.format("纬度：%.5f\n经度：%.5f", latitude, longitude), mPicturePath));
//            Log.i(TAG, "Image path in BroadcastReceiver is : " + mPicturePath);
//            mIsRecordEnable = false;

            if (mIsRecordEnable) {
                Log.i(TAG, "Receive GPS info.");
                double latitude = intent.getDoubleExtra(getString(R.string.latitude), 0);
                double longitude = intent.getDoubleExtra(getString(R.string.longitude), 0);

                insertNewCard(0, getCardBuilder(String.format("%.5f|\n%.5f", latitude, longitude)));

                mIsRecordEnable = false;
            }
        }
    };

    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (gesture != Gesture.SWIPE_LEFT && gesture != Gesture.SWIPE_RIGHT) {
                openOptionsMenu();
                return true;
            } else {
                return false;
            }
        }
    };

    public static final String RECEIVE_GPS_INFO = MainActivity.class.getSimpleName() + "receive_gps_info";

    private static final int TAKE_PICTURE_REQUEST = 1;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);

        initUI();

        initBroadcastReceiver();

        startGPSService();

        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
    }

    private void initUI() {
        mCardScroller = new CardScrollView(this);

        mSimpleCardScrollAdapter = new SimpleCardScrollAdapter();
//        mSimpleCardScrollAdapter.getCardBuilders().add(getCardBuilder("请使用语音\"ok glass\">\"preview\"->\"record\"记录GPS定位信息。"));
        mSimpleCardScrollAdapter.getCardBuilders().add(getCardBuilder("请使用轻触屏幕->\"preview\"->\"record\"记录GPS定位信息。"));
        mCardScroller.setAdapter(mSimpleCardScrollAdapter);
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.DISALLOWED);
            }
        });
        setContentView(mCardScroller);

        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
    }

    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(RECEIVE_GPS_INFO);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void startGPSService() {
        startService(new Intent(this, GpsService.class));
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCardScroller.deactivate();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
//            String thumbnailPath = data.getStringExtra(Intents.EXTRA_THUMBNAIL_FILE_PATH);

            mPicturePath = data.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);
//            mIsRecordEnable = true;
            processPictureWhenReady(mPicturePath);
            Log.i(TAG, "Image path in onActivityResult is : " + mPicturePath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
//            getMenuInflater().inflate(R.menu.activity_menu, menu);
//            return true;
//        } else {
//            return super.onCreatePanelMenu(featureId, menu);
//        }

        getMenuInflater().inflate(R.menu.activity_menu, menu);

        return true;
    }

    private Uri mUri;
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            if (item.getItemId() == R.id.preview) {
                Log.i(TAG, "Preview menu activate.");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TAKE_PICTURE_REQUEST);
            } else if (item.getItemId() == R.id.record) {
                Log.i(TAG, "Record menu activate.");
                enableRecordGPS();
                return true;
            } else if (item.getItemId() == R.id.quit) {
                Log.i(TAG, "Quit menu activate.");
                finish();
                return true;
            }
        } else {
            if (item.getItemId() == R.id.preview) {
                Log.i(TAG, "Preview menu activate.");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TAKE_PICTURE_REQUEST);
            } else if (item.getItemId() == R.id.record) {
                Log.i(TAG, "Record menu activate.");
                enableRecordGPS();
                return true;
            } else if (item.getItemId() == R.id.quit) {
                Log.i(TAG, "Quit menu activate.");
                finish();
                return true;
            }
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private CardBuilder getCardBuilder(String info) {
        CardBuilder cardBuilder;
        if (mBitmap != null) {
            cardBuilder = new CardBuilder(this, CardBuilder.Layout.TITLE);
//            Bitmap bitmap = BitmapFactory.decodeFile(mUri.getPath());
            cardBuilder.addImage(mBitmap);
            mBitmap = null;
        } else {
            cardBuilder = new CardBuilder(this, CardBuilder.Layout.TEXT);
        }
        cardBuilder.setText(info);
        return cardBuilder;
    }

    private void enableRecordGPS() {
        mIsRecordEnable = true;
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
    }

    private void insertNewCard(int position, CardBuilder card) {
        mSimpleCardScrollAdapter.insertCardWithoutNotification(position, card);
        mCardScroller.animate(position, CardScrollView.Animation.INSERTION);
    }

    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            Toast.makeText(this, "照片已保存。", Toast.LENGTH_SHORT).show();
            // The picture is ready; process it.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            mBitmap = BitmapFactory.decodeFile(picturePath, options);
        } else {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).

            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath(),
                    FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
                // Protect against additional pending events after CLOSE_WRITE
                // or MOVED_TO is handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = affectedFile.equals(pictureFile);

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }
}
