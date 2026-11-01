
package com.google.android.apps.nexuslauncher.graphics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.View;

import com.hdeva.launcher.LeanUtils;

public class IcuDateTextView extends DoubleShadowTextView {

    private final BroadcastReceiver mTimeChangeReceiver;

    private boolean mTimeReceiverRegistered = false;
    private boolean mRequestedVisible = false;

    public IcuDateTextView(Context context) {
        this(context, null);
    }

    public IcuDateTextView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);

        mTimeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reloadDateFormat();
            }
        };
    }

    public void reloadDateFormat() {
        String format = LeanUtils.formatDateTime(
                getContext(),
                System.currentTimeMillis()
        );

        setText(format);
        setContentDescription(format);
    }

    /*
     * SmartspaceView calls this method directly on Android 5.1.
     * Keep this method, but do not call the API-24 superclass method.
     */
    public void onVisibilityAggregated(boolean isVisible) {
        mRequestedVisible = isVisible;
        updateTimeReceiver();
    }

    private void updateTimeReceiver() {
        boolean shouldRegister = mRequestedVisible && isShown();

        if (shouldRegister && !mTimeReceiverRegistered) {
            registerReceiver();
            mTimeReceiverRegistered = true;
            reloadDateFormat();

        } else if (!shouldRegister && mTimeReceiverRegistered) {
            unregisterReceiver();
            mTimeReceiverRegistered = false;
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        getContext().registerReceiver(mTimeChangeReceiver, filter);
    }

    private void unregisterReceiver() {
        getContext().unregisterReceiver(mTimeChangeReceiver);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateTimeReceiver();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mTimeReceiverRegistered) {
            unregisterReceiver();
            mTimeReceiverRegistered = false;
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(
            View changedView,
            int visibility
    ) {
        super.onVisibilityChanged(changedView, visibility);
        updateTimeReceiver();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        updateTimeReceiver();
    }
}
