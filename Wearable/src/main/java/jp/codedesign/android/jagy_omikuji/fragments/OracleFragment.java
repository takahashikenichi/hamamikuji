package jp.codedesign.android.jagy_omikuji.fragments;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import jp.codedesign.android.jagy_omikuji.R;

/**
 * おみくじ表示ページ
 */
public class OracleFragment extends Fragment {
    private static final String TAG = "JOOracleFragment";

    private static final long ANIMATION_INTERVAL_MS = 500; // in milliseconds
    private TextView mCounterText;
    private Timer mAnimationTimer;
    private Handler mHandler;
    private TimerTask mAnimationTask;
    private boolean up = false;
    private Drawable mDownDrawable;
    private Drawable mUpDrawable;
    private boolean isAnimation = false;
    private int animationCounter = 0;
    private int[] result = {
            R.string.daikichi,
            R.string.chukichi,
            R.string.shokichi,
            R.string.kichi,
            R.string.kyou
    };

    ArrayList<Drawable> mResultImage = new ArrayList<Drawable>();

    private static final int REPEAT_NUM = 8;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.oracle_layout, container, false);
        // Image from http://www.irasutoya.com/2012/04/blog-post_7236.html
        // http://www.irasutoya.com/2012/04/blog-post_04.html
        mDownDrawable = getResources().getDrawable(R.drawable.omikuji_down);
        mUpDrawable = getResources().getDrawable(R.drawable.omikuji_up);

        mResultImage.add(getResources().getDrawable(R.drawable.img_daikichi));
        mResultImage.add(getResources().getDrawable(R.drawable.img_chukichi));
        mResultImage.add(getResources().getDrawable(R.drawable.img_shokichi));
        mResultImage.add(getResources().getDrawable(R.drawable.img_kichi));
        mResultImage.add(getResources().getDrawable(R.drawable.img_kyo));

        mCounterText = (TextView) view.findViewById(R.id.counter);
//        mCounterText.setCompoundDrawablesWithIntrinsicBounds(mUpDrawable, null, null, null);
//        setCounter(Utils.getCounterFromPreference(getActivity()));
        mHandler = new Handler();
//        startAnimation();
        return view;
    }

    public void startAnimation() {
        if(isAnimation) return;
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Animation Start");
        }
        mAnimationTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(animationCounter < REPEAT_NUM) {
                            mCounterText.setText("");
                            // 画像を入れる場合は下にセッティングする
                            mCounterText.setCompoundDrawablesWithIntrinsicBounds(
                                    null, up ? mUpDrawable : mDownDrawable, null, null);
                            if(animationCounter == REPEAT_NUM - 1) {
                                Random r = new Random();
                                int rnd = r.nextInt(result.length);
//                                mCounterText.setText(result[rnd]);
                                mCounterText.setCompoundDrawablesWithIntrinsicBounds(
                                        null, mResultImage.get(rnd), null, null);
//                                mCounterText.setBackgroundColor(Color.RED);
                            } else {
//                                mCounterText.setBackgroundColor(Color.WHITE);
                            }
                            up = !up;
                            animationCounter++;
                        } else {
                            isAnimation = false;
                            animationCounter = 0;
                            mAnimationTimer.cancel();
                        }
                    }
                });
            }
        };
        mAnimationTimer = new Timer();
        mAnimationTimer.scheduleAtFixedRate(mAnimationTask, ANIMATION_INTERVAL_MS,
                ANIMATION_INTERVAL_MS);
        isAnimation = true;
    }

    public void setCounter(String text) {
        mCounterText.setText(text);
    }

    public void setCounter(int i) {
        setCounter(i < 0 ? "0" : String.valueOf(i));
    }

    @Override
    public void onDetach() {
        mAnimationTimer.cancel();
        super.onDetach();
    }
}
