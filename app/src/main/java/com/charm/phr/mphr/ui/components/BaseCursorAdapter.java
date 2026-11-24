package com.charm.phr.mphr.ui.components;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import androidx.cursoradapter.widget.CursorAdapter;

import java.util.ArrayList;

public class BaseCursorAdapter extends CursorAdapter
{
    private int entriesCount = 0;
    private ArrayList<UpdateObserver> observers = new ArrayList<UpdateObserver>();

    public BaseCursorAdapter(Context context, Cursor c, boolean autoRequery)
    {
        super(context, c, autoRequery);
    }
    public BaseCursorAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);
    }
    public void setEntriesCount(int count)
    {
        this.entriesCount = count;
        for(UpdateObserver observer : observers)
        {
            observer.onChangeEntries(count);
        }
    }
    public interface UpdateObserver
    {
        public void onChangeEntries(int count);
    }
    public void setUpdateObserver(UpdateObserver observer)
    {
        this.observers.add(observer);
    }
    @Override
    public void bindView(View arg0, Context arg1, Cursor arg2)
    {

    }
    @Override
    public View newView(Context arg0, Cursor arg1, ViewGroup arg2)
    {
        return null;
    }

}

