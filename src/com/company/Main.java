package com.company;

import java.io.File;
import java.util.StringTokenizer;

public class Main {

    public static void main(String[] args) {
        String filepath = args[0];
        try{
            File file = new File(filepath);
            File[] fileList = file.listFiles();
            for(File file1:fileList){
                System.out.println("Path: "+file1.getPath()+"绝对路径: "+file1.getAbsolutePath());
                System.out.println(file1.isDirectory());
            }
            File testFile = fileList[0];
            File[] files = testFile.listFiles();
            for(File file1:files){
                System.out.println("Path: "+file1.getPath()+"绝对路径: "+file1.getAbsolutePath());
                System.out.println(file1.isDirectory());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
	// write your code here
    }
}
