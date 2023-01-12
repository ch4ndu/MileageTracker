package com.udnahc.locationapp.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udnahc.locationapp.R;
import com.udnahc.locationapp.controller.UtilActivity;
import com.udnahc.locationapp.util.Constants;
import com.udnahc.locationapp.util.Utils;
import com.udnahc.locationmanager.Mileage;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Date;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class OfflineExpenseItem extends AbstractFlexibleItem<OfflineExpenseItem.ViewHolder> {
    protected Mileage listExpense;
    private String description = "";

    public OfflineExpenseItem(UtilActivity context, Mileage listExpense) {
        this.listExpense = listExpense;
        getDescription();
    }

    private String getDescription() {
        if (TextUtils.isEmpty(description)) {
            StringBuilder builder = new StringBuilder();
            Date startTime = new Date(listExpense.getTimeStamp());
            DateTime start = new DateTime(startTime);
            builder.append(Constants.offlineDateFormat.format(startTime));
            if (listExpense.getEndTime() != -1) {
                Date endTime = new Date(listExpense.getEndTime());
                DateTime end = new DateTime(endTime);
                Duration temp = new Duration(start, end);
                builder.append(" - ")
                        .append(Constants.offlineEndDateFormat.format(endTime))
                        .append("\nDuration: ")
                        .append(temp.getStandardMinutes())
                        .append(" minutes");

            }
            description = builder.toString();
        }
        return description;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.expense_layout;
    }

    @Override
    public ViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolder holder, int position, List<Object> payloads) {

        holder.expenseTitle.setText(getDescription());
        holder.expenseDate.setText("");
        holder.expenseCost.setText(String.format("Trip - %s miles", listExpense.getMiles()));
    }

    public Mileage getListExpense() {
        return listExpense;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OfflineExpenseItem that = (OfflineExpenseItem) o;
        return listExpense.equals(that.listExpense);
    }

    @Override
    public int hashCode() {
        return ("" + listExpense.getTimeStamp()).hashCode();
    }

    static class ViewHolder extends FlexibleViewHolder {
        private final TextView rightView;
        private final ViewGroup frontView;
        TextView expenseTitle;
        TextView expenseCost;
        TextView expenseDate;
        TextView expenseType;

        ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            Utils.setFont(view);
            expenseTitle = view.findViewById(R.id.expense_title);
            expenseCost = view.findViewById(R.id.expense_cost);
            expenseDate = view.findViewById(R.id.expense_date);
            expenseType = view.findViewById(R.id.expense_category);
            rightView = view.findViewById(R.id.right_view);
            frontView = view.findViewById(R.id.front_view);
        }

        @Override
        public View getFrontView() {
            return frontView;
        }

        @Override
        public View getRearRightView() {
            return rightView;
        }
    }
}
