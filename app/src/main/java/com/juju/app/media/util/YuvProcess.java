package com.juju.app.media.util;

/**
 * Created by Administrator on 2016/3/14 0014.
 */
public class YuvProcess {

    public static byte[] rotate90YUV420SP(byte[] src,int width,int height)
    {
        byte[] des = new byte[width*height*3/2];
        int wh = width * height;
        //旋转Y
        int k = 0;
        for(int i=0;i<width;i++) {
            for(int j=height-1;j>=0;j--)
            {
                des[k] = src[width*j + i];
                k++;
            }
        }
        // System.arraycopy(src, width*height, des, width*height, src.length-width*height);

        for(int i=0;i<width;i+=2) {
            for(int j=height/2-1;j>=0;j--)
            {
                des[k] = src[wh+ width*j + i];
                des[k+1]=src[wh + width*j + i+1];
                k+=2;
            }
        }


        return des;
    }

    public static byte[] rotate270YUV420SP(byte[] src,int width,int height)
    {
        byte[] des = new byte[width*height*3/2];
        int wh = width * height;
        //旋转Y
        int k = 0;
        for(int i=width-1;i>=0;i--) {
            for(int j=0;j<height;j++)
            {
                des[k] = src[width*j + i];
                k++;
            }
        }
        // System.arraycopy(src, width*height, des, width*height, src.length-width*height);
        for(int i=width-1;i>=0;i-=2) {
            for(int j=0;j<height/2;j++)
            {
                des[k] = src[wh+ width*j + i-1];
                des[k+1]=src[wh + width*j + i];
                k+=2;
            }
        }


        return des;
    }

    public static byte[] rotate90YUV420(byte[] src,int width,int height)
    {
        byte[] des = new byte[width*height*3/2];
        int wh = width * height;
        //旋转Y
        int k = 0;
        for(int i=0;i<width;i++) {
            for(int j=height-1;j>=0;j--)
            {
                des[k] = src[width*j + i];
                k++;
            }
        }

        for(int i=0;i<width/2;i++){
            for(int j=height/2-1;j>=0;j--){
                des[k] = src[wh + width/2*j + i];
                k++;
            }
        }

        for(int i=0;i<width/2;i++){
            for(int j=height/2-1;j>=0;j--){
                des[k] = src[wh*5/4 + width/2*j + i];
                k++;
            }
        }


        return des;
    }

    public static byte[] rotate270YUV420(byte[] src,int width,int height)
    {
        byte[] des = new byte[width*height*3/2];
        int wh = width * height;
        //旋转Y
        int k = 0;
        for(int i=width-1;i>=0;i--) {
            for(int j=0;j<height;j++)
            {
                des[k] = src[width*j + i];
                k++;
            }
        }

        for(int i=width/2-1;i>=0;i--){
            for(int j=0;j<height/2;j++){
                des[k] = src[wh + width/2*j + i];
                k++;
            }
        }

        for(int i=width/2-1;i>=0;i--){
            for(int j=0;j<height/2;j++){
                des[k] = src[wh*5/4 + width/2*j + i];
                k++;
            }
        }


        return des;
    }


    public static byte[] rotate90YUV420ToI420(byte[] src,int width,int height)
    {
        byte[] des = new byte[width*height*3/2];
        int wh = width * height;
        //旋转Y
        int k = 0;
        for(int i=0;i<width;i++) {
            for(int j=height-1;j>=0;j--)
            {
                des[k] = src[width*j + i];
                k++;
            }
        }

        for(int i=0;i<width/2;i++){
            for(int j=height/2-1;j>=0;j--){
                des[k] = src[wh*5/4 + width/2*j + i];
                des[k+1] = src[wh + width/2*j + i];
                k=k+2;
            }
        }
        return des;
    }

    public static byte[] rotate270YUV420ToI420(byte[] src,int width,int height)
    {
        byte[] des = new byte[width*height*3/2];
        int wh = width * height;
        //旋转Y
        int k = 0;
        for(int i=width-1;i>=0;i--) {
            for(int j=0;j<height;j++)
            {
                des[k] = src[width*j + i];
                k++;
            }
        }

        for(int i=width/2-1;i>=0;i--){
            for(int j=0;j<height/2;j++){
                des[k] = src[wh*5/4 + width/2*j + i];
                des[k+1] = src[wh + width/2*j + i];
                k=k+2;
            }
        }
        return des;
    }




}
