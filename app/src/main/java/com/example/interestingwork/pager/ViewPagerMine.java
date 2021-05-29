package com.example.interestingwork.pager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

//参考博客：https://guolin.blog.csdn.net/article/details/48719871
//虽参考博客，但代码自己敲的
public class ViewPagerMine extends ViewGroup {
    public ViewPagerMine(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        slop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //widthMeasureSpec 是包含match parent信息的宽度
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childCount = getChildCount();
            int leftStartLayout = 0;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
//                child.getWidth() 是获取不到宽度的
                child.layout(leftStartLayout, 0, leftStartLayout + child.getMeasuredWidth(), child.getMeasuredHeight());
                leftStartLayout = leftStartLayout + child.getMeasuredWidth();
            }
            if (childCount > 0) {
                leftScrollBorder = getChildAt(0).getLeft();

                //最多向右滑动距离。所有孩子宽度相加 减去 父亲的宽度
                rightScrollBorder = 0;
                for (int i = 0; i < childCount; i++) {
                    rightScrollBorder += getChildAt(i).getMeasuredWidth();
                }
                rightScrollBorder = rightScrollBorder - getMeasuredWidth();

                childWidth = getChildAt(0).getMeasuredWidth();  //先假设每个孩子的宽度都一样
            }
        }
    }

    //Scroller只是个计算器，提供插值计算，让滚动过程具有动画属性，但它并不是UI，也不是辅助UI滑动，反而是单纯地为滑动提供计算。
    private Scroller scroller;
    private int slop;
    private float mLastMotionX;  //用于判断是否拦截滑动事件，与父布局边界判断
    private int leftScrollBorder; //最多向左滑动距离
    private int rightScrollBorder;//最多向右滑动距离。所有孩子宽度相加 减去 父亲的宽度
    private int childWidth;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //getX是相对父布局的。getRawX是相对最外层的
                mLastMotionX = ev.getRawX();
//                scroller.abortAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                float diff = Math.abs(ev.getRawX() - mLastMotionX);
                //大于slop值的时候，认为应该滚动，拦截事件
                if (diff > slop) {
                    mLastMotionX = ev.getRawX();
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                //mLastMotionX放前面。如要组件向右滑动：点击的位置 大于 event.getRawX获得的位置
                float moveX = mLastMotionX - event.getRawX();
                //左边界
                if (getScrollX() + moveX < leftScrollBorder) {
                    scrollTo(leftScrollBorder, 0);
                    return true;
                }
                //右边界。
                if (getScrollX() + moveX > rightScrollBorder) {
                    //有一个小bug：当最右边不是满屏时，向左滑，位置会跳动
                    scrollTo(rightScrollBorder, 0);
                    return true;
                }
                scrollBy((int) moveX, 0);
                mLastMotionX = event.getRawX();
                break;

            case MotionEvent.ACTION_UP:
                //下面实现的是弹性滑动。先假设每个孩子的宽度都一样
                //滑过超过一半认为要进入下一个条目
                int targetIndex = (childWidth / 2 + getScrollX()) / childWidth;
                //滑到一个条目左边界还剩下的距离
                int dx = targetIndex * childWidth - getScrollX();
                //调用startScroll()方法来初始化滚动数据并刷新界面
                scroller.startScroll(getScrollX(), 0, dx, 0);
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    //computeScroll也不是来让ViewGroup滑动的，真正让ViewGroup滑动的是scrollTo,scrollBy。
    // computeScroll的作用是计算ViewGroup如何滑动。而computeScroll是通过draw来调用的。
    //以下的机制：draw调用computeScroll，在这个方法中，当scroller中的值还没消耗完，又会调用invalidate方法去draw，以此循环起来
    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
//            invalidate()函数的主要作用是请求View树进行重绘,重绘时又会调用到computeScroll，以此连续起来
            invalidate();
        }
    }
}
