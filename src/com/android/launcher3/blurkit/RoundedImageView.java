package com.android.launcher3.blurkit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;


import static com.android.launcher3.LauncherAnimUtils.OVERVIEW_TRANSITION_MS;
import static com.android.launcher3.LauncherAnimUtils.SPRING_LOADED_EXIT_DELAY;
import static com.android.launcher3.LauncherAnimUtils.SPRING_LOADED_TRANSITION_MS;
import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
import static com.android.launcher3.LauncherState.ALL_APPS;
import static com.android.launcher3.LauncherState.NORMAL;
import static com.android.launcher3.LauncherState.SPRING_LOADED;
import static com.android.launcher3.config.FeatureFlags.ADAPTIVE_ICON_WINDOW_ANIM;
import static com.android.launcher3.dragndrop.DragLayer.ALPHA_INDEX_OVERLAY;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.android.launcher3.Launcher.LauncherOverlay;
import com.android.launcher3.LauncherAppWidgetHost.ProviderChangedListener;
import com.android.launcher3.LauncherStateManager.AnimationConfig;
import com.android.launcher3.accessibility.AccessibleDragListenerAdapter;
import com.android.launcher3.accessibility.WorkspaceAccessibilityHelper;
import com.android.launcher3.anim.AnimatorSetBuilder;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.dot.FolderDotInfo;
import com.android.launcher3.dragndrop.DragController;
import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.dragndrop.DragOptions;
import com.android.launcher3.dragndrop.DragView;
import com.android.launcher3.dragndrop.SpringLoadedDragController;
import com.android.launcher3.folder.Folder;
import com.android.launcher3.folder.FolderIcon;
import com.android.launcher3.folder.PreviewBackground;
import com.android.launcher3.graphics.DragPreviewProvider;
import com.android.launcher3.graphics.PreloadIconDrawable;
import com.android.launcher3.graphics.RotationMode;
import com.android.launcher3.logging.UserEventDispatcher;
import com.android.launcher3.pageindicators.WorkspacePageIndicator;
import com.android.launcher3.popup.PopupContainerWithArrow;
import com.android.launcher3.shortcuts.ShortcutDragPreviewProvider;
import com.android.launcher3.testing.TestProtocol;
import com.android.launcher3.touch.WorkspaceTouchListener;
import com.android.launcher3.userevent.nano.LauncherLogProto.Action;
import com.android.launcher3.userevent.nano.LauncherLogProto.ContainerType;
import com.android.launcher3.userevent.nano.LauncherLogProto.Target;
import com.android.launcher3.util.Executors;
import com.android.launcher3.util.IntArray;
import com.android.launcher3.util.IntSparseArrayMap;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.util.Thunk;
import com.android.launcher3.util.WallpaperOffsetInterpolator;
import com.android.launcher3.widget.LauncherAppWidgetHostView;
import com.android.launcher3.widget.PendingAddShortcutInfo;
import com.android.launcher3.widget.PendingAddWidgetInfo;
import com.android.launcher3.widget.PendingAppWidgetHostView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Predicate;
public class RoundedImageView extends ImageView {

    private float mCornerRadius = 0;
    public static final int DEFAULT_COLOR = 0xff000000;
    public static final int DEFAULT_RGB = 0;

    private RectF rectF;
    private PorterDuffXfermode porterDuffXfermode;

    public RoundedImageView(Context context) {
        super(context, null);
        rectF = new RectF();
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    public RoundedImageView(Context context, AttributeSet attributes) {
        super(context, attributes);
        rectF = new RectF();
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable myDrawable = getDrawable();
        if (myDrawable!=null && myDrawable instanceof BitmapDrawable && mCornerRadius > 0) {
            rectF.set(myDrawable.getBounds());
            int prevCount = canvas.saveLayer(rectF, null, Canvas.ALL_SAVE_FLAG);
            getImageMatrix().mapRect(rectF);

            Paint paint = ((BitmapDrawable) myDrawable).getPaint();
            paint.setAntiAlias(true);
            paint.setColor(DEFAULT_COLOR);
            Xfermode prevMode = paint.getXfermode();

            canvas.drawARGB(DEFAULT_RGB, DEFAULT_RGB, DEFAULT_RGB, DEFAULT_RGB);
            canvas.drawRoundRect(rectF, mCornerRadius, mCornerRadius, paint);

            paint.setXfermode(porterDuffXfermode);
            super.onDraw(canvas);

            paint.setXfermode(prevMode);
            canvas.restoreToCount(prevCount);
        } else {
            super.onDraw(canvas);
        }
    }

    public void setCornerRadius(float cornerRadius) {
        this.mCornerRadius = cornerRadius;
    }

    public float getCornerRadius() {
        return this.mCornerRadius;
    }
}