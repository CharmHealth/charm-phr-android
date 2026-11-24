package com.charm.phr.mphr.ui.components;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.ListPopupWindow;
import androidx.transition.TransitionManager;

import com.charm.phr.mphr.R;
import com.charm.phr.mphr.util.ModelUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MDClickToSelectEdittext extends TextInputEditText {

    public ListPopupWindow listPopupWindow;

    List<String> sourceList = new ArrayList<>();
    CharSequence hintToDisplay;

    OnItemSelectListener listener = null;

    private boolean allowAutoSelect = false;

    private boolean showIcon = false;
    private boolean showDividerVertical = true;

    private boolean isGravityCenter = true;

    private boolean showEmptyField = false;

    private int edittextPadding = getResources().getDimensionPixelSize(R.dimen.edittext_padding);
    private int edittextTopPadding = getResources().getDimensionPixelSize(R.dimen.edittext_top_padding);

    private int edittextDrawablePadding = getResources().getDimensionPixelSize(R.dimen.ctc_edittext_drawable_padding);

    private Paint paint = new Paint();

    private AttributeSet attrs;

    private float canvasWidth = 0f;
    private float canvasHeight = 0f;


    public MDClickToSelectEdittext(Context context) {
        super(context);
        init();

    }

    public MDClickToSelectEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        init();
    }

    public MDClickToSelectEdittext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.attrs = attrs;
        init();
    }


    public void init() {
        hintToDisplay = getHint();
        listPopupWindow = new ListPopupWindow(getContext());

        listPopupWindow.setVerticalOffset(20);

        //   listPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getContext() , R.drawable.item_dropdown_bg));
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MDClickToSelectEdittext);

        allowAutoSelect = typedArray.getBoolean(R.styleable.MDClickToSelectEdittext_allowAutoSelect, allowAutoSelect);
        showIcon = typedArray.getBoolean(R.styleable.MDClickToSelectEdittext_showIcon, showIcon);
        showDividerVertical = typedArray.getBoolean(R.styleable.MDClickToSelectEdittext_showSelectDivider, showDividerVertical);
        showEmptyField = typedArray.getBoolean(R.styleable.MDClickToSelectEdittext_showEmptyField, showEmptyField);
        typedArray.recycle();
        paint.setColor(Color.parseColor("#33000000"));
        paint.setStrokeWidth(2f);


        this.setEllipsize(TextUtils.TruncateAt.END);
        this.setSingleLine(true);

        setOnFocusChangeListener((view, hasFocus) -> {

            ViewParent viewParent = ((ViewGroup) getParent()).getParent();
            if (viewParent instanceof TextInputLayout) {
                TextInputLayout textInputLayout = (TextInputLayout) viewParent;
                if (hasFocus) {
                    if (isGravityCenter) {
                        TransitionManager.beginDelayedTransition((ViewGroup) getParent());
                        setGravity(Gravity.BOTTOM);
                        textInputLayout.setPadding(edittextPadding, edittextTopPadding, edittextPadding, edittextPadding);
                        isGravityCenter = false;
                    }
                } else {
                    if (!isGravityCenter && getText().toString().trim().equals("")) {
                        TransitionManager.beginDelayedTransition((ViewGroup) getParent());
                        setGravity(Gravity.CENTER_VERTICAL);
                        textInputLayout.setPadding(edittextPadding, 0, edittextPadding, edittextPadding);
                        isGravityCenter = true;
                    }
                }
            }
        });

        Field f = null;
        try {
            f = TextView.class.getDeclaredField(ModelUtil.mdClickToSelectEdittextCursorDr);
            f.setAccessible(true);
            f.set(this, android.R.color.transparent);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }


        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                ViewParent viewParent = ((ViewGroup) getParent()).getParent();
                if (viewParent instanceof TextInputLayout) {
                    TextInputLayout textInputLayout = (TextInputLayout) viewParent;

                    if (textInputLayout.getError() != null && !textInputLayout.getError().toString().trim().isEmpty()) {
                        textInputLayout.setError(null);
                    }

                    if (!editable.toString().trim().equals("")) {


                        if (isGravityCenter) {
                            TransitionManager.beginDelayedTransition((ViewGroup) getParent());
                            setGravity(Gravity.BOTTOM);
                            textInputLayout.setPadding(edittextPadding, edittextTopPadding, edittextPadding, edittextPadding);
                            isGravityCenter = false;
                        }
                    } else {
                        if (!isFocused()) {
                            if (getGravity() != Gravity.CENTER_VERTICAL && editable.toString().trim().isEmpty()) {
                                TransitionManager.beginDelayedTransition((ViewGroup) getParent());
                                setGravity(Gravity.CENTER_VERTICAL);
                                textInputLayout.setPadding(
                                        edittextPadding,
                                        0,
                                        edittextPadding,
                                        edittextPadding
                                );
                                isGravityCenter = true;
                            }
                        }
                    }
                }


            }
        });

        setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });

        updateIcon();

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setFocusable(false);
        setClickable(true);


        if (showDividerVertical && showIcon && !this.sourceList.isEmpty()) {
            canvas.drawLine(canvasWidth - 120f, 30f, canvasWidth - 120f, canvasHeight - 30f, paint);
        }
    }

    public void setPosition(int posn) {
        try {
            setText(sourceList.get(posn));
        } catch (ArrayIndexOutOfBoundsException e) {
            //
        }

    }

    private void updateIcon() {


        if (showIcon && !this.sourceList.isEmpty()) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_drop_down_black, 0);
            setCompoundDrawablePadding(edittextDrawablePadding);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        invalidate();
    }

    public int getPosition() {

        for (int i = 0; i < sourceList.size(); i++) {
            String temp = sourceList.get(i);
            if (temp.trim().equals(getText().toString().trim())) {
                return i;
            }

        }

        return -1;
    }


    /**
     * this method is used to set  a List of strings to the spinner
     */
    public void setItems(List<String> items, Activity activity) {

        this.sourceList = items;


        ArrayList<String> values = getValidList();


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(activity,
                R.layout.item_custom_list_pop_menu, values);

        listPopupWindow.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        listPopupWindow.setAnchorView(this);
        listPopupWindow.setModal(true);
        listPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);



