package com.zzy.zoom.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.view.View.OnTouchListener;

public class ZoomImageView extends ImageView
		implements
			OnGlobalLayoutListener,
			OnScaleGestureListener,
			OnTouchListener {

	public boolean mOnce;
	// 一开始缩放比例
	private float mInitScale;
	// 最大缩放比例
	private float mMaxScale;
	// 双击时候放大比例
	private float mMinScale;
	// 缩放移动矩阵
	private Matrix matrix;

	// 手指移动时候最后点
	private float mLastX, mLastY;

	// 多点触控手指的数量
	private int mLastPonitCount;

	private ScaleGestureDetector scaleGestureDetector;
	// 双击监听
	private GestureDetector gestureDetector;
	// 获取系统提供的可移动距离比较值
	private int mScaledSlop;
	private boolean isCanDrag;
	private boolean isAutoScale;
	public boolean isCheckLeftAndRight, isCheckBottomAndTop;
	public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ZoomImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public ZoomImageView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public void init() {
		matrix = new Matrix();
		scaleGestureDetector = new ScaleGestureDetector(getContext(), this);
		gestureDetector = new GestureDetector(getContext(),
				new GestureDetector.SimpleOnGestureListener() {

					@Override
					public boolean onDoubleTap(MotionEvent e) {

						if (isAutoScale) {
							return true;
						}
						float x = e.getX();
						float y = e.getY();
						if (getScale() < mMinScale) {
							// 放大两倍
							/*
							 * matrix.postScale(mMinScale / getScale(),
							 * mMinScale / getScale(), x, y);
							 */
							postDelayed(new AutoScaleRunnable(mMinScale, x, y),
									20);
							isAutoScale = true;
						} else {
							// 缩小
							/*
							 * matrix.postScale(mInitScale / getScale(),
							 * mInitScale / getScale(), x, y);
							 */
							postDelayed(
									new AutoScaleRunnable(mInitScale, x, y), 20);
							isAutoScale = true;
						}

						return true;
					}
				});
		setScaleType(ScaleType.MATRIX);
		setOnTouchListener(this);

		mScaledSlop = ViewConfiguration.get(getContext()).getScaledEdgeSlop();
	}
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}
	@SuppressWarnings("deprecation")
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}
	/**
	 * 利用Runnable实现动画缩放效果
	 * 
	 * @author ziyang
	 * 
	 */
	public class AutoScaleRunnable implements Runnable {

		private float targetScale;
		private float x = 0, y = 0;
		private final float BIGGER = 1.08f;
		private final float SMALLER = 0.91f;

		private float tempScale;

		public AutoScaleRunnable(float targetScale, float x, float y) {
			this.targetScale = targetScale;
			this.x = x;
			this.y = y;
			if (getScale() < targetScale) {
				tempScale = BIGGER;
			} else {
				tempScale = SMALLER;
			}

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			matrix.postScale(tempScale, tempScale, x, y);
			checkBorder();
			setImageMatrix(matrix);
			float currentScale = getScale();
			if ((tempScale > 1.0f && currentScale < targetScale)
					|| (tempScale < 1.0f && currentScale > targetScale)) {
				// 重新加载
				postDelayed(this, 20);
			} else {
				// 当缩放到达目标之后 设置最终结果
				float scale = targetScale / currentScale;
				matrix.postScale(scale, scale, x, y);
				checkBorder();
				setImageMatrix(matrix);
				isAutoScale = false;
			}
		}

	}
	@Override
	public void onGlobalLayout() {
		// TODO Auto-generated method stub
		if (!mOnce) {
			int width = getWidth();
			int height = getHeight();

			Drawable drawable = getDrawable();
			int dw = drawable.getIntrinsicWidth();
			int dh = drawable.getIntrinsicHeight();

			float sclae = 1.0f;
			// 获取缩放放大比例
			if (dw > width && dh < height) {
				sclae = width * 1.0f / dw;
				System.out.println("111111");
			}
			if (dw < width && dh > height) {
				sclae = height * 1.0f / dh;
				System.out.println("22222");
			}
			if (dw < width && dh < height) {
				// 图片被放的太大效果不好
				// sclae = Math.min(width * 1.0f / dw, height * 1.0f / dh);
				System.out.println("333333");
				sclae = 1.0f;
			}
			if (dw > width && dh > height) {
				sclae = Math.max(width * 1.0f / dw, height * 1.0f / dh);
				System.out.println("444444");
			}
			mOnce = true;

			mInitScale = sclae;
			mMinScale = sclae * 2;
			mMaxScale = sclae * 4;

			// 开始时候将图片移动到屏幕中央显示
			int dx = getWidth() / 2 - dw / 2;
			int dy = getHeight() / 2 - dh / 2;
			// 平移缩放
			matrix.postTranslate(dx, dy);
			matrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
			setImageMatrix(matrix);
		}

	}
	/**
	 * 得到放缩之后的宽和高 以及各点坐标
	 * 
	 * @return
	 */
	private RectF getCurrentRectF() {
		Matrix mMatrix = matrix;
		RectF rectF = new RectF();
		Drawable mDrawable = getDrawable();
		if (rectF != null) {
			rectF.set(0, 0, mDrawable.getIntrinsicWidth(),
					mDrawable.getIntrinsicHeight());
			mMatrix.mapRect(rectF);
		}
		return rectF;
	}
	/**
	 * 控制图片放大之后 图片显示范围
	 */
	public void checkBorder() {
		RectF rectF = getCurrentRectF();
		float delaX = 0, delaY = 0;
		int width = getWidth();
		int height = getHeight();
		// 左右方向
		if (rectF.width() >= width) {
			// 测试---》左边出现白边
			if (rectF.left > 0) {
				delaX = -rectF.left;
			}
			if (rectF.right < width) {
				delaX = width - rectF.right;
			}
		}
		// 垂直方向
		if (rectF.height() >= height) {
			// 上下出现白边
			if (rectF.top > 0) {
				delaY = -rectF.top;
			}
			if (rectF.bottom < height) {
				delaY = height - rectF.bottom;
			}
		}
		// 设置居中在中间位置
		if (rectF.width() < width) {
			delaX = width / 2 - rectF.right + rectF.width() / 2;
		}
		if (rectF.height() < height) {
			delaY = height / 2 - rectF.bottom + rectF.height() / 2;
		}
		matrix.postTranslate(delaX, delaY);
	}

	/**
	 * 返回当前缩放比例
	 * 
	 * @return
	 */
	public float getScale() {
		float[] values = new float[9];
		matrix.getValues(values);
		return values[matrix.MSCALE_X];
	}
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		float currentScale = getScale();
		float mScale = detector.getScaleFactor();

		// mscale >1时候 是放大效果 <1时候是缩小
		if ((currentScale > mInitScale && mScale < 1.0f)
				|| (currentScale < mMaxScale && mScale > 1.0f)) {
			// 设置最终缩小值
			if (currentScale * mScale < mInitScale) {
				mScale = mInitScale / currentScale;

			}
			// 设置最终放大值
			if (currentScale * mScale > mMaxScale) {
				mScale = mMaxScale / currentScale;
			}
			// 已触摸中心点进行缩放
			matrix.postScale(mScale, mScale, detector.getFocusX(),
					detector.getFocusY());
			// 缩放边界问题检测
			checkBorder();
			setImageMatrix(matrix);
		}
		return true;
	}
	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (gestureDetector.onTouchEvent(event)) {
			return true;
		}
		// TODO Auto-generated method stub
		// 绑定 scaleGestureDetector
		scaleGestureDetector.onTouchEvent(event);

		// ////////获取多点触控中心点 这样移动效果更好
		int ponitCount = event.getPointerCount();
		float x = 0, y = 0;
		for (int i = 0; i < ponitCount; i++) {
			x += event.getX(i);
			y += event.getY(i);
		}
		x /= ponitCount;
		y /= ponitCount;
		// 用户滑动时候 手指的数量发生变化
		if (mLastPonitCount != ponitCount) {
			isCanDrag = false;
			mLastX = x;
			mLastY = y;
		}
		mLastPonitCount = ponitCount;
		RectF rectF = getCurrentRectF();
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE :
				//避免与父控件的冲突 防止出现滑动异常 
				if (rectF.width() > getWidth() + 0.01f
						|| rectF.height() > getHeight() + 0.01f) {
					getParent().requestDisallowInterceptTouchEvent(true);
				}
				float dx = x - mLastX;
				float dy = y - mLastY;
				if (!isCanDrag) {
					isCanDrag = isMoveAction(dx, dy);
				}
				if (isCanDrag) {
					if (getDrawable() != null) {
						isCheckBottomAndTop = isCheckLeftAndRight = true;
						// 图片在屏幕内 没必要使其移动
						if (rectF.width() < getWidth()) {
							isCheckLeftAndRight = false;
							dx = 0;
						}
						if (rectF.height() < getHeight()) {
							isCheckBottomAndTop = false;
							dy = 0;
						}
						matrix.postTranslate(dx, dy);
						checkBorderWhenTranslate();
						setImageMatrix(matrix);
					}
				}
				mLastX = x;
				mLastY = y;

				break;
			case MotionEvent.ACTION_UP :
				break;
			case MotionEvent.ACTION_CANCEL :
				mLastPonitCount = 0;
				break;
			case MotionEvent.ACTION_DOWN :
				// 每次手指落下 就记录当前位置坐标
				mLastX = event.getX();
				mLastY = event.getY();
                //避免与父控件的冲突 防止出现滑动异常 
				if (rectF.width() > getWidth() + 0.01f
						|| rectF.height() > getHeight() + 0.01f) {
					getParent().requestDisallowInterceptTouchEvent(true);
				}
				break;
			default :
				break;
		}
		return true;
	}
	/**
	 * 控制放大之后图片移动的范围，测试多次 否则图片移除边界
	 */
	public void checkBorderWhenTranslate() {
		RectF rectF = getCurrentRectF();
		float delaX = 0, delaY = 0;
		int width = getWidth();
		int height = getHeight();
		if (rectF.left > 0 && isCheckLeftAndRight) {
			delaX = -rectF.left;
		}
		if (rectF.right < width && isCheckLeftAndRight) {
			delaX = width - rectF.right;
		}
		if (rectF.top > 0 && isCheckBottomAndTop) {
			delaY = -rectF.top;
		}
		if (rectF.bottom < height && isCheckBottomAndTop) {
			delaY = height - rectF.bottom;
		}
		matrix.postTranslate(delaX, delaY);

	}
	/**
	 * 判断移动的距离是否可以在移动的范围内
	 * 
	 * @param dx
	 * @param dy
	 * @return
	 */
	public boolean isMoveAction(float dx, float dy) {
		return Math.sqrt(dx * dx + dy * dy) > mScaledSlop;
	}

}
