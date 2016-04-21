/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2012 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nengfei.widget;

import com.nengfei.app.R;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This widget implements the dynamic action bar tab behavior that can change
 * across different configurations or circumstances.
 */
public class IconPageIndicator extends HorizontalScrollView implements PageIndicator {
    private final IcsLinearLayout mIconsLayout;

    private ViewPager mViewPager;
    private OnPageChangeListener mListener;
    private Runnable mIconSelector;
    private int mSelectedIndex;

    public IconPageIndicator(Context context) {
        this(context, null);
    }

    public IconPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);

        mIconsLayout = new IcsLinearLayout(context, R.attr.vpiIconPageIndicatorStyle);
        
        addView(mIconsLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));
    }

    private void animateToIcon(final int position) {
        final View iconView = mIconsLayout.getChildAt(position);
        if (mIconSelector != null) {
            removeCallbacks(mIconSelector);
        }
        mIconSelector = new Runnable() {
            public void run() {
                final int scrollPos = iconView.getLeft() - (getWidth() - iconView.getWidth()) / 2;
                smoothScrollTo(scrollPos, 0);
                mIconSelector = null;
            }
        };
        post(mIconSelector);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIconSelector != null) {
            // Re-post the selector we saved
            post(mIconSelector);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mIconSelector != null) {
            removeCallbacks(mIconSelector);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (mListener != null) {
            mListener.onPageScrollStateChanged(arg0);
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (mListener != null) {
            mListener.onPageScrolled(arg0, arg1, arg2);
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        setCurrentItem(arg0);
        if (mListener != null) {
            mListener.onPageSelected(arg0);
        }
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        PagerAdapter adapter = view.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        view.setOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    @SuppressWarnings("deprecation")
	public void notifyDataSetChanged() {
        mIconsLayout.removeAllViews();
        IconPagerAdapter iconAdapter = (IconPagerAdapter) mViewPager.getAdapter();
        int count = iconAdapter.getCount();
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        for (int i = 0; i < count; i++) {
        	LinearLayout layout=new LinearLayout(getContext());  
        	layout.setOrientation(LinearLayout.VERTICAL);
        	layout.setLayoutParams(new LayoutParams(width/count,LinearLayout.LayoutParams.WRAP_CONTENT));
        	layout.setGravity(Gravity.CENTER);
        	layout.setPadding(10,15,10,5);
            ImageView view = new ImageView(getContext(), null, R.attr.vpiIconPageIndicatorStyle);
            view.setImageResource(iconAdapter.getIconResId(i));
             
            view.setLayoutParams(new LayoutParams(48, 48));
             
            TextView txt=new TextView(getContext());
            txt.setText(iconAdapter.getTextResId(i));
            txt.setTextColor(Color.GRAY);
            txt.setGravity(Gravity.CENTER);
            final int j=i;
            
            
            layout.setOnClickListener(new OnClickListener() {		
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					setCurrentItem(j);
				}
			});
           txt.setTextSize(10);
           
            layout.addView(view);
            layout.addView(txt);
        
            
            
            mIconsLayout.addView(layout);
        }
        if (mSelectedIndex > count) {
            mSelectedIndex = count - 1;
        }
        setCurrentItem(mSelectedIndex);
        requestLayout();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mSelectedIndex = item;
        mViewPager.setCurrentItem(item);
        
        int tabCount = mIconsLayout.getChildCount();
        for (int i = 0; i < tabCount; i++) {
        	//其实是个线性布局
            View child = mIconsLayout.getChildAt(i);
            
            boolean isSelected = (i == item);
            if(isSelected){
            	((TextView) ((LinearLayout)child).getChildAt(1)).setTextColor(Color.argb(255, 95, 169, 249));;
            }else{
            	((TextView) ((LinearLayout)child).getChildAt(1)).setTextColor(Color.GRAY);
            }
            child.setSelected(isSelected);
            
            if (isSelected) {
            	
                animateToIcon(item);
            }
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }
}