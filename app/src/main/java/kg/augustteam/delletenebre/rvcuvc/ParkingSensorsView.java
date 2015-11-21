package kg.augustteam.delletenebre.rvcuvc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by delle on 19.11.15.
 */
public class ParkingSensorsView extends View {

    private SharedPreferences _settings;

    private Paint mPaint;
    private RectF mRectF;


    private int sensorsSize = 20;
    private int strokeWidth = 3;

    private int minDistance = 30;
    private int maxDistance = 255;

    private int carWidth = 0, carHeight = 0;

    private int emptyColor;

    private int[] rearSensorsData, frontSensorsData;
    private String rearInputString = "30,250,70,120";
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
        _settings = PreferenceManager.getDefaultSharedPreferences(getContext());

        emptyColor = Color.parseColor("#80ffffff");

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setColor(emptyColor);

        mRectF = new RectF();

        rearSensorsData = stringToIntArray(rearInputString);
        frontSensorsData = stringToIntArray(frontInputString);
    }

    public void setCarSize(int width, int height) {
        carWidth = width;
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

    private void drawSensors(Canvas canvas, int centerX, int centerY, int radius,
                             String side, int countSensors, int[] sensorsData) {
        Path path;
        final RectF oval = new RectF();

        int startAngle = side.equals("front") ? 210 : 30,
                sweepAngle = 120,
                sweepSegment = sweepAngle / countSensors;


        List listSensorsData = Arrays.asList(ArrayUtils.toObject(sensorsData));
        int min = (int)Collections.min(listSensorsData);

        int dataColor = getColorByPercent((min * 1f / maxDistance));

        for(int i = 0; i < sensorsSize; i++) {
            int currentPercent = i * 100 / sensorsSize;

            int offset = strokeWidth * 2 * i;
            oval.set(centerX - radius - offset,
                    centerY - radius - offset,
                    centerX + radius + offset,
                    centerY + radius + offset);

            for (int j = 0; j < countSensors; j++) {
                path = new Path();
                if ( countSensors == sensorsData.length ) {
                    int dataValuePercent = (sensorsData[j] - minDistance) * 100 /
                            (maxDistance - minDistance);

                    mPaint.setColor(
                            ( dataValuePercent >= currentPercent )
                                    ? dataColor
                                    : emptyColor);
                }

                path.addArc(oval, startAngle + (j * sweepSegment) + 2, sweepSegment - 2);
                canvas.drawPath(path, mPaint);
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if ( _settings.getBoolean("ps_enable", false) ) {

            int w = getWidth(),
                    h = getHeight(),
                    centerX = w / 2,
                    centerY = h / 2,
                    radius = 100;

            drawSensors(canvas, centerX, centerY + carHeight / 3, radius, "rear", 4, rearSensorsData);
            drawSensors(canvas, centerX, centerY - carHeight / 3, radius, "front", 2, frontSensorsData);
        }
    }
}