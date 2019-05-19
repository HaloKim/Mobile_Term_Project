package com.example.term_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import org.opencv.core.Rect;

public class eex extends View{
    Bitmap bm;
    public eex(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    protected void onDraw(Canvas canvas) {
        //비트맵 이미지 객체 이용
        try{
            String imgpath = ((openCV)openCV.context).ipath;
            bm = BitmapFactory.decodeFile(imgpath);
        }catch(Exception e){bm = BitmapFactory.decodeResource(getResources(), R.drawable.ex); }
        canvas.drawBitmap(bm, 0, 0, null);
        super.onDraw(canvas);
    }
}