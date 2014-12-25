package com.example.nobu.cloudapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.sql.Time;
import java.util.Date;
import java.util.Vector;
import java.io.File;
import java.net.*;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {
    // ログ用のタグ
    private static final String TAG = "cloudApp";
    // カメラビューのインスタンス
    private CameraBridgeViewBase mCameraView;
    /* OpenCv画像を収納する行列 */
    private Mat mOutputFrame;
    /* Time */
    private long previous_time, current_time;
    /* Netwotk */
    private static final String SERVER_ADDR = "157.82.5.54";
    private static final int PORT = 9876;
    // ライブラリ初期化完了後に呼ばれるコールバック (onManagerConnected)
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                // 読み込みが成功したらカメラプレビューを開始
                case LoaderCallbackInterface.SUCCESS:
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        // カメラビューのインスタンスを変数にバインド
        mCameraView = (CameraBridgeViewBase)findViewById(R.id.camera_view);
        // リスナーの設定 (後述)
        mCameraView.setCvCameraViewListener(this);


    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        // 非同期でライブラリの読み込み/初期化を行う
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback); // 各自のバージョンに合わせる
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mCameraView!=null){
            mCameraView.disableView();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mCameraView!=null){
            mCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height){
        mOutputFrame = new Mat(height, width, CvType.CV_8UC1);
        // 時間の初期化
        Date Time = new Date();
        previous_time = Time.getTime();
    }

    @Override
    public void onCameraViewStopped(){
        if(mOutputFrame!=null)
            mOutputFrame.release();
        mOutputFrame = null;
    }

    @Override
    public Mat onCameraFrame(Mat inputFrame){
        // 0.3sに一度画像を保存する
        Date Time = new Date();
        current_time = Time.getTime();
    if(current_time - previous_time > 3000) {
            Log.d(TAG, String.valueOf(current_time));
            previous_time = current_time;
            sendImage(inputFrame);
            //SaveImage(inputFrame);
        }
        // ここでビューから取り込んだ画像を取得できる
        return inputFrame;
    }

  /* public void SaveImage (Mat mat) {
        Mat mIntermediateMat = new Mat();

        Imgproc.cvtColor(mat, mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);

        File path = new File(Environment.getExternalStorageDirectory() + "/Images/");
        path.mkdirs();
        String filename = String.valueOf(baseTime) + ".jpeg";
        File file = new File(path, filename);

        filename = file.toString();
        Boolean bool = Highgui.imwrite(filename, mIntermediateMat);

        if (bool == true)
            Log.d(TAG, "SUCCESS writing image to external storage");
        else
            Log.d(TAG, "Fail writing image to external storage");
    }*/

    public void sendImage(Mat mat){
        try{
            /* 画像の圧縮 */
            int height = mat.height();
            int width = mat.width();
            MatOfByte buf = new MatOfByte();
            MatOfInt params = new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, 100);
            Highgui.imencode(".jpeg", mat, buf, params);

            /* 画像の送信 */
            byte[] data = buf.toArray();
            String decode = new String(data);
            StringBuffer info = new StringBuffer(String.valueOf(decode.length()));
            info.append("-");
            info.append(String.valueOf(height));
            info.append("-");
            info.append(String.valueOf(width));
            for(int i=info.length(); i<16; i++){info.append(" ");}
            Log.d(TAG, String.valueOf(info));
            info.append(decode);
            Socket socket = new Socket(SERVER_ADDR, PORT);
            PrintStream out = new PrintStream(socket.getOutputStream());
            //BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            out.print(info);
            out.flush();
            //out.write(sendArray);
            if(out != null) out.close();
            if(socket != null) socket.close();
        } catch (Exception e){
            System.out.println("Exception: " + e);
        }
    }

}
