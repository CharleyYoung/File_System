package com.company;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Year;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author YangJian
 * FileServer 该类实现了服务端
 */
public class FileServer {
    //定义服务器socket
    ServerSocket serverSocket;
    static DatagramSocket datagramSocket;
    //定义服务器端口
    private final int TCP = 2021;
    private final int UDP = 2020;
    private final String HOST = "127.0.0.1";
    private final int POOLSIZE = 10;// 线程池容量
    ExecutorService executorService;
    //获取服务器根目录
    private String rootpath;

    /**
     * 含参构造函数，参数为从program arguments中读入的数据，用于设置根目录
     * @param args 从program arguments中读入的数据
     * @throws IOException
     */
    public FileServer(String[] args) throws IOException {
        serverSocket = new ServerSocket(TCP, 2); // 创建服务器端套接字
        datagramSocket = new DatagramSocket(UDP);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOLSIZE);
        //令根目录为args传入的参数
        rootpath = args[0];
        //设置服务器目录（可更改）
        System.out.println(rootpath);
        System.out.println("服务器启动。");
    }

    public static void main(String[] args) throws IOException {
        new FileServer(args).servic();//启动服务器
    }

    /**
     * 服务实现
     */
    public void servic(){
        Socket socket = null;
        while (true) {
            try {
                // 等待并取出用户连接，并创建套接字
                socket = serverSocket.accept();
                Handler handler = new Handler(socket,rootpath);
                executorService.execute(handler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
