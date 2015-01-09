/**
 * Created by lisabug on 14/12/25.
 */
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

import java.io.*;
import java.net.*;


public class SimpleSample {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args){
        /*
        System.out.println("Welcome to OPENCV" + Core.VERSION);
        Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
        System.out.println("OpenCV Mat:" + m);
        Mat mr1 = m.row(1);
        mr1.setTo(new Scalar(1));
        Mat mc5 = m.col(5);
        mc5.setTo(new Scalar(5));
        System.out.println("OpenCV Mat data:\n" + m.dump());
        */
        Mat m = Highgui.imread("/Users/lisabug/Desktop/L.jpg");
        int imgLength = m.dump().length();
        //System.out.println(m.dump());
        String size = m.size().toString();
        StringBuffer info = new StringBuffer(String.valueOf(imgLength));
        info.append("-");
        info.append(String.valueOf(m.height()));
        info.append("-");
        info.append(String.valueOf(m.width()));
        System.out.println(info.length());


        while(info.length() < 24){
            info.append(" ");
        }
        info.append(m.dump());

        //System.out.println("width" + m.width() + "height" + m.height() + "depth" + m.depth());
        System.out.println("Size" + m.size());
        try{
            Socket socket = new Socket("localhost", 8888);
            System.out.println("Connect established!");
            //PrintStream out = new PrintStream(socket.getOutputStream());
            PrintStream out = new PrintStream(socket.getOutputStream());
            out.println(info);
            out.close();
        }catch(Exception e){
            System.out.println("Error"+e);
        }




    }

}
