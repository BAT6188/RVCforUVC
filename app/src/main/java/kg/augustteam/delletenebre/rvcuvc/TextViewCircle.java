package kg.augustteam.delletenebre.rvcuvc;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewCircle extends TextView {
    private int strokeWidth = 5;
    private Paint mPaint;
    private int ringColor;

    public TextViewCircle(Context context) {
        super(context);
        init();
    }

    public TextViewCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewCircle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);
    }

    public void setRingColor(int color) {
        ringColor = color;

        invalidate();
    }
    public void setRingColorAnimated(int color) {
        ValueAnimator anim = ObjectAnimator.ofInt(this, "ringColor", ringColor, color);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setDuration(500).start();

    }

    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);

        int width = this.getWidth();
        int height = this.getHeight();
        int radius = width > height ? height/2 : width/2;
        radius -= strokeWidth;
        int center_x = width/2;
        int center_y = height/2;


        mPaint.setColor(ringColor);

        canvas.drawCircle(center_x, center_y, radius, mPaint);
    }
}
