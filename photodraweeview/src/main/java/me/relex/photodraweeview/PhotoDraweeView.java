package me.relex.photodraweeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import javax.microedition.khronos.opengles.GL10;

public class PhotoDraweeView extends SimpleDraweeView implements IAttacher, ControllerListener<ImageInfo> {
    private final static int MAX_TEXTURE_SIZE = GL10.GL_MAX_TEXTURE_SIZE * 10 / 9;
    private Attacher mAttacher;
    private boolean mAdjustMaxScale = false;

    private boolean mAdjustOnResize;
    private int mImageWidth;
    private int mImageHeight;

    public PhotoDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init();
    }

    public PhotoDraweeView(Context context) {
        super(context);
        init();
    }

    public PhotoDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        if (mAttacher == null || mAttacher.getDraweeView() == null) {
            mAttacher = new Attacher(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        Matrix drawMatrix = mAttacher.getDrawMatrix();
        Log.d("fresco", "onDraw: " + drawMatrix);
        canvas.concat(drawMatrix);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void onAttachedToWindow() {
        init();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttacher.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override
    public float getMinimumScale() {
        return mAttacher.getMinimumScale();
    }

    @Override
    public float getMediumScale() {
        return mAttacher.getMediumScale();
    }

    @Override
    public float getMaximumScale() {
        return mAttacher.getMaximumScale();
    }

    @Override
    public void setMinimumScale(float minimumScale) {
        mAttacher.setMinimumScale(minimumScale);
    }

    @Override
    public void setMediumScale(float mediumScale) {
        mAttacher.setMediumScale(mediumScale);
    }

    @Override
    public void setMaximumScale(float maximumScale) {
        mAttacher.setMaximumScale(maximumScale);
    }

    @Override
    public float getScale() {
        return mAttacher.getScale();
    }

    @Override
    public void setScale(float scale) {
        mAttacher.setScale(scale);
    }

    @Override
    public void setScale(float scale, boolean animate) {
        mAttacher.setScale(scale, animate);
    }

    @Override
    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        mAttacher.setScale(scale, focalX, focalY, animate);
    }

    @Override
    public void setZoomTransitionDuration(long duration) {
        mAttacher.setZoomTransitionDuration(duration);
    }

    @Override
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAttacher.setAllowParentInterceptOnEdge(allow);
    }

    @Override
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener listener) {
        mAttacher.setOnDoubleTapListener(listener);
    }

    @Override
    public void setOnScaleChangeListener(OnScaleChangeListener listener) {
        mAttacher.setOnScaleChangeListener(listener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        mAttacher.setOnLongClickListener(listener);
    }

    @Override
    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mAttacher.setOnPhotoTapListener(listener);
    }

    @Override
    public void setOnViewTapListener(OnViewTapListener listener) {
        mAttacher.setOnViewTapListener(listener);
    }

    @Override
    public OnPhotoTapListener getOnPhotoTapListener() {
        return mAttacher.getOnPhotoTapListener();
    }

    @Override
    public OnViewTapListener getOnViewTapListener() {
        return mAttacher.getOnViewTapListener();
    }

    @Override
    public void update(int imageInfoWidth, int imageInfoHeight) {
        mAttacher.update(imageInfoWidth, imageInfoHeight);
    }

    public void setAdjustMaxScale(boolean adjustMaxScale) {
        mAdjustMaxScale = adjustMaxScale;
        setAutoScale(mImageWidth, mImageHeight);
    }

    @Override
    public void setController(DraweeController draweeController) {
        mImageWidth = mImageHeight = 0;
        if (draweeController instanceof PipelineDraweeController) {
            PipelineDraweeController controller = (PipelineDraweeController) draweeController;
            controller.removeControllerListener(this);
            controller.addControllerListener(this);
        }

        super.setController(draweeController);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mAdjustOnResize) {
            Drawable topLevelDrawable = getHierarchy().getTopLevelDrawable();
            if (topLevelDrawable != null) {
                topLevelDrawable.setBounds(new Rect(getPaddingLeft(), getPaddingRight(),
                        w - getPaddingRight(), h - getPaddingBottom()));
            }

            update(mImageWidth, mImageHeight);
            setAutoScale(mImageWidth, mImageHeight);
        }
    }

    private boolean adjustOnResize() {
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        mAdjustOnResize = (height == 0 || width == 0);
        return mAdjustOnResize;
    }

    private void setAutoScale(int imgWidth, int imgHeight) {
        if (mAdjustMaxScale) {
            if (adjustOnResize() || imgWidth == 0 || imgHeight == 0) {
                return;
            }

            int height = getMeasuredHeight();
            int width = getMeasuredWidth();

            float scaleMultipler;
            if (imgHeight > imgWidth) {
                scaleMultipler = width / ((float)imgWidth / imgHeight * height);
            } else {
                scaleMultipler = height / ((float)imgHeight / imgWidth * width);
            }

            mAttacher.setScaleMultipler(scaleMultipler);
        } else {
            mAttacher.setScaleMultipler(1.0f);
        }

        float minimumScale = mAttacher.getMinimumScale();
        if (getScale() != minimumScale) {
            setScale(minimumScale);
        }
    }

    @Override
    public void onSubmit(String id, Object callerContext) {
    }

    @Override
    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
        if (imageInfo != null) {
            // 渲染溢出控制
            mImageWidth = imageInfo.getWidth();
            mImageHeight = imageInfo.getHeight();
            int maxSize = Math.max(mImageWidth, mImageHeight);
            if (maxSize > MAX_TEXTURE_SIZE) {
                ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null);
            } else {
                ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_HARDWARE, null);
            }

            if (!adjustOnResize()) {
                update(mImageWidth, mImageHeight);
                setAutoScale(mImageWidth, mImageHeight);
            }
        }
    }

    @Override
    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {

    }

    @Override
    public void onIntermediateImageFailed(String id, Throwable throwable) {

    }

    @Override
    public void onFailure(String id, Throwable throwable) {

    }

    @Override
    public void onRelease(String id) {

    }
}
