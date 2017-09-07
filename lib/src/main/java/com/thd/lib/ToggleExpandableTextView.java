package com.thd.lib;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;



/**
 * @package： com.thd.lib
 * @author： haidev
 * @time： 2017/7/6
 */
public class ToggleExpandableTextView extends TextView implements View.OnClickListener {

    private static final String TAG = ToggleExpandableTextView.class.getSimpleName();

    /* The default number of lines */
    private static final int MAX_COLLAPSED_LINES = 2;

    /* The default animation duration */
    private static final int DEFAULT_ANIM_DURATION = 200;

    /* The default alpha value when the animation starts */
    private static final float DEFAULT_ANIM_ALPHA_START = 1.0f;

    /* The default animator */
    private static final boolean DEFAULT_USE_TOGGLEVIEW_ANIMATOR = true;

    private boolean mRelayout=true;

    private boolean mCollapsed = true; // Show short version as default.

    private int mCollapsedHeight;

    private int mTextHeightWithMaxLines;

    private int mMaxCollapsedLines=2;

    private int mMarginBetweenTxtAndBottom;


    private int mAnimationDuration;

    private float mAnimAlphaStart;

    private boolean useToggleDefualtAnimator;

    private ImageView toggleView;

    /* Listener for callback */
    private OnExpandStateChangeListener mListener;

    /* For saving collapsed status when used in ListView */
    private SparseBooleanArray mCollapsedStatus;

    private int mPosition;

    private boolean mAnimating;


    public ToggleExpandableTextView(Context context) {
        this(context,null);
    }

    public ToggleExpandableTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ToggleExpandableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        mMaxCollapsedLines = typedArray.getInt(R.styleable.ExpandableTextView_maxCollapsedLines, MAX_COLLAPSED_LINES);
        mAnimationDuration = typedArray.getInt(R.styleable.ExpandableTextView_animDuration, DEFAULT_ANIM_DURATION);
        mAnimAlphaStart = typedArray.getFloat(R.styleable.ExpandableTextView_animAlphaStart, DEFAULT_ANIM_ALPHA_START);
        useToggleDefualtAnimator = typedArray.getBoolean(R.styleable.ExpandableTextView_useToggleViewDefaultAnimator, DEFAULT_USE_TOGGLEVIEW_ANIMATOR);
        int d=1;
        typedArray.recycle();

    }

    public void bindToggle(ImageView toggleView){
        if(toggleView==null){
            throw new RuntimeException("toggleView can't be null");
        }
        this.toggleView=toggleView;
        this.toggleView.setOnClickListener(this);
    }

    public void setMaxCollapsedLines(int maxCollapsedLines){
        mMaxCollapsedLines=maxCollapsedLines;
    }

    public void useToggleDefaultAnimator(boolean useToggleDefualtAnimator){
        this.useToggleDefualtAnimator=useToggleDefualtAnimator;
    }
    public void setToggleVisiableWithLessLines(boolean toggleVisiableWithLessLines){
        
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // If no change, measure and return
        if (!mRelayout || getVisibility() == View.GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        mRelayout = false;

        // Setup with optimistic case
        // i.e. Everything fits. No button needed
        if(toggleView!=null)
            toggleView.setVisibility(View.GONE);
        setMaxLines(Integer.MAX_VALUE);

        // Measure
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // If the text fits in collapsed mode, we are done.
        if (getLineCount() <= mMaxCollapsedLines) {
            return;
        }

        // Saves the text height w/ max lines
        mTextHeightWithMaxLines = getRealTextViewHeight();

        // Doesn't fit in collapsed mode. Collapse text view as needed. Show
        // button.
        if (mCollapsed) {
            setMaxLines(mMaxCollapsedLines);
        }
        if(toggleView!=null)
            toggleView.setVisibility(View.VISIBLE);

        // Re-measure with new setup
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mCollapsed) {
            mCollapsedHeight = getMeasuredHeight();
        }
    }

    private int getRealTextViewHeight() {
        int textHeight = getLayout().getLineTop(getLineCount());
        int padding = getCompoundPaddingTop() + getCompoundPaddingBottom();
        return textHeight + padding;
    }

    @Override
    public void onClick(View v) {
        if (toggleView.getVisibility() != View.VISIBLE) {
            return;
        }
        mCollapsed = !mCollapsed;

        if (mCollapsedStatus != null) {
            mCollapsedStatus.put(mPosition, mCollapsed);
        }

        // mark that the animation is in progress
        mAnimating = true;

        Animation animation;
        ObjectAnimator ivOa;

        if (mCollapsed) {
            animation = new ExpandCollapseAnimation(this, getHeight(), mCollapsedHeight);
            ivOa = ObjectAnimator.ofFloat(toggleView, "rotation", 180f, 360f);
        } else {
            ivOa = ObjectAnimator.ofFloat(toggleView, "rotation", 0f, 180f);
            animation = new ExpandCollapseAnimation(this, getHeight(),mTextHeightWithMaxLines);
        }

        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                applyAlphaAnimation(ToggleExpandableTextView.this, mAnimAlphaStart);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                // clear animation here to avoid repeated applyTransformation() calls
                clearAnimation();
                // clear the animation flag
                mAnimating = false;

                // notify the listener
                if (mListener != null) {
                    mListener.onExpandStateChanged(ToggleExpandableTextView.this, !mCollapsed);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        clearAnimation();
        startAnimation(animation);
        if(useToggleDefualtAnimator){
            ivOa.setDuration(mAnimationDuration).start();
        }

    }


    class ExpandCollapseAnimation extends Animation {
        private final View mTargetView;
        private final int mStartHeight;
        private final int mEndHeight;

        public ExpandCollapseAnimation(View view, int startHeight, int endHeight) {
            mTargetView = view;
            mStartHeight = startHeight;
            mEndHeight = endHeight;
            setDuration(mAnimationDuration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final int newHeight = (int)((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
            setMaxHeight(newHeight - mMarginBetweenTxtAndBottom);
            if (Float.compare(mAnimAlphaStart, 1.0f) != 0) {
                applyAlphaAnimation(ToggleExpandableTextView.this, mAnimAlphaStart + interpolatedTime * (1.0f - mAnimAlphaStart));
            }
            mTargetView.getLayoutParams().height = newHeight;
            mTargetView.requestLayout();
        }

        @Override
        public void initialize( int width, int height, int parentWidth, int parentHeight ) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds( ) {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void applyAlphaAnimation(View view, float alpha) {
        if (isPostHoneycomb()) {
            view.setAlpha(alpha);
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
            // make it instant
            alphaAnimation.setDuration(0);
            alphaAnimation.setFillAfter(true);
            view.startAnimation(alphaAnimation);
        }
    }
    private static boolean isPostHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }



    public interface OnExpandStateChangeListener {
        /**
         * Called when the expand/collapse animation has been finished
         *
         * @param textView - TextView being expanded/collapsed
         * @param isExpanded - true if the TextView has been expanded
         */
        void onExpandStateChanged(TextView textView, boolean isExpanded);
    }
}
