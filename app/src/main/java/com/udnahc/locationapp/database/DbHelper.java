package com.udnahc.locationapp.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.udnahc.locationapp.model.Expense;
import com.udnahc.locationapp.util.Plog;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private final String TAG = getClass().getSimpleName();
    private static final String DATABASE_NAME = "EzMoney";
    private static final int DATABASE_VERSION = 2;
    private final String USER = "user";
    private final String EXPENSES = "expenses";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Plog.d(TAG, "constructor");
        getWritableDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 2 && newVersion > oldVersion) {
            final StringBuilder builder = new StringBuilder();
            builder.append("ALTER TABLE ").append(EXPENSES).append(" ADD COLUMN ").append(ExpenseColumn.endTime.name()).append(" ").append(ExpenseColumn.endTime.getValue());
            db.execSQL(builder.toString());
            Plog.d("murali", "upgrade done");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder createAlbumTableQuery = new StringBuilder("CREATE TABLE " + USER + " (");
        int last = UserColumn.values().length - 1, index = 0;
        for (UserColumn column : UserColumn.values()) {
            createAlbumTableQuery.append(column.name()).append(" ").append(column.getValue()).append(index == last ? ")" : ", ");
            index++;
        }
        db.execSQL(createAlbumTableQuery.toString());

//        StringBuilder createMileageQuery = new StringBuilder("CREATE TABLE " + MILEAGE + " (");
//        last = UserColumn.values().length - 1;
//        index = 0;
//        for (MileageColumn column : MileageColumn.values()) {
//            createMileageQuery.append(column.name()).append(" ").append(column.getValue()).append(index == last ? ")" : ", ");
//            index++;
//        }
//        db.execSQL(createMileageQuery.toString());

        StringBuilder createExpenseTable = new StringBuilder("CREATE TABLE " + EXPENSES + " (");
        last = ExpenseColumn.values().length - 1;
        index = 0;
        for (ExpenseColumn column : ExpenseColumn.values()) {
            createExpenseTable.append(column.name()).append(" ").append(column.getValue()).append(index == last ? ")" : ", ");
            index++;
        }
        db.execSQL(createExpenseTable.toString());
    }

    public void addMileage(Expense expense) {
        String query = "SELECT * FROM " + EXPENSES + " WHERE " + ExpenseColumn.timeStamp.name() + "=?";
        SQLiteDatabase db = getWritableDatabase();
        try (Cursor cursor = db.rawQuery(query, new String[]{"" + expense.getTimeStamp()})) {
            final ContentValues values = new ContentValues();
            values.put(ExpenseColumn.timeStamp.name(), expense.getTimeStamp());
            values.put(ExpenseColumn.extra1.name(), expense.getMiles());
            values.put(ExpenseColumn.extra2.name(), expense.getPoly());
            values.put(ExpenseColumn.extra3.name(), expense.getLatLongString());
            values.put(ExpenseColumn.endTime.name(), expense.getEndTime());
            if (!cursor.moveToFirst()) {
                db.insert(EXPENSES, null, values);
            } else {
                db.update(EXPENSES, values, ExpenseColumn.timeStamp.name() + "=?", new String[]{"" + expense.getTimeStamp()});
            }
        } catch (Exception e) {
            Plog.e(TAG, e, "addMileage");
        }
    }

    public void deleteMileage(long timeStamp) {
        if (timeStamp == -1)
            return;
        String query = "SELECT * FROM " + EXPENSES + " WHERE " + ExpenseColumn.timeStamp.name() + "=?";
        SQLiteDatabase db = getWritableDatabase();
        try (Cursor cursor = db.rawQuery(query, new String[]{"" + timeStamp})) {
            if (cursor.moveToFirst()) {
                db.delete(EXPENSES, ExpenseColumn.timeStamp.name() + "=?", new String[]{"" + timeStamp});
            }
        } catch (Exception e) {
            Plog.e(TAG, e, "addMileage");
        }
    }

    public List<Expense> getMileages() {
        List<Expense> expenseList = new ArrayList<>();
        String query = "SELECT * FROM " + EXPENSES;
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                do {
                    Expense expense = getExpenseFromCursor(cursor);
                    if (expense != null) {
                        expenseList.add(expense);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Plog.e(TAG, e, "addMileage");
        }
        return expenseList;
    }

    @Nullable
    private Expense getExpenseFromCursor(Cursor cursor) {
        try {
            long timeStamp = Long.parseLong(cursor.getString(ExpenseColumn.timeStamp.ordinal()));
            String endTimeString = cursor.getString(ExpenseColumn.endTime.ordinal());
            if (TextUtils.isEmpty(endTimeString)) {
                endTimeString = "-1";
                Plog.d("murali", "empty end time");
            }
            long endTime = Long.parseLong(endTimeString);
            String extra1 = cursor.getString(ExpenseColumn.extra1.ordinal());
            String extra2 = cursor.getString(ExpenseColumn.extra2.ordinal());
            String extra3 = cursor.getString(ExpenseColumn.extra3.ordinal());
            String uuId = cursor.getString(ExpenseColumn.id.ordinal());
            Expense expense = new Expense(timeStamp);
            expense.setEndTime(endTime);
            if (!TextUtils.isEmpty(extra1))
                expense.setMiles(Double.parseDouble(extra1));
            expense.setPoly(extra2);
            expense.setLatLongString(extra3);
            List<Location> locations = new ArrayList<>();
            String[] split = extra3.split("\\|");
            for (String s : split) {
                String[] latlng = s.split(",");
                if (latlng.length == 2) {
                    Location location = new Location("fused");
                    location.setLatitude(Double.parseDouble(latlng[0]));
                    location.setLongitude(Double.parseDouble(latlng[1]));
                    locations.add(location);
                }
            }
            expense.setPath(locations);
            expense.postProcessMileage(true);
            return expense;
        } catch (Exception e) {
            Plog.e(TAG, e, "getExpenseFromCursor");
        }
        return null;
    }

//    public void addExpense(Expense expense) {
//        String query = "SELECT * FROM " + EXPENSES + " WHERE " + ExpenseColumn.timeStamp + "=?";
//        SQLiteDatabase db = getWritableDatabase();
//        try (Cursor cursor = db.rawQuery(query, new String[]{"" + expense.getTimeStamp()})) {
//            if (!cursor.moveToFirst()) {
//                final ContentValues values = new ContentValues();
//                values.put(ExpenseColumn.id.name(), expense.getId());
//                values.put(ExpenseColumn.timeStamp.name(), expense.getTimeStamp());
//                values.put(ExpenseColumn.merchant.name(), expense.getMerchant());
//                values.put(ExpenseColumn.cost.name(), "" + expense.getCost());
//                values.put(ExpenseColumn.reason.name(), expense.getReason());
//                values.put(ExpenseColumn.category.name(), expense.getCategory().getDisplayName());
//                values.put(ExpenseColumn.extra1.name(), expense.getMiles());
//                values.put(ExpenseColumn.extra2.name(), expense.getPoly());
//                values.put(ExpenseColumn.extra3.name(), expense.getLatLongString());
//                values.put(ExpenseColumn.receipt.name(), expense.getReceiptKey());
//                db.insert(EXPENSES, null, values);
//            }
//        }
//    }
//
//    public List<Expense> getFullExpenses() {
//        String query = "SELECT * FROM " + EXPENSES;
//        SQLiteDatabase db = getReadableDatabase();
//        List<Expense> expenses = new ArrayList<>();
//        try (Cursor cursor = db.rawQuery(query, null)) {
//            if (cursor.moveToFirst()) {
//                do {
//                    final Expense temp = getExpenseFromCursor(cursor);
//                    if (temp != null)
//                        expenses.add(temp);
//                } while (cursor.moveToNext());
//            }
//        }
//        return expenses;
//    }
//
//    @Nullable
//    public Expense getFullExpense(String startTime) {
//        String query = "SELECT * FROM " + EXPENSES + " WHERE " + ExpenseColumn.timeStamp.name() + "=?";
//        SQLiteDatabase db = getReadableDatabase();
//        try (Cursor cursor = db.rawQuery(query, new String[]{startTime})) {
//            if (cursor.moveToFirst()) {
//                return getExpenseFromCursor(cursor);
//            }
//        }
//        return null;
//    }
}
