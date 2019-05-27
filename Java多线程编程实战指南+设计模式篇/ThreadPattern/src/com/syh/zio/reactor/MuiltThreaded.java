package com.syh.zio.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 19-4-4
 * Time: 上午11:39
 * To change this template use File | Settings | File Templates.
 */
public class MuiltThreaded implements Runnable {
    final Selector selector;
    final ServerSocketChannel serverSocket;

    public MuiltThreaded(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        sk.attach(new Acceptor());
    }

    @Override
    public void run() {
        try{
            while (!Thread.interrupted()){
                selector.select();
                Set selected = selector.selectedKeys();
                Iterator it = selected.iterator();
                while (it.hasNext())
                    dispatch((SelectionKey) it.next());
                selected.clear();
            }
        } catch (IOException e){

        }
    }

    void dispatch(SelectionKey k) {
        Runnable r = (Runnable)(k.attachment());
        if (r != null)
            r.run();
    }

    class Acceptor implements Runnable{

        @Override
        public void run() {
            try {
                SocketChannel c = serverSocket.accept();
                if (c != null)
                    new MultiThreadHandler(c, selector);
            }
            catch(Exception ex) { /* ... */ }
        }
    }

    class MultiThreadHandler implements Runnable {
        public static final int READING = 0, WRITING = 1;
        int state;
        final SocketChannel socket;
        final SelectionKey sk;

        //多线程处理业务逻辑
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        public MultiThreadHandler(SocketChannel socket, Selector sl) throws Exception {
            this.state = READING;
            this.socket = socket;
            sk = socket.register(selector, SelectionKey.OP_READ);
            sk.attach(this);
            socket.configureBlocking(false);
        }

        @Override
        public void run() {
            if (state == READING) {
                read();
            } else if (state == WRITING) {
                write();
            }
        }

        private void read() {
            //任务异步处理
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    process();
                }
            });

            //下一步处理写事件
            sk.interestOps(SelectionKey.OP_WRITE);
            this.state = WRITING;
        }

        private void write() {
            //任务异步处理
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    process();
                }
            });

            //下一步处理读事件
            sk.interestOps(SelectionKey.OP_READ);
            this.state = READING;
        }

        /**
         * task 业务处理
         */
        public void process() {
            //do IO ,task,queue something
        }
    }


}
