package com.syh.zio.nio;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 19-4-3
 * Time: 上午2:18
 * To change this template use File | Settings | File Templates.
 */
public class NIOServer extends Thread {
    Selector selector = null;
    ServerSocketChannel serverSocket = null;

    public NIOServer(int port) throws Exception {


        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                //阻塞等待事件
                selector.select();
                // 事件列表
                Set selected = selector.selectedKeys();
                Iterator it = selected.iterator();
                while (it.hasNext()) {
                    it.remove();
                    //分发事件
                    dispatch((SelectionKey) (it.next()));
                }
            } catch (Exception e) {
            }
        }
    }

    private void dispatch(SelectionKey key) throws Exception {
        if (key.isAcceptable()) {
            register(key);//新链接建立，注册
        } else if (key.isReadable()) {
            read(key);//读事件处理
        } else if (key.isWritable()) {
            wirete(key);//写事件处理
        }
    }

    private void wirete(SelectionKey key) {
    }

    private void read(SelectionKey key) {
    }

    private void register(SelectionKey key) throws Exception {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        // 获得和客户端连接的通道
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        //客户端通道注册到selector 上
        channel.register(this.selector, SelectionKey.OP_READ);
    }
}
