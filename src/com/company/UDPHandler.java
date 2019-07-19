package com.company;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Yang Jian
 * UDPHandler 类负责处理文件发送
 */
public class UDPHandler implements Runnable{
    private Socket socket;
    private static final int TCP = 2021;
    private static final String HOST = "127.0.0.1";
    private static final int sendSize = 1500;
    File file;
    DatagramSocket datagramSocket;
    SocketAddress socketAddres;
    BufferedWriter bw;
    PrintWriter pw;

    /**
     * UDPHandler 用于初始化UDPHandler
     * @param socket 传入的socket
     * @param subDir 传入的要传输的subDir
     */
    public UDPHandler(SocketAddress socket,File subDir){
        this.socketAddres = socket;
        this.file = subDir;
        this.datagramSocket = FileServer.datagramSocket;
    }

    /**
     * 初始化输入输出流
     * @throws IOException
     */
    public void initStream() throws IOException { // 初始化输入输出流对象方法

    }

    @Override
    /**
     * run 函数
     */
    public void run() {
        try {
            initStream();
            System.out.println("UDPHandler开始运作");
            DatagramPacket dp;
            byte[] sendInfo = new byte[sendSize];
            int size = 0;
            dp = new DatagramPacket(sendInfo, sendInfo.length, socketAddres);
            BufferedInputStream bfdIS = new BufferedInputStream(new FileInputStream(file));

            while ((size = bfdIS.read(sendInfo)) > 0) {
                dp.setData(sendInfo);
                datagramSocket.send(dp);
                sendInfo = new byte[sendSize];
                //休眠以确保发送顺序
                TimeUnit.MICROSECONDS.sleep(500);
            }
            bfdIS.close();
            //datagramSocket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
