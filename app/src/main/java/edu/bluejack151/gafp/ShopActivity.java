package edu.bluejack151.gafp;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

public class ShopActivity extends FragmentActivity implements ThemeFragment.OnFragmentInteractionListener,AvatarFragment.OnFragmentInteractionListener,CollectiblesFragment.OnFragmentInteractionListener{
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    private GestureDetector gestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    float diffAbs = Math.abs(e1.getY() - e2.getY());
                    float diff = e1.getX() - e2.getX();

                    if (diffAbs > 150)
                        return false;

                    // Left swipe
                    if (diff > 120 && Math.abs(velocityX) > 100) {
                        if(mPager.getCurrentItem() > 0){
                            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                        }
                    } // Right swipe
                    else if (-diff > 120 && Math.abs(velocityX) > 100) {
                        if(mPager.getCurrentItem() < 1){
                            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                        }
                    }
                } catch (Exception e) {
                    Log.e("YourActivity", "Error on gestures");
                }
                return false;
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        gestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }


    @Override
    public void onBackPressed() {
//        if (mPager.getCurrentItem() == 0) {
//            // If the user is currently looking at the first step, allow the system to handle the
//            // Back button. This calls finish() on this activity and pops the back stack.
//            super.onBackPressed();
//        } else {
//            // Otherwise, select the previous step.
//            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
//        }
        Toast.makeText(ShopActivity.this, "Exiting...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0)
                return new ThemeFragment();
            else if(position == 2)
                return new CollectiblesFragment();
            else if(position == 1)
                return new AvatarFragment();
            else
                return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


}
