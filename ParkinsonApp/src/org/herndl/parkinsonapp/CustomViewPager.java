package org.herndl.parkinsonapp;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

// custom ViewPage which is capable of disabling swipe on given pages
public class CustomViewPager extends ViewPager {

	private List<Integer> swipeDisabledPages = null;

	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public List<Integer> getSwipeDisabledPages() {
		return swipeDisabledPages;
	}

	public void setSwipeDisabledPages(List<Integer> swipeDisabledPages) {
		this.swipeDisabledPages = swipeDisabledPages;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		// disable swiping
		if (swipeDisabledPages != null
				&& swipeDisabledPages.contains(getCurrentItem()))
			return false;
		else
			return super.onInterceptTouchEvent(arg0);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		// disable swiping
		if (swipeDisabledPages != null
				&& swipeDisabledPages.contains(getCurrentItem()))
			return false;
		else
			return super.onTouchEvent(arg0);
	}

}