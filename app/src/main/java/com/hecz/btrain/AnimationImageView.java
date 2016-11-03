package com.hecz.btrain;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * Created by kolmanp on 03/10/15.
 */
public class AnimationImageView extends ImageView {
    private Animation lastAnimation = null;

    public AnimationImageView(Context context) {
        super(context);
    }

    public AnimationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimationImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void startAnimation(Animation animation) {
        super.startAnimation(animation);
        lastAnimation = animation;
    }

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();
        //Functionality here
        //clearAnimation();
        if(lastAnimation != null) {
            //lastAnimation.
            startAnimation(lastAnimation);
        }
    }
}
