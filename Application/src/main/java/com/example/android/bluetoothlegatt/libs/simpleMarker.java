package com.example.android.bluetoothlegatt.libs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by New User on 3/23/2015.
 */
public class simpleMarker {
    int wid,hei,gridWid,gridHei;
    public simpleMarker(int w,int h, int gridW, int gridH){
        wid=w;
        hei=h;
        gridWid=gridW;
        gridHei=gridH;

    }
    public static Bitmap makeMarker(int w,int h,int wid,int hei,int[] arr){
        try {
           int Width=w;
           int Height=h;
            //BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth,
            //        BufferedImage.TYPE_INT_RGB);

            //image.createGraphics();

            //Graphics2D graphics = (Graphics2D) image.getGraphics();
            //graphics.setColor(Color.WHITE);
            //graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
            //graphics.setColor(Color.BLACK);
            Bitmap bmp= Bitmap.createBitmap(wid, hei, Bitmap.Config.ARGB_8888);
            Canvas canv=new Canvas(bmp);
            Paint p=new Paint(0xffffffff);
            p.setColor(0xff000000);
            p.setStrokeWidth(0);
            canv.drawARGB(255, 255, 255, 255);
            int stepx=wid/Width;
            int stepy=hei/Height;

            for (int i = 0; i < Height; i++) {
                int idx=i*Width;
                for (int j = 0; j < Width; j++) {
                    if (arr[idx+j]!=0) {
                        //graphics.fillRect(i, j, 1, 1);
                        canv.drawRect(new Rect(stepx*j,stepy*i, stepx*(j+1), stepy*(i+1)), p);
                    }
                }
            }
            return bmp;
            //ImageIO.write(image, fileType, myFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
