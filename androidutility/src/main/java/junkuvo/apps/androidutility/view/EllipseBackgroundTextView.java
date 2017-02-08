package junkuvo.apps.androidutility.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import junkuvo.apps.androidutility.R;


/**
 *
 */
public class EllipseBackgroundTextView extends TextView {
    private String mTextString = "";
    private int mTextColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
    private float mTextSize = 100;
    private Drawable mBackgroundDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private int paddingLeft = getPaddingLeft();
    private int paddingTop = getPaddingTop();
    private int paddingRight = getPaddingRight();
    private int paddingBottom = getPaddingBottom();


    public EllipseBackgroundTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public EllipseBackgroundTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EllipseBackgroundTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setWillNotDraw(false);
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EllipseBackgroundTextView, defStyle, 0);

        mTextColor = a.getColor(R.styleable.EllipseBackgroundTextView_textColor, mTextColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mTextSize = a.getDimension(R.styleable.EllipseBackgroundTextView_textSize, mTextSize);

        if (a.hasValue(R.styleable.EllipseBackgroundTextView_exampleDrawable)) {
            mBackgroundDrawable = a.getDrawable(R.styleable.EllipseBackgroundTextView_exampleDrawable);
            mBackgroundDrawable.setCallback(this);
        }

        a.recycle();

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextWidth = mTextPaint.measureText(getTextString().toString());

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 文字列の幅に合わせて横幅を変化させたいので下記のように計算する
        int contentWidth = (int) mTextWidth + paddingLeft + paddingRight + 100;// + 円の半径//canvas.getWidth() - paddingLeft - paddingRight;
        int contentHeight = canvas.getHeight() - paddingTop - paddingBottom;
        int diffHorizontial = (canvas.getWidth() - contentWidth) / 2;

        int xPos = contentWidth / 2 + diffHorizontial;//(canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));

        // Draw the text.
        canvas.drawText(mTextString, xPos, yPos, mTextPaint);

        // Draw the example drawable on top of the text.
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(paddingLeft + diffHorizontial, paddingTop, paddingLeft + diffHorizontial + contentWidth, paddingTop + contentHeight);
            mBackgroundDrawable.draw(canvas);
        }
    }

    public String getTextString() {
        return getText().toString();
    }

    // TODO : そもそもあるメソッドをオーバーライドするべし
    public void setTextString(String textString) {
        setText(textString);
        invalidateTextPaintAndMeasurements();
    }

    public void setmBackgroundDrawable(Drawable mBackgroundDrawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(mBackgroundDrawable);
        }else{
            setmBackgroundDrawable(mBackgroundDrawable);
        }
        invalidateTextPaintAndMeasurements();
    }
}
