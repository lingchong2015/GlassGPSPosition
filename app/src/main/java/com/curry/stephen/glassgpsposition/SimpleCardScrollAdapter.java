package com.curry.stephen.glassgpsposition;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingchong on 2017/5/25.
 */

public class SimpleCardScrollAdapter extends CardScrollAdapter {

    private List<CardBuilder> mCardBuilders = new ArrayList<CardBuilder>();

    public List<CardBuilder> getCardBuilders() {
        return mCardBuilders;
    }

    public void setCardBuilders(List<CardBuilder> cardBuilders) {
        mCardBuilders = cardBuilders;
    }

    @Override
    public int getCount() {
        return mCardBuilders.size();
    }

    @Override
    public Object getItem(int i) {
        return mCardBuilders.get(i);
    }

    @Override
    public int getItemViewType(int position){
        return mCardBuilders.get(position).getItemViewType();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return mCardBuilders.get(i).getView(view, viewGroup);
    }

    @Override
    public int getPosition(Object o) {
        int index = mCardBuilders.indexOf(o);
        return index < 0 ? AdapterView.INVALID_POSITION : index;
    }

    public void insertCardWithoutNotification(int position, CardBuilder card) {
        mCardBuilders.add(position, card);
    }
}
