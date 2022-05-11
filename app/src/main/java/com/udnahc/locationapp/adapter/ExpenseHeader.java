package com.udnahc.locationapp.adapter;


import android.view.View;
import android.widget.TextView;

import com.udnahc.locationapp.R;
import com.udnahc.locationapp.util.Utils;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ExpenseHeader extends AbstractHeaderItem<ExpenseHeader.HeaderViewHolder> {
    private String title;

    public ExpenseHeader(String title) {
        this.title = title;
        setHidden(true);
        setSelectable(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpenseHeader that = (ExpenseHeader) o;

        return title.equals(that.title);

    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.header_layout;
    }

    @Override
    public int getSpanSize(int spanCount, int position) {
        return spanCount;
    }

    @Override
    public HeaderViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new HeaderViewHolder(view, adapter, true);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
        holder.headerTextView.setText(title);
    }

    class HeaderViewHolder extends FlexibleViewHolder {
        private TextView headerTextView;

        HeaderViewHolder(View view, FlexibleAdapter adapter, boolean stickyHeader) {
            super(view, adapter, stickyHeader);
            headerTextView = view.findViewById(R.id.header_title);
            Utils.setFont(view);
            view.setBackgroundColor(Utils.getAlphaColorAccent(view.getContext()));
        }
    }
}
