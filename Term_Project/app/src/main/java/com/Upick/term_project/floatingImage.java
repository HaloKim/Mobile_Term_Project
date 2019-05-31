package com.Upick.term_project;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
public class floatingImage extends View{
    Bitmap bm;
    Bitmap resize_bitmap;
    public floatingImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    protected void onDraw(Canvas canvas) {
        //이미지 최대사이즈 조정을 위한 변수
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        //비트맵 이미지 객체 이용
        try{
            Paint alpha = new Paint(); //페인트 객체 선언
            alpha.setAlpha(80);//페인트의 알파값(투명도)지정 0~255
            String imgpath = ((openCV)openCV.context).ipath;//비트맵 임시저장소 불러오기
            bm = BitmapFactory.decodeFile(imgpath);//비트맵저장
            resize_bitmap = Bitmap.createScaledBitmap(bm, width, height, true);//비트맵 화면사이즈로 재조정
            canvas.drawBitmap(resize_bitmap, 0, 0, alpha);//캔버스에 재조정한 비트맵이미지를 그려준다.
            super.onDraw(canvas);
        }catch(Exception e){ Log.d("dd", "dd");}
    }
}