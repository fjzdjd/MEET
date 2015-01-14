package com.qq.cn;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class DragPager extends RelativeLayout {

	private View mMenu; // �˵�
	private View mContent; // ������ͼ

	private int mStartx = 0; // �����ʼ
	private float mContentStartTransX = 0; // ������ͼ��ʼ��������ľ���
	private float mMenuStartTransX = 0;// �˵���ʼ��������ľ���
	private int DEFAULT_RIGHT_MARGIN = getResources().getDisplayMetrics().widthPixels / 4;// �˵�����߽������ȡ��1/4��Ļ

	ObjectAnimator mContent_animator;// ������ͼ����
	ObjectAnimator mMenu_animator;// �˵�����
	private boolean misDrag = false; // ��ǰ�Ƿ��϶�
	final int DEFALT_DRAG_DISTENCE = 40;// ��Ϊ������ȱʡֵ

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
		mMenu.setScaleY(0.75f);// ��ʼ��Y��0.75��ô��
		mMenu.setTranslationX(-DEFAULT_RIGHT_MARGIN * 3);// ��ʼ���˵���-Y����
	}

	public void setContent(View content) {
		mContent = content;
		addView(content);
	}

	/**
	 * ���㵱ǰMenuX�ɻ�����λ��
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
	 * ����������ͼX������λ��
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
	 * ֹͣ����
	 */
	private void stopAnimation() {
		if (mContent_animator != null && mMenu_animator != null) {
			mContent_animator.cancel();
			mMenu_animator.cancel();
		}
	}

	// �ƶ�
	private void move(float distence) {
		float nowx = getContentDragX(distence);
		if (nowx != mContent.getTranslationX()) {
			mContent.setTranslationX(nowx);
			float scale = nowx / (mContent.getWidth() - DEFAULT_RIGHT_MARGIN); // ����alph��Χ��0-1
			mContent.setScaleY(1 - scale * 0.25f); // ���ݵ�Y����
			mMenu.setTranslationX(getMenuDragX(distence)); // ���ò˵��ľ���
			mMenu.setScaleY(0.75f + scale * 0.25f);// ���ò˵���Y����
			mMenu.setAlpha(scale);// �˵���Alpha
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			stopAnimation(); // ֹͣ����
			mStartx = (int) ev.getRawX(); // ��ȡ�����ʼ��x
			if (mContent != null)
				mContentStartTransX = mContent.getTranslationX();// ��ȡ��ʼ��x
			if (mMenu != null)
				mMenuStartTransX = mMenu.getTranslationX();
			break;
		case MotionEvent.ACTION_MOVE:
			// �����һ�������ֵ����Ϊ�����϶���
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
			return true;// ���������·ַ����Ѿ�����
		} else {
			return false; // ���¼�������view����Ȼ��ͨ����view��dispatch�ַ�
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionIndex() > 1)
			return true;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			return true; // ���ѵ�����¼�����������
		case MotionEvent.ACTION_MOVE:
			float distence = event.getRawX() - mStartx; // �����ﴦ���ƶ�
			move(distence);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			toggle(); // ���ڻص�
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * �ص��Ķ���
	 */
	private void toggle() {
		if (mContent.getTranslationX() > DEFAULT_RIGHT_MARGIN * 2) {
			// ���Ҷ���
			// ����content����
			PropertyValuesHolder content_transani = PropertyValuesHolder
					.ofFloat("translationX", mContent.getTranslationX(),
							mContent.getWidth() - DEFAULT_RIGHT_MARGIN);
			PropertyValuesHolder content_ScaleY = PropertyValuesHolder.ofFloat(
					"scaleY", mContent.getScaleY(), 0.75f);
			mContent_animator = ObjectAnimator.ofPropertyValuesHolder(mContent,
					content_transani, content_ScaleY);
			mContent_animator.start();

			// ����˵�����

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
			// ���󶯻�
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