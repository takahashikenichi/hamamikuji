/*
 * Japan Android Group Yokohama Div.
 * Hamamikuji - Omikuji for Android Watch
 */

package jp.codedesign.android.jagy_omikuji;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import jp.codedesign.android.jagy_omikuji.fragments.OracleFragment;
import jp.codedesign.android.jagy_omikuji.fragments.SettingsFragment;

/**
 * ハマみくじのメインアクティビティ
 * センサー情報をこのアクティビティで受ける。
 */
public class MainActivity extends Activity implements SensorEventListener {

    private static final String TAG = "JOMainActivity";

    /** スクリーンがオンになっている時間 **/
    private static final long SCREEN_ON_TIMEOUT_MS = 20000; // msec - 20秒

    /** an up-down movement that takes more than this will not be registered as such **/
    private static final long TIME_THRESHOLD_NS = 2000000000; // in nanoseconds (= 2sec)

    /**
     * 天地がかわったことを検知する敷居値 (gravity = 9.8 に対して)
     */
    private static final float GRAVITY_THRESHOLD = 7.0f;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long mLastTime = 0;
    private boolean wasUpsideDown = false;
    private int mRotationCounter = 0;
    private ViewPager mPager;
    private OracleFragment mOraclePage;
    private SettingsFragment mSettingPage;
    private ImageView mSecondIndicator;
    private ImageView mFirstIndicator;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jo_layout);
        setupViews(); // ページ設定
        mHandler = new Handler();
        mRotationCounter = Utils.getCounterFromPreference(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // スクリーンを消灯しない
        renewTimer();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
//        setCounter(mRotationCounter);
    }

    private void setupViews() {
        mPager = (ViewPager) findViewById(R.id.pager);
        mFirstIndicator = (ImageView) findViewById(R.id.indicator_0);
        mSecondIndicator = (ImageView) findViewById(R.id.indicator_1);
        final PagerAdapter adapter = new PagerAdapter(getFragmentManager());
        mOraclePage = new OracleFragment(); // おみくじFragment設定
        mSettingPage = new SettingsFragment(); // 設定Fragment設定
        adapter.addFragment(mOraclePage);
        adapter.addFragment(mSettingPage);
        setIndicator(0); // インジケーターが最初のページであることを表示
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                setIndicator(i);
                renewTimer();
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        mPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Successfully registered for the sensor updates");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Unregistered for sensor events");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        detectJump(sensorEvent.values[1], sensorEvent.timestamp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     */
    private void detectJump(float yValue, long timestamp) {
        // 敷居値以上の変更があったとき
        if ((Math.abs(yValue) > GRAVITY_THRESHOLD)) {
            // TIME_THRESHOLD_NS 以内の時間で閾値を変更したか / 一回転したか
            if(timestamp - mLastTime < TIME_THRESHOLD_NS && wasUpsideDown != (yValue < 0)) {
                onRotationDetected(!wasUpsideDown);
            }
            wasUpsideDown = yValue < 0;
            mLastTime = timestamp;
        }
    }

    /**
     * ローテーションが検知された場合
     */
    private void onRotationDetected(boolean up) {
        // we only count a pair of up and down as one successful movement
        if (up) {
            return;
        }
        // ここにおみくじ処理を入れる
        mRotationCounter++;
        setCounter(mRotationCounter);
        renewTimer(); // タイマーの初期化
    }

    /**
     * Updates the counter on UI, saves it to preferences and vibrates the watch when counter
     * reaches a multiple of 10.
     */
    private void setCounter(int i) {
        mSettingPage.setCounter(i);
//        mOraclePage.setCounter(i);
        mOraclePage.startAnimation();
        Utils.saveCounterToPreference(this, i);
        Utils.vibrate(this, 0); // 回転したらバイブレーションさせる
    }

    public void resetCounter() {
        mRotationCounter = 0;
        mSettingPage.setCounter(0);
        Utils.saveCounterToPreference(this, 0);
//        setCounter(0);
        renewTimer();
    }

    /**
     * Starts a timer to clear the flag FLAG_KEEP_SCREEN_ON.
     */
    private void renewTimer() {
        if (null != mTimer) {
            mTimer.cancel();
        }
        // タイマータスクの処理を設定する
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG,
                            "Removing the FLAG_KEEP_SCREEN_ON flag to allow going to background");
                }
                resetFlag();
            }
        };
        mTimer = new Timer();
        // タイマーをスケジュールし、画面を維持する時間の間表示する
        mTimer.schedule(mTimerTask, SCREEN_ON_TIMEOUT_MS);
    }

    /**
     * Resets the FLAG_KEEP_SCREEN_ON flag so activity can go into background.
     */
    private void resetFlag() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Resetting FLAG_KEEP_SCREEN_ON flag to allow going to background");
                }
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                finish();
            }
        });
    }

    /**
     * Sets the page indicator for the ViewPager.
     */
    private void setIndicator(int i) {
        switch (i) {
            case 0:
                mFirstIndicator.setImageResource(R.drawable.full_10);
                mSecondIndicator.setImageResource(R.drawable.empty_10);
                break;
            case 1:
                mFirstIndicator.setImageResource(R.drawable.empty_10);
                mSecondIndicator.setImageResource(R.drawable.full_10);
                break;
        }
    }


}
