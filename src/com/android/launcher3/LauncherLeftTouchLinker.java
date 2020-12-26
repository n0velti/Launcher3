package com.android.launcher3;

import android.util.Log;
import android.view.MotionEvent;

import com.android.launcher3.anim.AnimatorSetBuilder;
import com.android.launcher3.touch.AbstractStateChangeTouchController;
import com.android.launcher3.touch.SingleAxisSwipeDetector;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.quickstep.OverviewInteractionState;
import com.android.quickstep.SysUINavigationMode;

import static com.android.launcher3.LauncherState.NORMAL;
import static com.android.launcher3.LauncherState.QUICK_SWITCH;
import static com.android.launcher3.anim.AnimatorSetBuilder.ANIM_ALL_APPS_FADE;
import static com.android.launcher3.anim.AnimatorSetBuilder.ANIM_OVERVIEW_FADE;
import static com.android.launcher3.anim.AnimatorSetBuilder.ANIM_OVERVIEW_SCALE;
import static com.android.launcher3.anim.AnimatorSetBuilder.ANIM_OVERVIEW_TRANSLATE_Y;
import static com.android.launcher3.anim.AnimatorSetBuilder.ANIM_VERTICAL_PROGRESS;
import static com.android.launcher3.anim.AnimatorSetBuilder.ANIM_WORKSPACE_FADE;
import static com.android.launcher3.anim.AnimatorSetBuilder.ANIM_WORKSPACE_TRANSLATE;
import static com.android.launcher3.anim.Interpolators.ACCEL_2;
import static com.android.launcher3.anim.Interpolators.DEACCEL_2;
import static com.android.launcher3.anim.Interpolators.INSTANT;
import static com.android.launcher3.anim.Interpolators.LINEAR;
import static com.android.launcher3.util.SystemUiController.UI_STATE_OVERVIEW;
import static com.android.quickstep.views.RecentsView.UPDATE_SYSUI_FLAGS_THRESHOLD;
import static com.android.systemui.shared.system.QuickStepContract.SYSUI_STATE_OVERVIEW_DISABLED;

public class LauncherLeftTouchLinker extends AbstractStateChangeTouchController {


    public LauncherLeftTouchLinker(Launcher l) {
        super(l, SingleAxisSwipeDetector.HORIZONTAL);
    }

    @Override
    protected boolean canInterceptTouch(MotionEvent ev) {
        Log.d("e_intercept", ev.toString());

        if (mCurrentAnimation != null) {
            Log.d("e_intercept", "not null");

            return true;
        }
        if (!mLauncher.isInState(LauncherState.NORMAL)) {
            Log.d("e_intercept", "normal");

            return false;
        }
        if ((ev.getEdgeFlags() & Utilities.EDGE_NAV_BAR) == 0) {
            Log.d("e_intercept", "nav");

            return false;
        }
        Log.d("e_intercept", "final");
        return true;
    }

    @Override
    protected LauncherState getTargetState(LauncherState fromState, boolean isDragTowardPositive) {
        int stateFlags = OverviewInteractionState.INSTANCE.get(mLauncher).getSystemUiStateFlags();
        if ((stateFlags & SYSUI_STATE_OVERVIEW_DISABLED) != 0) {
            System.out.println(stateFlags);
            return NORMAL;
        }

        System.out.println(isDragTowardPositive);

        return isDragTowardPositive ? QUICK_SWITCH : NORMAL;
    }

    @Override
    protected float initCurrentAnimation(int animComponents) {
        AnimatorSetBuilder animatorSetBuilder = new AnimatorSetBuilder();
        setupInterpolators(animatorSetBuilder);
        long accuracy = (long) (getShiftRange() * 2);
        mCurrentAnimation = mLauncher.getStateManager().createAnimationToNewWorkspace(mToState,
                animatorSetBuilder, accuracy, this::clearState, LauncherStateManager.ANIM_ALL);

        return 1 / getShiftRange();
    }

    private void setupInterpolators(AnimatorSetBuilder animatorSetBuilder) {
        animatorSetBuilder.setInterpolator(ANIM_WORKSPACE_FADE, DEACCEL_2);
        animatorSetBuilder.setInterpolator(ANIM_ALL_APPS_FADE, DEACCEL_2);
        if (SysUINavigationMode.getMode(mLauncher) == SysUINavigationMode.Mode.NO_BUTTON) {
            // Overview lives to the left of workspace, so translate down later than over
            animatorSetBuilder.setInterpolator(ANIM_WORKSPACE_TRANSLATE, ACCEL_2);
            animatorSetBuilder.setInterpolator(ANIM_VERTICAL_PROGRESS, ACCEL_2);
            animatorSetBuilder.setInterpolator(ANIM_OVERVIEW_SCALE, ACCEL_2);
            animatorSetBuilder.setInterpolator(ANIM_OVERVIEW_TRANSLATE_Y, ACCEL_2);
            animatorSetBuilder.setInterpolator(ANIM_OVERVIEW_FADE, INSTANT);
        } else {
            animatorSetBuilder.setInterpolator(ANIM_WORKSPACE_TRANSLATE, LINEAR);
            animatorSetBuilder.setInterpolator(ANIM_VERTICAL_PROGRESS, LINEAR);
        }
    }


    @Override
    protected void updateProgress(float progress) {
        super.updateProgress(progress);
        System.out.println("progress " + progress);
    }


    @Override
    protected float getShiftRange() {
        return mLauncher.getDeviceProfile().widthPx / 2f;
    }

    @Override
    protected int getLogContainerTypeForNormalState(MotionEvent ev) {
        return LauncherLogProto.ContainerType.NAVBAR;
    }

    @Override
    protected int getDirectionForLog() {
        return Utilities.isRtl(mLauncher.getResources()) ? LauncherLogProto.Action.Direction.LEFT : LauncherLogProto.Action.Direction.RIGHT;
    }
}
