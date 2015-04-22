package com.zzy.zoom;


import java.util.ArrayList;

import com.zzy.zoom.view.ZoomImageView;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends Activity {

	private ViewPager viewPager;
	private ZoomImageView imageView1;
	private ZoomImageView imageView2;
	private ZoomImageView imageView3;
	private ArrayList<ZoomImageView> arrays;
	private int[] images=new int[]{R.drawable.skin_ios7_preview2,
			R.drawable.skin_ios7_preview3,R.drawable.skin_ios7_thumb};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		viewPager = (ViewPager)findViewById(R.id.viewPage);
		imageView1 = new ZoomImageView(getApplicationContext());
		imageView2= new ZoomImageView(getApplicationContext());
		imageView3 = new ZoomImageView(getApplicationContext());
		arrays = new ArrayList<ZoomImageView>();
		arrays.add(imageView1);
		arrays.add(imageView2);
		arrays.add(imageView3);
		viewPager.setAdapter(new PagerAdapter() {
			
			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				// TODO Auto-generated method stub
				container.removeView(arrays.get(position));
			}
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				// TODO Auto-generated method stub
				return arg0 == arg1;
			}
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				// TODO Auto-generated method stub
				ZoomImageView imageView=arrays.get(position);
				imageView.setImageResource(images[position]);
				container.addView(imageView);
				return imageView;
			}
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return arrays.size();
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
