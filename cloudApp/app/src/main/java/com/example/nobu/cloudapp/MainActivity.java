package com.example.nobu.cloudapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.os.Handler;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.*;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;


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
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.Date;
import java.util.Vector;
import java.io.File;
import java.net.*;
import java.util.Vector;
import java.lang.Double;
import java.nio.ByteBuffer;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener , LocationListener{
    // ログ用のタグ
    private static final String TAG = "cloudApp";
    // カメラビューのインスタンス
    private CameraBridgeViewBase mCameraView;
    /* OpenCv画像を収納する行列 */
    private Mat mOutputFrame;
    /* Time */
    private long previous_time, current_time;
    /* Netwotk */
    private String SERVER_ADDR = "192.168.100.101";
    private int PORT = 9876;
    private int PORT2 = 9875;
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
    // 結果を表示するTextView
    private TextView mText, mText2;
    // TextViewに別スレッドからアクセスするためのハンドラ
    private Handler handler;
    // ソケット
    private ServerSocket serverSocket = null;
    private ServerSocket serverSocket2 = null;
    //
    private LocationManager manager;
    //
    private double lat;
    private double lng;

    private String[] intr = {"赤門", "安田講堂", "工学部2号館", "理学部1号艦", "三四郎池"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);
        // カメラビューのインスタンスを変数にバインド
        mCameraView = (CameraBridgeViewBase)findViewById(R.id.camera_view);
        // リスナーの設定 (後述)
        mCameraView.setCvCameraViewListener(this);
        //
        mText = (TextView)findViewById(R.id.textView);

        /*mText2 = (TextView)findViewById(R.id.textView2);*/

        handler = new Handler();

        manager = (LocationManager)getSystemService(LOCATION_SERVICE);

        setTheme(android.R.style.Theme_Black_NoTitleBar);       //タイトルバー(アクションバー)なし
        setTheme(android.R.style.Theme_WithActionBar);        //アクションバーあり

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // メニューの要素を追加
        menu.add("Set IP_ADDR & PORT");

        // メニューの要素を追加して取得
        MenuItem actionItem = menu.add("Action Button");

        // SHOW_AS_ACTION_IF_ROOM:余裕があれば表示
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // アイコンを設定
        actionItem.setIcon(android.R.drawable.ic_menu_share);

        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //テキスト入力を受け付けるビューを作成します。
        final EditText editView = new EditText(MainActivity.this);
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("IPアドレス入力ダイアログ")
                        //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //入力した文字をトースト出力する
                        Toast.makeText(MainActivity.this,
                                (SERVER_ADDR=editView.getText().toString()),
                                Toast.LENGTH_LONG).show();
                    }

                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
        /*
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        // 非同期でライブラリの読み込み/初期化を行う
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback); // 各自のバージョンに合わせる
        if(manager != null){
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,this);
        }
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mCameraView!=null){
            mCameraView.disableView();
        }
        if(manager != null) {
            manager.removeUpdates(this);
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
        if(current_time - previous_time > 4000) {
            Log.d(TAG, String.valueOf(current_time));
            previous_time = current_time;
            Send_thread sth = new Send_thread(inputFrame);
            sth.start();
            Recieve_thread rth = new Recieve_thread();
            rth.start();
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

    class Recieve_thread extends Thread{

        public void run(){
            try {
                if(serverSocket2==null) serverSocket2 = new ServerSocket(PORT2);
                Socket socket = serverSocket2.accept();
                InputStream is = socket.getInputStream();
                DataInputStream dis = new DataInputStream(is);
                final String in = dis.readLine();
                final int index = Integer.parseInt(in);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mText.setText(intr[index]/*in*/);
                    }
                });
                is.close();
                dis.close();
                socket.close();
            }catch(Exception e) {
                System.out.println("Exception: " + e);
            }
        }

    }

    class Send_thread extends Thread{

        private Mat mat;
        private Socket socket;

        public Send_thread(Mat m) {
            this.mat = m;
            try {
                socket = new Socket(SERVER_ADDR, PORT);
            }catch(Exception e){
                System.out.println("Exception: " + e);
            }
        }

        public void run(){
            checkGPS();
            sendImage(mat);
        }

        private void sendImage(Mat mat){
            try{
            /* 画像の圧縮 */
                int height = mat.height();
                int width = mat.width();
                Mat img = new Mat();
                Imgproc.cvtColor(mat, img, Imgproc.COLOR_RGB2BGR);
                MatOfByte buf = new MatOfByte();
                MatOfInt params = new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, 50);
                Highgui.imencode(".jpg", img, buf, params);

            /* 画像の送信 */
                boolean binary = true; //　バイナリで送りたい場合はtrueにする.
                byte[] data = buf.toArray();
                if(binary){
                    BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                    out.write(ByteBuffer.allocate(8).putDouble(lat).array());
                    out.write(ByteBuffer.allocate(8).putDouble(lng).array());
                    out.write(data);
                    out.flush();
                    out.close();

//                    StringBuffer location = new StringBuffer(String.valueOf(lat));
//                    location.append("-");
//                    location.append(String.valueOf(lng));
//                    PrintStream out2 = new PrintStream(socket.getOutputStream());
//                    out2.print(location);
//                    out2.flush();
//                    out2.close();

                    if (out != null) out.close();
                    if (socket != null) socket.close();
                }else {
                    Mat m = Highgui.imdecode(buf,Highgui.CV_LOAD_IMAGE_COLOR);
                    int imgLength = m.dump().length();
                    String size = m.size().toString();
                    StringBuffer info = new StringBuffer(String.valueOf(imgLength));
                    info.append("-");
                    info.append(String.valueOf(m.height()));
                    info.append("-");
                    info.append(String.valueOf(m.width()));
                    Log.d(TAG, String.valueOf(info));
                    while(info.length() < 24){
                        info.append(" ");
                    }
                    info.append(m.dump());
                    Socket socket = new Socket(SERVER_ADDR, PORT);
                    PrintStream out = new PrintStream(socket.getOutputStream());
                    out.print(info);
                    out.flush();
                    if (out != null) out.close();
                    if (socket != null) socket.close();
                }
            } catch (Exception e){
                System.out.println("Exception: " + e);
            }
        }
    }

    public void checkGPS(){
        if(manager != null){
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
        }
    }

    @Override
    public void onLocationChanged(Location location){
        lat = location.getLatitude();
        lng = location.getLongitude();
        //mText2.setText("Lat: " + lat + "\tLng: " + lng);
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}


}



