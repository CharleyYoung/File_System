package com.company;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author YangJian
 * Handler 该类负责实际处理用户命令
 */
public class Handler implements Runnable{
    private Socket socket;
    private static final int TCP = 2021;
    private static final int UDP = 2020;
    private static final String HOST = "127.0.0.1";
    private static final int sendSize = 1500;
    private String rootpath;
    private String filepath;
    private final int POOLSIZE = 10;// 线程池容量
    ExecutorService executorService;
    SocketAddress socketAddres;
    BufferedReader br;
    BufferedWriter bw;
    PrintWriter pw;

    /**
     *
     * @param socket 传入的socket
     * @param rootpath 传入的根路径
     */
    public Handler(Socket socket, String rootpath){
        this.socket = socket;
        this.rootpath = rootpath;
        this.filepath = this.rootpath;
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOLSIZE);
    }

    /**
     * 初始化输入输出流
     * @throws IOException
     */
    public void initStream() throws IOException { // 初始化输入输出流对象方法
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bw = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));
        pw = new PrintWriter(bw, true);
    }

    /**
     * 服务实现
     */
    @Override
    public void run() {
        try {
            System.out.println("新连接，连接地址：" + socket.getInetAddress() + "："
                    + socket.getPort()); //客户端信息
            initStream(); // 初始化输入输出流对象
            //向客户端发送链接相关信息
            pw.println(socket.getInetAddress()+":"+socket.getPort()+">链接成功");
            String info = null;
            while (null != (info = br.readLine())) {
                //定义一个String数组来存放切分的数组
                String[] command = info.split(" ",2);
                System.out.println(command[0]);
                //条件嵌套判断命令标识，根据命令标识执行不同的命令
                if(command[0].equals("ls")){
                    //调用执行ls的函数
                    lsFunction();
                }else if(command[0].equals("cd")){
                    //调用执行cd的函数
                    cdFunction(command);
                }else if(command[0].equals("get")){
                    System.out.println("start file service");
                    getFunction(command);
                    System.out.println("finish file service");
                }else if(command[0].equals("bye")){
                    pw.println("链接即将关闭");
                    break;
                }else {
                    pw.println("unknown commands");
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        } finally{
            if (null != socket) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ls命令的实现函数
     */
    public void lsFunction(){
        File file = new File(filepath);
        //获取目录下的所有文件
        File[] files = file.listFiles();
        //判断该目录下是否有文件
        if(files==null){
            pw.println("no directory");
        }else {
            outDirectory(files);
        }
    }

    /**
     * cd命令的实现函数
     * @param command 传入进来的经过split切割获得的String数组
     */
    public void cdFunction(String[] command){
        if(command.length==1){
            pw.println("unknown commands");
        }else{
            File file = new File(filepath);
            String subFileName = command[1];
            if(subFileName.equals("..")){
                filepath = file.getPath();
                //判断当前是否已经到达根目录
                if(filepath.equals(rootpath)){
                    pw.println(file.getName()+">OK");
                }else {
                    filepath = file.getParentFile().getPath();
                    String parentFileName = file.getParentFile().getName();
                    pw.println(parentFileName + ">OK");
                }
            }else{
                String subpath = filepath+"\\"+subFileName;
                //创建路径为subpath的文件变量
                File subDir = new File(subpath);
                if(subDir.exists()){
                    filepath=subpath;
                    pw.println(subFileName+">OK");
                }else{
                    pw.println("no such directory");
                }
            }
        }
    }

    /**
     * get命令的实现函数
     * @param command 传入进来的经过split切割获得的String数组
     * @throws IOException
     * @throws InterruptedException
     */
    public void getFunction(String[] command) throws IOException, InterruptedException {
        if(command.length==1){
            pw.println(-1);
        }else{
            String subFileName = command[1];
            String subpath = filepath+"\\"+subFileName;
            //创建路径为subpath的文件变量
            File subDir = new File(subpath);
            if(subDir.exists()){
                if(subDir.isFile()) {
                    pw.println(subDir.length());
                    //获取客户端的UDP端口
                    int CUDP = Integer.parseInt(br.readLine());
                    System.out.println("Handler类获取的客户端UDP端口号为："+CUDP);
                    //UDP传输
                    socketAddres = new InetSocketAddress(HOST, CUDP);
                    UDPHandler udpHandler = new UDPHandler(socketAddres,subDir);
                    executorService.execute(udpHandler);
                }else{
                    pw.println(-1);
                }
            }else{
                pw.println(-1);
            }
        }
    }

    /**
     * 负责输出目录下所有子文件的函数
     * @param files 传入的file目录下的所有文件列表
     */
    public void outDirectory(File[] files){
        //循环输出目录
        for (File file1 : files) {
            //通过type来判断file1是目录还是文件
            int type = file1.getName().lastIndexOf(".");
            if(type == -1) {
                pw.println("<dir>   " + file1.getName() + "     " + file1.length() + "Bytes");
            }else{
                pw.println("<file>  "+file1.getName()+"     "+file1.length()+"Bytes");
            }
        }
        pw.println("");
    }

}
