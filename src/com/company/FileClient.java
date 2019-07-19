package com.company;

import org.omg.CORBA.portable.UnknownException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author YangJian
 * FileClient 该类实现了一个客户端
 */
public class FileClient {
    private static final int TCP = 2021;
    private static final int UDP = 2020;
    private static final int sendSize = 1500;
    private static final String HOST = "127.0.0.1";
    //用于跳出!br.ready()的死循环
    private int count = 0;
    private Socket socket = new Socket();
    private DatagramSocket datagramSocket;
    BufferedWriter bw;
    BufferedReader br;
    PrintWriter pw;

    /**
     * FileClient函数，用于初始化Client
     * @throws IOException
     */
    private FileClient() throws IOException{
        //socket = new Socket(HOST, PORT); //创建客户端套接字
        socket = new Socket();
        socket.connect(new InetSocketAddress(HOST, TCP));
        //datagramSocket = new DatagramSocket(UDP);
    }

    public static void main(String[] args) throws UnknownException, IOException {
        new FileClient().send();
    }

    /**
     * initStream函数，用于初始化数据读入，输出流
     * @throws IOException
     */
    public void initStream() throws IOException{
        //客户端输出流，向服务器发消息
         bw = new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream()));
        //客户端输入流，接收服务器消息
         br = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
         pw = new PrintWriter(bw, true); //装饰输出流，及时刷新
    }

    /**
     *  发送命令实现
     */
    public void send(){
        try {
            initStream();
            Scanner in = new Scanner(System.in); //接受用户信息
            //接收链接成功的信息
            System.out.println(br.readLine());
            String msg = null;
            while ((msg = in.nextLine()) != null) {
                pw.println(msg); //发送给服务器端
                //定义一个String数组来存放切分的数组
                String[] command = msg.split(" ",2);
                String cmd = command[0];
                if(cmd.equals("get")){//判断是文件传输命令
                    long fileLength = Long.parseLong(br.readLine());
                    System.out.println("要传输的文件大小为："+fileLength);
                    if(fileLength!=-1){
                        System.out.println("开始传输文件");
                        getFile(command[1], fileLength);
                        System.out.println("get file successfully");
                    }else{
                        System.out.println("unknown command");
                    }
                }
                //尝试解决br.ready()的神奇问题
                while (!br.ready()){
                    if(count==30){
                        count = 0;
                        System.out.println("br is still nor ready, you may need to restart the client");
                        break;
                    }else {
                        count++;
                        Thread.sleep(100);
                    }
                }
                //考虑服务器返回可能不止一行，在此循环输出服务器返回的数据
                while(br.ready()) {
                    System.out.println(br.readLine()); //输出服务器返回的消息
                }
                if (msg.equals("bye")) {
                    break; //退出
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                try {
                    socket.close(); //断开连接
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 用于接收文件的函数
     * @param fileName 要接收的文件名
     * @param fileLength 要接收的文件路径
     * @throws IOException
     */
    private void getFile(String fileName, long fileLength) throws IOException {
        datagramSocket = new DatagramSocket();
        //告知服务器当前客户端申请的UDP端口
        pw.println(datagramSocket.getLocalPort());
        System.out.println("客户端的UDP端口号为:"+datagramSocket.getLocalPort());
        //定义传输标识变量
        double temp = 0;
        double t = 0;
        //定义传输比例计算变量
        DecimalFormat df = new DecimalFormat("##.##%");
        DatagramPacket dp = new DatagramPacket(new byte[sendSize], sendSize);
        byte[] recInfo = new byte[sendSize];
        //定义文件输出位置
        FileOutputStream fos = new FileOutputStream(new File(("D:\\") + fileName));
        int count = (int) (fileLength / sendSize) + ((fileLength % sendSize) == 0 ? 0 : 1);
        while ((count--) > 0) {//循环接收datagram
            datagramSocket.receive(dp);
            recInfo = dp.getData();
            fos.write(recInfo, 0, dp.getLength());
            temp += dp.getLength();
            t = temp/fileLength;
            int p = (int)(t*100);
            if(df.format(t).equals("100%")) {
                System.out.println("\r■■■■■downloaded " + df.format(t));
            }else {
                if(p%20==0&&p/20==0){
                    System.out.print("\rdownloaded □□□□□"+df.format(t));
                }else if(p%20==0&&p/20==1){
                    System.out.print("\rdownloaded ■□□□□"+df.format(t));
                }else if(p%20==0&&p/20==2){
                    System.out.print("\rdownloaded ■■□□□"+df.format(t));
                }else if(p%20==0&&p/20==3){
                    System.out.print("\rdownloaded ■■■□□"+df.format(t));
                }else if(p%20==0&&p/20==4){
                    System.out.print("\rdownloaded ■■■■□"+df.format(t));
                }
            }
            fos.flush();
        }
        datagramSocket.close();
        fos.close();
    }
}
