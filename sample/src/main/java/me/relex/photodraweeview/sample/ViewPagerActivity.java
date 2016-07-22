package me.relex.photodraweeview.sample;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import me.relex.circleindicator.CircleIndicator;
import me.relex.photodraweeview.IAdjustController;
import me.relex.photodraweeview.PhotoDraweeView;

public class ViewPagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);

        ((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        MultiTouchViewPager viewPager = (MultiTouchViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new DraweePagerAdapter());
        indicator.setViewPager(viewPager);
    }

    public static boolean isLongImageByVertical(int imgWidth, int imgHeight) {
        if (imgWidth == 0 || imgHeight == 0) {
            return false;
        }
        if (imgHeight >= imgWidth * 3) {
            return true;
        } else {
            return false;
        }
    }
    public class DraweePagerAdapter extends PagerAdapter {

        private int[] mDrawables = new int[] {
                R.drawable.long_img
                , R.drawable.viewpager_2, R.drawable.viewpager_3,
                R.drawable.long_img
                , R.drawable.viewpager_2};
        private IAdjustController mAdjustController = new IAdjustController() {
            @Override
            public boolean onAdjustWidth(int width, int height) {
                return isLongImageByVertical(width, height);
            }
        };

        @Override
        public int getCount() {
            return mDrawables.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup viewGroup, int position) {
            final PhotoDraweeView photoDraweeView = new PhotoDraweeView(viewGroup.getContext());
            photoDraweeView.setAdjustController(mAdjustController);

            photoDraweeView.getHierarchy().setProgressBarImage(new ProgressBarDrawable());
            PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            controller.setImageRequest(ImageRequestBuilder.newBuilderWithResourceId(mDrawables[position]).build());
            controller.setOldController(photoDraweeView.getController());


            photoDraweeView.setController(controller.build());

            try {
                viewGroup.addView(photoDraweeView, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return photoDraweeView;
        }
    }
}
