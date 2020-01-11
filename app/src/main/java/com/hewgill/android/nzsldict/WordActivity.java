package com.hewgill.android.nzsldict;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

public class WordActivity extends BaseActivity {

    private TextView gloss;
    private TextView minor;
    private TextView maori;
    private FlexboxLayout tagsLayout;
    private SignVideoFragment mVideoFragment;
    private ViewPager viewPager;
    private Dictionary.DictItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word);
        setupAppToolbar();
        Intent intent = getIntent();
        item = (Dictionary.DictItem) intent.getSerializableExtra("item");

        gloss = (TextView) findViewById(R.id.gloss);
        minor = (TextView) findViewById(R.id.minor);
        maori = (TextView) findViewById(R.id.maori);
        tagsLayout = (FlexboxLayout) findViewById(R.id.tags);
        viewPager = (ViewPager) findViewById(R.id.sign_media_pager);
        setupSignMediaPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sign_tabs);
        tabLayout.setupWithViewPager(viewPager);

        gloss.setText(item.gloss);
        minor.setText(item.minor);
        maori.setText(item.maori);

        // fetch link colour from styles.xml
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        String linkColor = String.format("%X", typedValue.data).substring(2); // strip alpha value

        // create the list of tags
        for (int i = 0; i < item.categories.size(); i++){
            TextView tv = new TextView(this);
            String text = item.categories.get(i).toLowerCase();
            text = String.format("<font color=\"#%s\"><b><u>%s</u></b></font>", linkColor, text);

            // add trailing comma
            if (i < item.categories.size() - 1) {
                text += ", ";
            }
            tv.setText(Html.fromHtml(text));

            tv.setTag(item.categories.get(i));
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent next = new Intent();
                    next.setClass(WordActivity.this, CategoryActivity.class);
                    next.putExtra("category", (String) v.getTag());
                    startActivity(next);
                }
            });
            tagsLayout.addView(tv);
        }
    }

    private void setupSignMediaPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(SignIllustrationFragment.newInstance(item), "Illustration");
        mVideoFragment = SignVideoFragment.newInstance(item);
        adapter.addFragment(mVideoFragment, "Video");
        viewPager.addOnPageChangeListener(new WordPageChangeListener(viewPager));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private class WordPageChangeListener implements ViewPager.OnPageChangeListener {
        private final ViewPager mPager;

        private WordPageChangeListener(ViewPager pager) {
            mPager = pager;
        }
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            ViewPagerAdapter vpa = (ViewPagerAdapter) mPager.getAdapter();
            if (vpa.getPageTitle(position).equals("Video")) {
                mVideoFragment.start();
            } else {
                mVideoFragment.stop();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
