package com.syh.zio.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 19-4-2
 * Time: 下午12:25
 * To change this template use File | Settings | File Templates.
 */
public class BlockServer {
    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(1986);
            System.out.println(" server init " );
            Socket socket = null;
            while (true){
                socket = server.accept();
                System.out.println(" server received connect . " );
                Thread thread = new BioServerHandle(socket);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
