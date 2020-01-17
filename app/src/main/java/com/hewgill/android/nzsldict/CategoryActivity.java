package com.hewgill.android.nzsldict;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CategoryActivity extends BaseActivity {
    private Dictionary dictionary;
    private ListView mSearchResultsList;
    private DictAdapter adapter;
    private Dictionary.DictCategory category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dictionary = Dictionary.getInstance(getApplicationContext());
        // following based on http://stackoverflow.com/questions/1737009/how-to-make-a-nice-looking-listview-filter-on-android
        setContentView(R.layout.category);
        setupAppToolbar();

        mSearchResultsList = (ListView) findViewById(android.R.id.list);
        String categoryStr = getIntent().getStringExtra("category");
        category = dictionary.getCategories().get(categoryStr);

        adapter = new DictAdapter(this, R.layout.list_item, category.words);
        getListView().setAdapter(adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick((ListView) parent, view, position, id);
            }
        });
    }

    public ListView getListView() {
        return mSearchResultsList;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        Dictionary.DictItem item = (Dictionary.DictItem) getListView().getItemAtPosition(position);
        Log.d("list", item.gloss);
        Intent next = new Intent();
        next.setClass(this, WordActivity.class);
        next.putExtra("item", item);
        startActivity(next);
    }

    class DictAdapter extends BaseAdapter {
        private int resource;
        private List<Dictionary.DictItem> words;
        private LayoutInflater inflater;
        private Filter filter;

        public DictAdapter(Context context, int resource, List<Dictionary.DictItem> words) {
            this.resource = resource;
            this.words = words;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return words.size();
        }

        @Override
        public Dictionary.DictItem getItem(int position) {
            return words.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                v = inflater.inflate(resource, parent, false);
            } else {
                v = convertView;
            }
            TextView gv = (TextView) v.findViewById(R.id.item_gloss);
            TextView mv = (TextView) v.findViewById(R.id.item_minor);
            TextView mtv = (TextView) v.findViewById(R.id.item_maori);
            ImageView dv = (ImageView) v.findViewById(R.id.diagram);
            if (position >= getCount()) {
                Log.e("filter", "request for item " + position + " in list of size " + getCount());
                return v;
            }
            Dictionary.DictItem item = getItem(position);
            gv.setText(item.gloss);
            mv.setText(item.minor);
            mtv.setText(item.maori);

            try {
                InputStream ims = getAssets().open(item.imagePath());
                Drawable d = Drawable.createFromStream(ims, null);
                dv.setImageDrawable(d);
            } catch (IOException e) {
                dv.setImageDrawable(null);
                System.out.println(e.toString());
            }
            return v;
        }
    }
}
