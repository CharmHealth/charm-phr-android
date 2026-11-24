package com.charm.phr.mphr.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.QwertyKeyListener;
import android.text.style.ImageSpan;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;

import com.charm.phr.mphr.R;
import com.charm.phr.mphr.util.ModelUtil;
import com.charm.phr.mphr.util.UIUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;




        

        /*import com.zoho.vtouch.android.R;
        import com.zoho.vtouch.components.BaseCursorAdapter;
        import com.zoho.vtouch.components.BaseCursorAdapter.UpdateObserver;
        import com.zoho.vtouch.resources.FontManager;
        import com.zoho.vtouch.resources.FontManager.Font;*/


public /*abstract*/ class MDChipsMultiSelctAutoCompleteEdittext extends AppCompatMultiAutoCompleteTextView implements AdapterView.OnItemClickListener {
    //the following are default measurements in pixels for mdpi
    protected int chipBorder = 1;
    protected int chipPadding = 4;
    protected int chipFontSize = 14;
    protected int chipHeight = 32;
    protected boolean iconToLeft = true;
    protected int chipBackground = 0xFFEFEFEF;// 0xFFDFDFDF;
    protected int chipBorderColor = Color.LTGRAY;
    protected int chipErrorBackground = 0xFFFFD1D1;
    protected int chipErrorBorderColor = Color.RED;
    protected int chipSelIconBackground = 0x22F71951; //33B5E5;
    protected int chipSelBackground = 0xFF33B5E5;
    protected PopupWindow popup = null;
    protected int selTextColor = Color.parseColor("#DB000000"); //No I18N

    static float density = -1;

    private Tokenizer tokenizer = null;
    private CustomSpan selChip = null;
    private ScrollView scrollView = null;
    private boolean gettingScrollView = false;
    private CustomSpan touchedChip;
    private OnTokenChangeListener onTokenChangeListener;
    private AddNewContactListener addNewContactListener;


    public MDChipsMultiSelctAutoCompleteEdittext(Context context) {
        super(context);
        init(context, null);
    }

    public MDChipsMultiSelctAutoCompleteEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MDChipsMultiSelctAutoCompleteEdittext(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    public void init(Context context, AttributeSet attrs) {
        if (density == -1) {
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            density = metrics.density;
        }

        if (attrs != null) {
            TypedArray aDetails = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MDChipsMultiSelctAutoCompleteEdittext, 0, 0);
            try {
                iconToLeft = aDetails.getBoolean(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_iconToLeft, true);
                chipPadding = aDetails.getDimensionPixelSize(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_chipPadding, getPixels(chipPadding));
                chipFontSize = aDetails.getDimensionPixelSize(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_chipFontSize, getPixels(chipFontSize));
                chipHeight = aDetails.getDimensionPixelSize(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_chipHeight, getPixels(chipHeight));
                chipBorder = aDetails.getDimensionPixelSize(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_chipBorder, getPixels(chipBorder));
                chipBackground = aDetails.getColor(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_chipBackground, chipBackground);
                chipErrorBackground = aDetails.getColor(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_chipErrorBackground, chipErrorBackground);
                chipBorderColor = aDetails.getColor(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_chipBorderColor, Color.LTGRAY);
                chipErrorBorderColor = aDetails.getColor(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_chipErrorBorderColor, Color.RED);
                chipSelBackground = aDetails.getColor(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_chipSelBackground, chipSelBackground);
                chipSelIconBackground = aDetails.getColor(R.styleable.MDChipsMultiSelctAutoCompleteEdittext_chipSelIconBackground, chipSelIconBackground);
                selTextColor = getLabelTextColor(chipSelBackground);
                if (density >= 4) {
                    setLineSpacing(getPixels(4), 1);
                } else {
                    setLineSpacing(getPixels(2), 1);
                }
            } finally {
                aDetails.recycle();
            }
        }


        Drawable drawable = getDropDownBackground();

       /* if (drawable instanceof GradientDrawable){
            Log.d("MDChips" , "Yessssssz");
            GradientDrawable  gradientDrawable = ((GradientDrawable) drawable) ;
            float cornerRadius  = context.getResources().getDimensionPixelSize(R.dimen.common_card_radius) ;
            float [] cornerRadii  = {cornerRadius,cornerRadius,cornerRadius,cornerRadius,cornerRadius,cornerRadius,cornerRadius,cornerRadius} ;
            gradientDrawable.setCornerRadii(cornerRadii);
            setDropDownBackgroundDrawable(gradientDrawable);
            setDropDownVerticalOffset(20);
        }*/

        setDropDownVerticalOffset(20);

        setTokenizer(new Rfc822Tokenizer());

        setOnItemClickListener(this);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //prevent on text change listener from updating the chips when any of the chip is selected.
               /* if(selChip != null)
                {
                    if (before - count == 1)
                    {
                        append(" ");
                        setSelection(getText().length());
                    }
                    return;
                }*/
                if (s.toString().trim().length() == 0) {
                    if (onTokenChangeListener != null) {
                        onTokenChangeListener.onTokensClear();
                    }
                } else if (before - count == 1) {
                    deleteChipIfNeeded();
                } else if (count > before) {
                    if (tobeConverted(s)) {
                        replaceWithChip();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        setDropDownBackgroundResource(R.drawable.item_dropdown_bg);

    }

    private void deleteChipIfNeeded() {
        // need to delete the chip if the space before it is deleted.
        int selStart = getSelectionStart();
        CustomSpan[] spans = getCustomSpans(selStart);


        if (spans.length > 0) {
            Editable editable = getText();
            int tokenStart = tokenizer.findTokenStart(editable, selStart);
            int tokenEnd = tokenizer.findTokenEnd(editable, tokenStart);
            tokenEnd = tokenEnd + 1;
            if (tokenEnd > editable.length()) {
                tokenEnd = editable.length();
            }
            Spannable spanText = (Spannable) getText();
            CustomSpan[] cSpans = spanText.getSpans(tokenStart, tokenEnd, CustomSpan.class);
            CustomSpan custSpan = cSpans[0];
            /*if(custSpan.isSelected)
            {*/
            editable.delete(tokenStart, tokenEnd);

            if (onTokenChangeListener != null) {
                onTokenChangeListener.onTokenDelete(custSpan.entry);
            }
            //}
            /*else
            {
                CustomSpan cust = constructChipSpan(custSpan.getEntry(), true);
                cust.setSelected(true);
                editable.removeSpan(custSpan);
                editable.setSpan(cust, tokenStart, tokenEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                QwertyKeyListener.markAsReplaced(editable, tokenStart, tokenEnd, "");
                append(" ");
*//*
                showPopup(cust.getEntry(), 0, 0);
*//*
            }*/
        }
    }

    @Override
    public void setTokenizer(Tokenizer tokenizer) {
        super.setTokenizer(tokenizer);
        this.tokenizer = tokenizer;
    }

    protected int getPixels(int dp) {
        return (int) (dp * density);
    }

    //to check whether span is to be created or not, based on  , or ; delimiters
    protected boolean tobeConverted(CharSequence s) {
        char last;
        int end = getSelectionEnd() == 0 ? 0 : getSelectionEnd() - 1;
        int len = length() - 1;
        if (end != len) {
            last = s.charAt(end);
        } else {
            last = s.charAt(len);
        }
        return last == ',' || last == ';'; //No I18N
    }

    private void replaceWithChip() {
        Editable editable = getText();


        int end = getSelectionEnd();
        int start = tokenizer.findTokenStart(editable, end);
        String text = editable.toString().substring(start, end);
        clearComposingText();
        if (text.equals(",") || text.equals(";")) {
            return;
        }
        if (text != null && text.length() > 0 && start > -1 && end > -1) {
            ChipEntry entry = createEntry(text);

            if (entry != null) {
                QwertyKeyListener.markAsReplaced(editable, start, end, ""); //No I18N
                //CharSequence chipText = createSingleChip(text+" ", entry,false); //No I18N
                CustomSpan chipText = constructChipSpan(entry, false);
                //editable.replace(start, end, chipText);
                editable.setSpan(chipText, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        }

        if (end == getSelectionEnd()) {
            dismissDropDown();
        }
        append(" ");
        setSelection(getText().length());
    }

    //create the spannable for replacing the text
    protected CharSequence createSingleChip(String text, ChipEntry entry, boolean isSelected) {
        SpannableString chipSpan = new SpannableString(text);
        ImageSpan chip = constructChipSpan(entry, isSelected);
        chipSpan.setSpan(chip, 0, text.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return chipSpan;
    }

    protected ChipEntry createEntry(String token) {

        Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(token);
        String temp = tokens[0].getAddress();


        String[] tempList = temp.split(":");


        StringBuilder nameBuilder = new StringBuilder();

        String id = tempList[tempList.length - 1];

        for (int i = 0; i < tempList.length - 1; i++) {
            nameBuilder.append(tempList[i]);
            if (i != tempList.length - 2) {
                nameBuilder.append(":");
            }
        }

        String name = nameBuilder.toString();

        if (name != null && !"".equals(name.toString())) {
            try {
                name = new StringBuilder(URLDecoder.decode(name.toString(), "UTF-8")).toString();
            } catch (UnsupportedEncodingException e) {
            }
        }

        if (name.trim().isEmpty() || id.trim().isEmpty()) {
            return null;
        }
        ChipEntry entry = new ChipEntry(name, id);

        if (onTokenChangeListener != null) {
            onTokenChangeListener.onTokenAdded(entry);
        }
        return entry;
    }

    protected CustomSpan constructChipSpan(ChipEntry entry, Boolean isSelected) {
        TextPaint paint = getPaint();
/*
        paint.setTypeface(Typeface.createFromAsset());todo:to be implemented
*/


        float defaultSize = paint.getTextSize();
        int defaultColor = paint.getColor();

        int height = chipHeight;
        int iconWidth = height;
        int background = chipBackground;
        int borderColor = Color.TRANSPARENT;
        int textColor = Color.parseColor("#DB000000"); //No I18N

        boolean isError = entry.isError();
        if (isError) {
            background = chipErrorBackground;
            borderColor = chipErrorBorderColor;
            iconWidth = 0;
        } else {
            background = chipSelBackground;
            borderColor = chipSelBackground;
            textColor = selTextColor;
        }


        float[] widths = new float[1];
        String name = entry.name;


        Bitmap deleteBitmap = getDeleteBitmap();

        float availWidthForText = calculateAvailableWidth() - iconWidth - widths[0];

        CharSequence ellipsizedText = ellipsizeText(name, paint, availWidthForText);
        int width = Math.max(iconWidth, (int) Math.floor(paint.measureText(ellipsizedText, 0, ellipsizedText.length())) + (chipPadding * 2) + (height) + 10);

        Bitmap tmpBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tmpBitmap);

        //draw the background and border
        paint.setColor(background);

        paint.setStyle(Paint.Style.FILL);

        canvas.drawRoundRect(0, 0, width, height, 40, 40, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(borderColor);
        paint.setStrokeWidth(chipBorder);
        canvas.drawRoundRect(0, 0, width, height, 40, 40, paint);

        int iconStart = 0;
        int textStart = 10;
       /* if(iconToLeft)
        {
            textStart = iconWidth;
        }
        else
        {
            iconStart = width - iconWidth;
        }*/

        //draw the image of contact
        /*
                if(!isError)
        {
            Bitmap photo = null;
            if((photo = getIconBitmap(entry)) != null) //No I18N
            {
                RectF src = new RectF(0, 0, photo.getWidth(), photo.getHeight());
                RectF dst = new RectF(iconStart, 0, iconStart+iconWidth, height);
                Matrix matrix = new Matrix();
                matrix.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
                canvas.drawBitmap(photo, matrix, paint);
            }
            else
            {
                paint.setColor(Color.LTGRAY);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(iconStart, 0, iconStart+iconWidth, height, paint);

                String text = name.substring(0,1).toUpperCase();
                paint.setColor(Color.DKGRAY);
                canvas.drawText(text,iconStart + iconWidth/2 - getTextXOffset(text, paint) , getTextYOffset(text, paint, height), paint);
            }
        }*/

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(textColor);
        paint.setTypeface(UIUtil.Companion.getFont(getContext(), R.font.open_sans_medium));
        canvas.drawText(ellipsizedText, 0, ellipsizedText.length(), textStart + chipPadding, getTextYOffset((String) ellipsizedText, paint, height), paint);


        paint.setColor(chipSelIconBackground);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(width - height - chipBorder, 0, width - chipBorder, height - chipBorder, 40, 40, paint);
        //            canvas.drawBitmap(bitmap, null, dst, paint);


        RectF src = new RectF(0, 0, deleteBitmap.getWidth(), deleteBitmap.getHeight());
        RectF dst = new RectF(width - height - chipBorder - chipPadding + 15, 10, width - chipBorder - chipPadding, height - chipBorder - 5);
        Matrix matrix = new Matrix();
        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawBitmap(deleteBitmap, matrix, paint);


        // Pass the full text to the chip.
        Drawable result = new BitmapDrawable(getResources(), tmpBitmap);
        result.setBounds(0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight());
        CustomSpan recipientChip = new CustomSpan(result, entry);
        recipientChip.setSelected(true);

        paint.setTextSize(defaultSize);
        paint.setColor(defaultColor);
        return recipientChip;
    }

    /*abstract protected*/
    public Bitmap getDeleteBitmap() {
        return drawableToBitmap(R.drawable.ic_cancel_24px);
    }

    ;
    /*abstract protected public Bitmap getIconBitmap(ChipEntry entry){
        return drawableToBitmap(R.drawable.ic_info);
         }
         */


    public Bitmap drawableToBitmap(int drawableRes) {

        Drawable drawable = getResources().getDrawable(drawableRes, null);


        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;


        //  BitmapFactory.decodeResource(getContext().getResources() , drawableRes , options);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private CharSequence ellipsizeText(CharSequence text, TextPaint paint, float maxWidth) {
        paint.setTextSize(chipFontSize);
        return TextUtils.ellipsize(text, paint, maxWidth, TextUtils.TruncateAt.END);
    }

    private float calculateAvailableWidth() {

        return getWidth() - getPaddingLeft() - getPaddingRight() - (chipPadding * 2);
    }

    private float getTextYOffset(String text, TextPaint paint, int height) {
        return (int) (height / 2 - ((paint.descent() + paint.ascent()) / 2));
    }

    private int getTextXOffset(String text, TextPaint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        paint.getTextBounds(text, 0, 1, bounds);
        int textWidth = bounds.right - bounds.left;
        return (int) textWidth / 2;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(view.getTag() != null && view.getTag().equals(ModelUtil.documentShareNoMember))
        {
            clearComposingText();

            Editable editable = getText();
            int end = getSelectionEnd();
            int start = tokenizer.findTokenStart(editable, end);

            String text = editable.toString().substring(start, end);
            QwertyKeyListener.markAsReplaced(editable, start, end, "");
            Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(text);
            String temp = tokens[0].getAddress();


            String[] tempList = temp.split(":");
            editable.replace(start, end, "");
        }
        else if (view.getTag() != null && view.getTag().equals(ModelUtil.documentShareAddContact)) {
            clearComposingText();

            Editable editable = getText();
            int end = getSelectionEnd();
            int start = tokenizer.findTokenStart(editable, end);

            String text = editable.toString().substring(start, end);
            QwertyKeyListener.markAsReplaced(editable, start, end, "");
            Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(text);
            String temp = tokens[0].getAddress();


            String[] tempList = temp.split(":");
            editable.replace(start, end, "");
            if(tempList[1].equals("emailAddress@gmail.com"))
            {
                addNewContactListener.onAddContactClick("","");
            }
            else
            {
                addNewContactListener.onAddContactClick(tempList[0], tempList[1]);
            }
        } else {
            clearComposingText();

            Editable editable = getText();

        /*String temp = editable.toString().trim()

        if (temp.endsWith(",")){
            temp = temp.substring(0 , temp.length() - 1);
        }
        editable =new SpannableStringBuilder(temp);

        setText(temp);
        setSelection(temp.length());
*/
            int end = getSelectionEnd();
            int start = tokenizer.findTokenStart(editable, end);

            String text = editable.toString().substring(start, end);
            QwertyKeyListener.markAsReplaced(editable, start, end, ""); //No I18N

            ChipEntry entry = createEntry(text);
            if (entry != null) {
                CharSequence chipText = createSingleChip(text, entry, false);

                if (chipText != null && start >= 0 && end >= 0) {
                    editable.replace(start, end, chipText);
                }
            }
        }
    }


    public void addChip(String value)
    {
        Editable editable = getText();
        ChipEntry entry = createEntry(value);
        if (entry != null) {
            CharSequence chipText = createSingleChip(value, entry, false);
            editable.append(chipText);
            editable.append(" ");
        }

        setSelection(editable.length());

    }

/*    public ArrayList<String> getSpanKeys()
    {
        ArrayList<String> list = new ArrayList<String>();
        CustomSpan[] cSpans = getCustomSpans(0);
        for(CustomSpan cSpan : cSpans )
        {
            list.add(cSpan.getAddress());
        }
        return list;
    }

    public ArrayList<String> validateAndGetSpanKeys() throws Exception
    {
        handleFocusLost();
        ArrayList<String> list = new ArrayList<String>();
        CustomSpan[] cSpans = getCustomSpans(0);
        for(CustomSpan cSpan : cSpans )
        {
            if(cSpan.getEntry().isError())
            {
                throw new Exception("invalid entry ::: "+cSpan.getAddress()); //No I18N
            }
            list.add(cSpan.getAddress());
        }
        return list;
    }

    public ArrayList<String> validateAndGetSpanKeysWithName() throws Exception
    {
        handleFocusLost();
        ArrayList<String> list = new ArrayList<String>();
        CustomSpan[] cSpans = getCustomSpans(0);
        for(CustomSpan cSpan : cSpans )
        {
            if(cSpan.getEntry().isError())
            {
                throw new Exception("invalid entry ::: "+cSpan.getAddress()); //No I18N
            }
            list.add("\""+cSpan.getName()+"\"<"+cSpan.getAddress()+">");
        }
        return list;
    }*/

    protected CustomSpan[] getCustomSpans(int start) {
        Spannable spanText = (Spannable) getText();
        CustomSpan[] cSpans = spanText.getSpans(start, spanText.length(), CustomSpan.class);
        return cSpans;
    }

    //to force the keyboard to have Done button instead of carriage return
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        int imeActions = outAttrs.imeOptions & EditorInfo.IME_MASK_ACTION;
        if ((imeActions & EditorInfo.IME_ACTION_NEXT) != 0) {
            // clear the existing action
            outAttrs.imeOptions ^= imeActions;
            // set the DONE action
            outAttrs.imeOptions |= EditorInfo.IME_ACTION_NEXT;
        }
        if ((outAttrs.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        }
        return connection;
    }

    @Override
    public boolean performLongClick() {
        if (touchedChip != null) {
            touchedChip = null;
            return true;
        }
        return super.performLongClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isFocused()) {
            return super.onTouchEvent(event);
        }

        float x = event.getX();
        float y = event.getY();
        int offset = putOffsetInRange(x, y);
        CustomSpan currentChip = findChip(offset);
        touchedChip = currentChip;

        boolean handled = super.onTouchEvent(event);
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            setSelection(getText().length());
           /* if (currentChip != null)
            {*/
                /*if (selChip != null && selChip != currentChip)
                {
                    clearSelectedChip();
                    selChip = selectChip(currentChip);
*//*
                    showPopup(selChip.getEntry(),x,y);
*//*
                    setCursorVisible(false);
                }
                else if (selChip == null)
                {
                    setSelection(getText().length());
                    selChip = selectChip(currentChip);
*//*
                    showPopup(selChip.getEntry(),x,y);
*//*
                    setCursorVisible(false);
                }
                else
                {*/
            handleChipClick(currentChip, offset);
            setCursorVisible(true);
            //}
            /*}
            else if (selChip != null )
            {
                clearSelectedChip();
                setCursorVisible(true);
            }*/
            handled = true;
        }
        return handled;
    }

    private void dismissPopup() {
        if (popup != null && popup.isShowing()) {
            popup.dismiss();
        }
    }

    private int putOffsetInRange(final float x, final float y) {
        int offset = supportGetOffsetForPosition(x, y);
        return putOffsetInRange(offset);
    }

    //From JB MR2's TextView -->
    private int supportGetOffsetForPosition(float x, float y) {
        if (getLayout() == null) {
            return -1;
        }
        final int line = supportGetLineAtCoordinate(y);
        final int offset = supportGetOffsetAtCoordinate(line, x);
        return offset;
    }

    private float supportConvertToLocalHorizontalCoordinate(float x) {
        x -= getTotalPaddingLeft();
        // Clamp the position to inside of the view.
        x = Math.max(0.0f, x);
        x = Math.min(getWidth() - getTotalPaddingRight() - 1, x);
        x += getScrollX();
        return x;
    }

    private int supportGetLineAtCoordinate(float y) {
//		y -= getTotalPaddingLeft(); we have added padding for edittext in mail( to,cc,bcc fields). so this line removed for chips issues.
        // Clamp the position to inside of the view.
        y = Math.max(0.0f, y);
        y = Math.min(getHeight() - getTotalPaddingBottom() - 1, y);
        y += getScrollY();
        return getLayout().getLineForVertical((int) y);
    }

    private int supportGetOffsetAtCoordinate(int line, float x) {
        x = supportConvertToLocalHorizontalCoordinate(x);
        return getLayout().getOffsetForHorizontal(line, x);
    }
    //  <-- Ends

    //TODO .. .comment from chips.
    private int putOffsetInRange(int o) {
        int offset = o;
        Editable text = getText();
        int length = text.length();
        // Remove whitespace from end to find "real end"
        int realLength = length;
        for (int i = length - 1; i >= 0; i--) {
            if (text.charAt(i) == ' ') {
                realLength--;
            } else {
                break;
            }
        }

        // If the offset is beyond or at the end of the text,
        // leave it alone.
        if (offset >= realLength) {
            return offset;
        }
        Editable editable = getText();
        while (offset >= 0 && findText(editable, offset) == -1 && findChip(offset) == null) {
            // Keep walking backward!
            offset--;
        }
        return offset;
    }

    private static int findText(Editable text, int offset) {
        if (text.charAt(offset) != ' ') {
            return offset;
        }
        return -1;
    }

    protected void clearSelectedChip() {
        if (selChip != null) {
            unselectChip(selChip);
            selChip = null;
            dismissPopup();
        }
    }

    private void unselectChip(CustomSpan chip) {
        Editable editable = getText();
        int start = editable.getSpanStart(chip);
        int end = editable.getSpanEnd(chip);
        if (start >= 0 && end > 0) {
            String text = editable.toString().substring(start, end);
            CharSequence chipText = createSingleChip(text + " ", chip.getEntry(), false); //No I18N
            editable.delete(start, end + 1);
            editable.insert(start, chipText);
        }
        setSelection(editable.length());
    }

    protected void handleChipClick(CustomSpan chip, int offset) {
        if (chip != null) {
            if (chip.isSelected()) {
                if (isInDelete(chip, offset)) {
                    removeChip(chip);
                } else {
                    clearSelectedChip();
                }
                dismissPopup();
            }
        }
    }

    private boolean isInDelete(CustomSpan chip, int offset) {
        return chip.isSelected() && offset == getText().getSpanEnd(chip);
    }

    private void removeChip(CustomSpan chip) {

        Editable text = getText();
        int spanStart = text.getSpanStart(chip);
        int spanEnd = text.getSpanEnd(chip);
        int toDelete = spanEnd;

        boolean wasSelected = chip == selChip;
        if (wasSelected) {
            selChip = null;
        }
        // remove the trailing space
        while (toDelete >= 0 && toDelete < text.length() && text.charAt(toDelete) == ' ') {
            toDelete++;
        }

        if (spanStart >= 0 && toDelete > 0) {
            text.delete(spanStart, toDelete);

            if (onTokenChangeListener != null) {
                onTokenChangeListener.onTokenDelete(chip.entry);
            }
        }
    }


    private CustomSpan selectChip(CustomSpan chip) {
        Editable editable = getText();
        int start = editable.getSpanStart(chip);
        int end = editable.getSpanEnd(chip);
        editable.removeSpan(chip);

        CustomSpan newChip = constructChipSpan(chip.getEntry(), true);
        QwertyKeyListener.markAsReplaced(editable, start, end, "");
        if (start >= 0 && end > 0) {
            editable.setSpan(newChip, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        newChip.setSelected(true);
        return newChip;
    }

    private void showPopup(ChipEntry entry, float x, float y) {
        View popupView = getPopupView(entry);
        if (popup == null) {
            popup = new PopupWindow(popupView, getWidth() - getPixels(20), ViewGroup.LayoutParams.WRAP_CONTENT, false);
            popup.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            popup.setOutsideTouchable(true);
        } else {
            popup.setContentView(popupView);
        }

        popup.showAsDropDown(this, getPixels(10), 0);
    }


    /*abstract protected*/
    public View getPopupView(ChipEntry entry) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_member_list_for_message, null, false);

        TextView memberTV = (TextView) view.findViewById(R.id.memberListName);
        memberTV.setText(entry.name);

        return view;
    }

    public CustomSpan findChip(int offset) {
        CustomSpan[] chips = getCustomSpans(0);
        for (int i = 0; i < chips.length; i++) {
            CustomSpan chip = chips[i];
            int start = getText().getSpanStart(chip);
            int end = getText().getSpanEnd(chip);
            if (offset >= start && offset <= end) {
                return chip;
            }
        }
        return null;
    }

    protected int getLabelTextColor(int hexcolor) {
        int red = (hexcolor >> 16) & 0xFF;
        int green = (hexcolor >> 8) & 0xFF;
        int blue = hexcolor & 0xFF;
        int yiq = ((red * 299) + (green * 587) + (blue * 114)) / 1000;
        return /*(yiq >= 128) ? Color.BLACK : Color.WHITE*/ selTextColor;
    }


    public static class ChipEntry {
        public String name = null;
        public String id = null;

        //		private static final String EMAIL_PATTERN = "^[A-Z0-9._%+-]+@(?:[A-Z0-9-]+\\.)+[A-Z]{2,6}$"; //No I18N
        private static Pattern mPattern = android.util.Patterns.EMAIL_ADDRESS;

        public ChipEntry(String name, String id) {

            this.name = name;
            this.id = id;
        }

        public boolean isError() {
            /*Matcher matcher = mPattern.matcher(mailID);
            if(!matcher.matches())
            {
                return true;
            }*/
            return false;
        }
    }

    protected class CustomSpan extends ImageSpan {
        private ChipEntry entry = null;
        private boolean isSelected = false;

        public CustomSpan(Drawable d, ChipEntry entry) {
            super(d);
            this.entry = entry;
        }


        public ChipEntry getEntry() {
            return entry;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSel) {
            isSelected = isSel;
        }

        public String getName() {
            return entry.name;
        }
    }

    public void handleFocusLost() {
        Editable ediText = getText();
       /* if(ediText.length() > 0 && ediText.charAt(ediText.length()-1) != ' ') //need to complete the address
        {
            //add the delimiter
            append(",");  //No I18N
        }*/
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (!gainFocus) {
            handleFocusLost();
        }

        Log.d("MDChipsSelectAE", "has focus -> " + gainFocus + " ; filter -> " + getFilter());//No i18N

        if (gainFocus && getFilter() != null) {
            performFiltering(getText(), 0);
            showDropDown();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (scrollView == null && !gettingScrollView) {
            ViewParent parent = getParent();
            while (parent != null && !(parent instanceof ScrollView)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                scrollView = (ScrollView) parent;
                gettingScrollView = true;
            }
        }
    }

    private void scrollBottomIntoView() {
        if (scrollView != null && isFocused()) {
            int[] location = new int[2];
            getLocationOnScreen(location);
            int diff = (int) (100 * (this.getContext().getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
            int height = getHeight();
            int currentPos = location[1] + height;
            int bottom = scrollView.getBottom();
            if (bottom - currentPos < diff) {
                scrollView.scrollBy(0, diff - (bottom - currentPos));
            }
        }

    }

    public void setOnTokenChangeListner(OnTokenChangeListener listener) {
        this.onTokenChangeListener = listener;
    }

    public interface OnTokenChangeListener {
        public void onTokenAdded(ChipEntry chipEntry);

        public void onTokenDelete(ChipEntry chipEntry);

        public void onTokensClear();
    }

    public void setAddNewContactListener(AddNewContactListener listener) {
        this.addNewContactListener = listener;
    }

    public interface AddNewContactListener {
        public void onAddContactClick(String name, String email);
    }
    /*@Override
    public <T extends ListAdapter & Filterable> void setAdapter(T adapter)
    {
        super.setAdapter(adapter);
        ((BaseCursorAdapter)adapter).setUpdateObserver(new UpdateObserver()
        {

            @Override
            public void onChangeEntries(int count)
            {
                if(count>0)
                {
                    scrollBottomIntoView();
                }
            }
        });
    }*/
}