/*
        listPopupWindow.setDropDownGravity(Gravity.CENTER_HORIZONTAL);
*/


        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                ArrayList<String> values = getValidList();

                String sourceValue = values.get(position);

                if (showEmptyField && position == 0) {
                    sourceValue = "";
                }

                if (!sourceValue.equals(getText().toString())) {
                    setText(sourceValue);
                    if (listener != null) {

                        if (showEmptyField) {


                            listener.onItemSelect(position - 1, sourceValue);
                        } else {
                            listener.onItemSelect(position, sourceValue);
                        }
                    }
                }
                listPopupWindow.dismiss();
            }
        });

        if (allowAutoSelect) {

            if (showEmptyField) {
                setText("");
                if (listener != null) {
                    listener.onItemSelect(-1, "");
                }
            } else {
                setText(sourceList.get(0));
                if (listener != null) {
                    listener.onItemSelect(0, sourceList.get(0));
                }
            }
        }
        configureOnClickListener();

        updateIcon();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasWidth = w;
        canvasHeight = h;
    }


    public List<String> getItems() {
        return sourceList;
    }

    private void configureOnClickListener() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listPopupWindow.show();

                listPopupWindow.getListView().setBackgroundResource(R.drawable.item_dropdown_bg);
            }
        });
    }


    public void setAnchorView(View view) {
        listPopupWindow.setAnchorView(view);
        listPopupWindow.setVerticalOffset(20);

    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    @Override
    public int getSelectionStart() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getMethodName().equals("canPaste")) {
                return -1;
            }
        }
        return super.getSelectionStart();
    }


    private ArrayList<String> getValidList() {
        ArrayList<String> values = new ArrayList<String>();

        if (showEmptyField) {
            values.add(getContext().getString(R.string.clicktoselect_empty_field));
        }

        values.addAll(sourceList);

        return values;
    }


    public void setOnItemClickListener(OnItemSelectListener itemSelectListener) {
        this.listener = itemSelectListener;
    }

    public boolean isShowDividerVertical() {
        return showDividerVertical;
    }

    public void setShowDividerVertical(boolean showDividerVertical) {
        this.showDividerVertical = showDividerVertical;
    }


    public interface OnItemSelectListener {
        public void onItemSelect(int position, String str);
    }

}