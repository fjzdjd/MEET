package com.qq.cn;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class DragPager extends RelativeLayout {

	private View mMenu; // 菜单
	private View mContent; // 内容视图

	private int mStartx = 0; // 点击开始
	private float mContentStartTransX = 0; // 内容视图开始点击滑动的距离
	private float mMenuStartTransX = 0;// 菜单开始点击滑动的距离
	private int DEFAULT_RIGHT_MARGIN = getResources().getDisplayMetrics().widthPixels / 4;// 菜单距离边界距离在取的1/4屏幕

	ObjectAnimator mContent_animator;// 内容视图动画
	ObjectAnimator mMenu_animator;// 菜单动画
	private boolean misDrag = false; // 当前是否拖动
	final int DEFALT_DRAG_DISTENCE = 40;// 认为滑动的缺省值

	public DragPager(Context context) {
		super(context);

	}

	public DragPager(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

	}

	public DragPager(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void setMenu(View menu) {
		mMenu = menu;
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		layoutParams.rightMargin = DEFAULT_RIGHT_MARGIN;
		addView(menu, layoutParams);
		mMenu.setScaleY(0.75f);// 初始化Y到0.75这么高
		mMenu.setTranslationX(-DEFAULT_RIGHT_MARGIN * 3);// 初始化菜单到-Y距离
	}

	public void setContent(View content) {
		mContent = content;
		addView(content);
	}

	/**
	 * 计算当前MenuX可滑动的位置
	 * 
	 * @param distence
	 * @return
	 */
	private float getMenuDragX(float distence) {
		float newX = distence + mMenuStartTransX;
		if (newX <= -DEFAULT_RIGHT_MARGIN * 3) {
			newX = -DEFAULT_RIGHT_MARGIN * 3;
		} else if (newX >= 0) {
			newX = 0;
		}
		return newX;
	}

	/**
	 * 计算内容视图X滑动的位置
	 * 
	 * @param distence
	 * @return
	 */
	private float getContentDragX(float distence) {

		float newX = distence + mContentStartTransX;
		if (newX <= 0) {
			newX = 0;
		} else if (newX >= mContent.getWidth() - DEFAULT_RIGHT_MARGIN) {
			newX = mContent.getWidth() - DEFAULT_RIGHT_MARGIN;
		}
		return newX;
	}

	/**
	 * 停止动画
	 */
	private void stopAnimation() {
		if (mContent_animator != null && mMenu_animator != null) {
			mContent_animator.cancel();
			mMenu_animator.cancel();
		}
	}

	// 移动
	private void move(float distence) {
		float nowx = getContentDragX(distence);
		if (nowx != mContent.getTranslationX()) {
			mContent.setTranslationX(nowx);
			float scale = nowx / (mContent.getWidth() - DEFAULT_RIGHT_MARGIN); // 计算alph范围是0-1
			mContent.setScaleY(1 - scale * 0.25f); // 内容的Y缩放
			mMenu.setTranslationX(getMenuDragX(distence)); // 设置菜单的距离
			mMenu.setScaleY(0.75f + scale * 0.25f);// 设置菜单的Y缩放
			mMenu.setAlpha(scale);// 菜单的Alpha
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			stopAnimation(); // 停止动画
			mStartx = (int) ev.getRawX(); // 获取点击初始化x
			if (mContent != null)
				mContentStartTransX = mContent.getTranslationX();// 获取初始化x
			if (mMenu != null)
				mMenuStartTransX = mMenu.getTranslationX();
			break;
		case MotionEvent.ACTION_MOVE:
			// 当左右滑动到阀值就认为是在拖动了
			if (Math.abs(ev.getRawX() - mStartx) > DEFALT_DRAG_DISTENCE) {
				misDrag = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			misDrag = false;
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (misDrag) {
			return true;// 不进行向下分发了已经拦截
		} else {
			return false; // 把事件交给子view处理然后通过子view的dispatch分发
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionIndex() > 1)
			return true;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			return true; // 消费掉这个事件让它不传递
		case MotionEvent.ACTION_MOVE:
			float distence = event.getRawX() - mStartx; // 在这里处理移动
			move(distence);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			toggle(); // 窗口回弹
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 回弹的动画
	 */
	private void toggle() {
		if (mContent.getTranslationX() > DEFAULT_RIGHT_MARGIN * 2) {
			// 向右动画
			// 先是content动画
			PropertyValuesHolder content_transani = PropertyValuesHolder
					.ofFloat("translationX", mContent.getTranslationX(),
							mContent.getWidth() - DEFAULT_RIGHT_MARGIN);
			PropertyValuesHolder content_ScaleY = PropertyValuesHolder.ofFloat(
					"scaleY", mContent.getScaleY(), 0.75f);
			mContent_animator = ObjectAnimator.ofPropertyValuesHolder(mContent,
					content_transani, content_ScaleY);
			mContent_animator.start();

			// 这里菜单动画

			PropertyValuesHolder menu_transani = PropertyValuesHolder.ofFloat(
					"translationX", mMenu.getTranslationX(), 0);
			PropertyValuesHolder menu_ScaleY = PropertyValuesHolder.ofFloat(
					"scaleY", mMenu.getScaleY(), 1f);
			PropertyValuesHolder menu_Alpha = PropertyValuesHolder.ofFloat(
					"Alpha", mMenu.getAlpha(), 1f);
			mMenu_animator = ObjectAnimator.ofPropertyValuesHolder(mMenu,
					menu_transani, menu_ScaleY, menu_Alpha);
			mMenu_animator.start();

		} else {
			// 向左动画
			PropertyValuesHolder content_transanx = PropertyValuesHolder
					.ofFloat("translationX", mContent.getTranslationX(), 0);
			PropertyValuesHolder content_ScaleY = PropertyValuesHolder.ofFloat(
					"scaleY", mContent.getScaleY(), 1.0f);
			mContent_animator = ObjectAnimator.ofPropertyValuesHolder(mContent,
					content_transanx, content_ScaleY);
			mContent_animator.start();

			PropertyValuesHolder menu_transani = PropertyValuesHolder.ofFloat(
					"translationX", mMenu.getTranslationX(),
					-DEFAULT_RIGHT_MARGIN * 3);
			PropertyValuesHolder menu_ScaleY = PropertyValuesHolder.ofFloat(
					"scaleY", mMenu.getScaleY(), 0.75f);
			PropertyValuesHolder menu_Alpha = PropertyValuesHolder.ofFloat(
					"Alpha", mMenu.getAlpha(), 0f);
			mMenu_animator = ObjectAnimator.ofPropertyValuesHolder(mMenu,
					menu_transani, menu_ScaleY, menu_Alpha);
			mMenu_animator.start();
		}

	}

}