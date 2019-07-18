package com.example.uk.locationtaskremainder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class OnboardingActivity extends AppCompatActivity {

    /**
     * Views required for on-boarding fragments.
     */
    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private Button btnSkip, btnNext;

    /**
     * Stores the image resource id that is used to display on-boarding items.
     */
    int[] imageResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        setOnboardingSeen();

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);

        // Contains images that will be shown on different onoarding screens.
        imageResources = new int[]{
                R.drawable.ic_onboard_route_trimmed,
                R.drawable.ic_onboard_assistant_trimmed,
                R.drawable.ic_onboard_productivity
        };
        // adding bottom dots
        addBottomDots(0);

        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        // Listen to skip events.
        btnSkip.setOnClickListener(v -> launchHomeScreen());

        btnNext.setOnClickListener(v -> {
            // checking for last page
            // if last page home screen will be launched
            int current = getItem(+1);
            if (current < imageResources.length) {
                // move to next screen
                viewPager.setCurrentItem(current);
            } else {
                launchHomeScreen();
            }
        });
    }

    /**
     * Sets the field that onboarding has been displayed so that it is not displayed again.
     */
    private void setOnboardingSeen() {
        SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(this).edit();
        ed.putBoolean(getString(R.string.pref_has_user_seen_onboarding), true);
        ed.apply();
    }

    /**
     * Adds the bottom dots to the current page.
     *
     * @param currentPage page number, zero based.
     */
    private void addBottomDots(int currentPage) {
        TextView[] dots = new TextView[imageResources.length];

        int colorActiveDot = ContextCompat.getColor(this, R.color.dot_light_active);
        int colorInactiveDot = ContextCompat.getColor(this, R.color.dot_dark_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorInactiveDot);
            dotsLayout.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(colorActiveDot);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    /**
     * Launches the home screen.
     */
    private void launchHomeScreen() {
        startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager
            .OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == imageResources.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(R.string.onboarding_got_it);
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(R.string.onboarding_next);
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // Do nothing.
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // Do nothing.
        }
    };

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        private String[] titles;
        private String[] descriptions;

        MyViewPagerAdapter() {
            titles = getResources().getStringArray(R.array.onboarding_titles);
            descriptions = getResources().getStringArray(R.array.onboarding_descriptions);
            // For error checking.
            if (titles.length != descriptions.length || titles.length != imageResources.length) {
                finish();
            }
        }

        @Override
        @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (layoutInflater == null) {
                finish();
            }
            View view = layoutInflater.inflate(R.layout.fragment_onboarding, container, false);
            ImageView imageView = view.findViewById(R.id.image);
            TextView titleView = view.findViewById(R.id.titleView);
            TextView descriptionView = view.findViewById(R.id.descriptionView);
            // Set the data on views.
            Picasso.with(OnboardingActivity.this)
                    .load(imageResources[position])
                    .into(imageView);
            titleView.setText(titles[position]);
            descriptionView.setText(descriptions[position]);

            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return imageResources.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
