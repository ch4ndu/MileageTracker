package com.udnahc.locationapp.adapter;


import android.animation.Animator;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.udnahc.locationapp.R;
import com.udnahc.locationapp.controller.UtilActivity;
import com.udnahc.locationapp.model.Expense;
import com.udnahc.locationapp.util.Constants;
import com.udnahc.locationapp.util.Utils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ExpenseItem extends AbstractFlexibleItem<ExpenseItem.ViewHolder>
        implements ISectionable<ExpenseItem.ViewHolder, ExpenseHeader>, IFilterable {

    private final UtilActivity activity;
    @Nullable
    private ExpenseHeader header;
    protected Expense listExpense;

    public ExpenseItem(UtilActivity activity, Expense listExpense, @Nullable ExpenseHeader header) {
        this.listExpense = listExpense;
        this.header = header;
        this.activity = activity;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.expense_layout;
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, final ViewHolder holder, int position, List<Object> payloads) {
        holder.expenseDate.setText(Constants.dateFormat.format(new Date(listExpense.geteDate())));
        holder.expenseCost.setText(String.format("Trip - %s", listExpense.getMiles()));
    }

    @Override
    public ViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ViewHolder(view, adapter);
    }

    public Expense getListExpense() {
        return listExpense;
    }

    @Override
    public void unbindViewHolder(FlexibleAdapter adapter, ViewHolder holder, int position) {
        super.unbindViewHolder(adapter, holder, position);
    }

    @Nullable
    @Override
    public ExpenseHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(@Nullable ExpenseHeader header) {
        this.header = header;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpenseItem that = (ExpenseItem) o;
        return listExpense.equals(that.listExpense);
    }

    @Override
    public int hashCode() {
        return ("" + listExpense.getTimeStamp()).hashCode();
    }

    @Override
    public boolean filter(Serializable constraint) {
        return false;
    }

    static class ViewHolder extends FlexibleViewHolder {
        TextView expenseTitle;
        TextView expenseCost;
        TextView expenseDate;
        TextView expenseType;
        //        ImageView expenseReceipt;

        ViewHolder(View view, final FlexibleAdapter adapter) {
            super(view, adapter);
            Utils.setFont(view);
            expenseTitle = view.findViewById(R.id.expense_title);
            expenseCost = view.findViewById(R.id.expense_cost);
            expenseDate = view.findViewById(R.id.expense_date);
            expenseType = view.findViewById(R.id.expense_category);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean handled = false;
                    if (adapter.mItemClickListener != null) {
                        handled = adapter.mItemClickListener.onItemClick(itemView, getAdapterPosition());
                    }
//                    if (!handled) {
//                        try {
//                            Expense expense = ((ExpenseItem) adapter.getItem(getAdapterPosition())).getListExpense();
//                            if (activity instanceof MainActivity) {
//                                ((MainActivity) activity).setModifyingExpense(expense);
//                            }
//                            Fragment fragment = new AddExpenseFragment();
//                            if (expense.getCategory() == CategoriesEnum.AutoMileage) {
//                                fragment = new ViewMileageFragment();
//                            }
//                            Bundle bundle = new Bundle();
//                            bundle.putBoolean("saved", true);
//                            String backStackKey = "ViewExpense";
//                            bundle.putString(Constants.BackStackKey, backStackKey);
//                            fragment.setRetainInstance(true);
//                            fragment.setArguments(bundle);
//                            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
//                            transaction.add(R.id.main_content, fragment, "ViewExpense")
//                                    .addToBackStack(backStackKey)
//                                    .commit();
//                        } catch (NullPointerException e) {
//                            Plog.e("ExpenseItem", e, "onclick");
//                        }
//                    }
                }
            });
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.scaleAnimator(animators, itemView, 0f);
//            AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0f);
        }
    }
}
