import java.io.*;
import java.net.*;
import org.opencv.highgui.*;
import org.opencv.core.Mat;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.Runtime.*;
import java.nio.ByteBuffer;
import java.util.Date;

public class Main {

        public static void printInputStream(InputStream is) throws IOException {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                try {
                        for (;;) {
                                String line = br.readLine();
                                if (line == null) break;
                                System.out.println(line);
                        }
                } finally {
                        br.close();
                }
        }

                
        private static final int PORT = 9876;
        private static final String ANDROID_ADDR = "192.168.1.6";
        
        public static void main(String argv[]) {  
                System.out.println("サーバ起動");  
                int num = 0;
                int counter = 0;
                ServerSocket serverSocket = null;
                double latitude, longitude;
                byte[] lat = new byte[8]; // latitude
                byte[] lng = new byte[8]; // longitude
                
                while(true){  
                        try {
                                ///////////////////////////
                                /* get data from Android */
                                ///////////////////////////
                                
                                if(serverSocket == null) serverSocket = new ServerSocket(PORT);  
                                Socket socket = serverSocket.accept();
                                Date d = new Date();
                                String file_name = /*num*/d.toString()  + ".jpg";
                                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file_name)));  
                                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                                in.read(lat);
                                in.read(lng);
                                byte[] buf = new byte[1024];  
                                int len;  
                                while((len=in.read(buf))!=-1){  
                                        out.write(buf, 0, len);  
                                }
                                latitude = ByteBuffer.wrap(lat).getDouble();
                                longitude = ByteBuffer.wrap(lng).getDouble();
                                System.out.println(latitude);
                                System.out.println(longitude);
                                // GUIで画像を表示  
                                //  new GUIExe(num);  
                                // 入出力ストリームを閉じる  
                                out.flush();  
                                out.close();  
                                in.close();  
                                System.out.println("done");  
                                socket.close();  
                                

                                //////////////////////////
                                /* send data to Android */
                                //////////////////////////
                                Socket socket2 = new Socket(ANDROID_ADDR, PORT);
                                OutputStream os = socket2.getOutputStream();
                                PrintStream pos = new PrintStream(os);

                                /* classification */
                                Runtime r = Runtime.getRuntime();
                                String e = "./cafe.sh " + file_name;
                                System.out.print(e);
                                Process process = r.exec(e);
                                InputStream is = process.getInputStream();	//標準出力
                                //printInputStream(is);
                                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                                String result = br.readLine();
                                br.close();

                                pos.printf(result);
                                
                                os.flush();
                                socket2.close();
                                
                                // ソケットを閉じる  
                                num++;
                        } catch(Exception e) {  
                                e.printStackTrace();  
                        }  
                }  
        }  
}  
  
class GUIExe extends Thread {  
        private int num;  
   
        public GUIExe(int num) {  
                this.num = num;  
                this.start();  
                // スレッド開始  
        }  
        public void run() {  
                new GUI(num);  
        }  
}

