package com.udnahc.locationapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.udnahc.locationapp.R;
import com.udnahc.locationapp.controller.UtilActivity;
import com.udnahc.locationapp.model.Expense;
import com.udnahc.locationapp.util.Constants;

import java.util.Date;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;

public class OfflineExpenseItem extends ExpenseItem {
    public OfflineExpenseItem(UtilActivity context, Expense listExpense, @Nullable ExpenseHeader header) {
        super(context, listExpense, header);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.expense_layout;
    }

    @Override
    public ExpenseItem.ViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ExpenseItem.ViewHolder holder, int position, List<Object> payloads) {
        StringBuilder builder = new StringBuilder();
        builder.append("Start: ").append(Constants.offlineDateFormat.format(new Date(listExpense.getTimeStamp())));
        if (listExpense.getEndTime() != -1) {
            builder.append("\nEnd : ").append(Constants.offlineDateFormat.format(new Date(listExpense.getEndTime())));
        }
        holder.expenseTitle.setText(builder.toString());
        holder.expenseDate.setText("");
        holder.expenseCost.setText(String.format("Trip - %s miles", listExpense.getMiles()));
    }

    class ViewHolder extends ExpenseItem.ViewHolder {
        private TextView rightView;
        private ViewGroup frontView;

        ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
//            rightView = view.findViewById(R.id.right_view);
//            frontView = view.findViewById(R.id.front_view);
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
