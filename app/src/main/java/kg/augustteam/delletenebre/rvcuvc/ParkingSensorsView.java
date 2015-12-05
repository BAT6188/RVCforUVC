package kg.augustteam.delletenebre.rvcuvc;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class ParkingSensorsView extends View {
    private final String TAG = getClass().getName();
    private APP mAPP;

    private SharedPreferences _settings;

    private Paint mPaint;
    private RectF mRectF;
    private Path frontBackgroundPath, rearBackgroundPath;


    private int sensorsSize = 20;
    private int strokeWidth = 3;

    private int minDistance = 0;
    private int maxDistance = 255;

    private int carWidth = 0, carHeight = 0;
    private int sweepAngle, radius;
    private int rearSensorsCount = 0, frontSensorsCount = 0;

    private int emptyColor;
    private String units;
    private SpannableString unitsSpan;

    private String emptyDistanseText;
    private int[] rearSensorsData, frontSensorsData;
    private String rearInputString = "-1,-1,-1,-1";
    private String frontInputString = "120,220";

    public ParkingSensorsView(Context context) {
        super(context);
        init();
    }
    public ParkingSensorsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParkingSensorsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mAPP = APP.getInstance();
        mAPP.setParkingSensorsView(this);
        _settings = PreferenceManager.getDefaultSharedPreferences(getContext());

        emptyColor = Color.parseColor("#80ffffff");

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setColor(emptyColor);

        mRectF = new RectF();

        rearSensorsData = stringToIntArray(rearInputString);
        frontSensorsData = stringToIntArray(frontInputString);

        setUnits("см");

        sweepAngle = 120;
        radius = 100;

        emptyDistanseText = getContext().getString(R.string.parking_sensor_none_value);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAPP.setParkingSensorsView(null);
    }

    public void setMinMaxDistances(int min, int max) {
        minDistance = min;
        maxDistance = max;

        invalidate();
    }

    public String getUnits() {
        return units;
    }
    public void setUnits(String units) {
        this.units = units;
        unitsSpan = new SpannableString(this.units);
        unitsSpan.setSpan(new RelativeSizeSpan(0.5f), 0, unitsSpan.length(), 0);
    }

    public void setFrontSensorsCount(int count) {
        frontSensorsCount = count;
    }
    public void setRearSensorsCount(int count) {
        rearSensorsCount = count;
    }

    public void setSensorsData(String type, String data) {
        if ( type.equals("rear") ) {
            rearSensorsData = stringToIntArray(data);
            ArrayUtils.reverse(rearSensorsData);
        } else if ( type.equals("front") ) {
            rearSensorsData = stringToIntArray(data);
        }

        invalidate();
    }

    public void setCarHeight(int height) {
        carHeight = height;
        invalidate();
    }

    private int[] stringToIntArray(String data) {
        String[] strArray = data.split(",");
        int[] intArray = new int[strArray.length];
        for(int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }

        return intArray;
    }

    int getColorByPercent(double value){
        return android.graphics.Color.HSVToColor(new float[]{(float)value*120f,1f,1f});
    }


    private void drawSensors(Canvas canvas, String side, int countSensors, int[] sensorsData) {
        final TextViewCircle ringView = mAPP.getRingView(side);

        if ( countSensors > 0 && countSensors == sensorsData.length) {
            Path path;
            final RectF oval = new RectF();

            int heightOffset = carHeight / 3,
                    centerX = getWidth() / 2,
                    centerY = getHeight() / 2,
                    startAngle = 30,
                    sweepSegment = sweepAngle / countSensors;

            if (side.equals("front")) {
                centerY -= heightOffset;
                startAngle = 210;
            } else {
                centerY += heightOffset;
            }


            int[] tempSensorsData = sensorsData;
            while (ArrayUtils.contains(tempSensorsData, -1)) {
                tempSensorsData = ArrayUtils.removeElement(tempSensorsData, -1);
            }
            int min = -1;
            if (tempSensorsData.length > 0) {
                min = Collections.min(Arrays.asList(ArrayUtils.toObject(tempSensorsData)));
                if (min > maxDistance) {
                    min = maxDistance;
                }
            }

            int dataColor = (min < 0)
                    ? emptyColor
                    : getColorByPercent((min * 1f / maxDistance));

            SpannableString textDistanceSpan = new SpannableString((min > -1) ? String.valueOf(min) : emptyDistanseText);
            textDistanceSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, textDistanceSpan.length(), 0);

            if (ringView != null) {
                ringView.setRingColorAnimated(dataColor);
                ringView.setText(TextUtils.concat(textDistanceSpan, "\n", unitsSpan));
                //            String currentString = String.valueOf(ringView.getText());
                //            String[] separated = currentString.split("\n");
                //

                //            if (separated[0].equals(emptyDistanseText) || min < 0) {
                //                ringView.setText(TextUtils.concat(textDistanceSpan, "\n", unitsSpan));
                //            } else {
                //
                //                ValueAnimator animator = new ValueAnimator();
                //                animator.setObjectValues(Integer.parseInt(separated[0]), min);
                //                animator.setDuration(200);
                //                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                //                    public void onAnimationUpdate(ValueAnimator animation) {
                //                        String textDistance = String.valueOf(animation.getAnimatedValue());
                //                        SpannableString textDistanceSpan = new SpannableString(textDistance);
                //                        textDistanceSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, textDistance.length(), 0);
                //                        ringView.setText(TextUtils.concat(textDistanceSpan, "\n", unitsSpan));
                //                    }
                //                });
                //                animator.start();
                //            }
            }

            mPaint.setColor(dataColor);
            for (int j = 0; j < countSensors; j++) {
                path = new Path();

                int sensorVal = sensorsData[j];
                if (sensorVal > maxDistance) {
                    sensorVal = maxDistance;
                }

                int dataValuePercent = (sensorVal - minDistance) * 100 /
                        (maxDistance - minDistance);

                for (int i = 0; i < sensorsSize; i++) {
                    int currentPercent = i * 100 / sensorsSize;

                    if (dataValuePercent < currentPercent || sensorVal < 0) {
                        break;
                    }

                    int offset = strokeWidth * 2 * i;
                    oval.set(centerX - radius - offset,
                            centerY - radius - offset,
                            centerX + radius + offset,
                            centerY + radius + offset);

                    path.addArc(oval, startAngle + (j * sweepSegment) + 2, sweepSegment - 2);
                }

                canvas.drawPath(path, mPaint);
            }
        } else if (ringView != null) {
            SpannableString textDistanceSpan = new SpannableString(emptyDistanseText);
            textDistanceSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, textDistanceSpan.length(), 0);
            ringView.setRingColorAnimated(emptyColor);
            ringView.setText(TextUtils.concat(textDistanceSpan, "\n", unitsSpan));
        }
    }

    private Path drawBackground(int countSensors, String side) {
        Path path = new Path();

        if ( countSensors > 0) {
            RectF oval = new RectF();

            int heightOffset = carHeight / 3,
                    centerX = getWidth() / 2,
                    centerY = getHeight() / 2,
                    startAngle = 30,
                    sweepSegment = sweepAngle / countSensors;

            if (side.equals("front")) {
                centerY -= heightOffset;
                startAngle = 210;
            } else {
                centerY += heightOffset;
            }

            for (int i = 0; i < sensorsSize; i++) {
                int offset = strokeWidth * 2 * i;
                oval.set(centerX - radius - offset,
                        centerY - radius - offset,
                        centerX + radius + offset,
                        centerY + radius + offset);

                for (int j = 0; j < countSensors; j++) {
                    path.addArc(oval, startAngle + (j * sweepSegment) + 2, sweepSegment - 2);
                }
            }
        }

        return path;
    }

    protected void onSizeChanged(int w, int h, int wOld, int hOld) {
        super.onSizeChanged(w, h, wOld, hOld);

        if ( _settings.getBoolean("ps_enable", false) ) {
            rearBackgroundPath = drawBackground(rearSensorsCount, "rear");
            frontBackgroundPath = drawBackground(frontSensorsCount, "front");
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ( _settings.getBoolean("ps_enable", false) ) {
            mPaint.setColor(emptyColor);
            canvas.drawPath(rearBackgroundPath, mPaint);
            canvas.drawPath(frontBackgroundPath, mPaint);
            drawSensors(canvas, "rear", rearSensorsCount, rearSensorsData);
            drawSensors(canvas, "front", frontSensorsCount, frontSensorsData);
        }
    }
}