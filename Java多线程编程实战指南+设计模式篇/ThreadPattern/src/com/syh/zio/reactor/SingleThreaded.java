package com.syh.zio.reactor;

import com.sun.corba.se.pept.transport.Acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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
 * Time: 上午2:58
 * To change this template use File | Settings | File Templates.
 */
public class SingleThreaded implements Runnable{
    final Selector selector;
    final ServerSocketChannel serverSocket;

    public SingleThreaded(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        sk.attach(new Acceptor());
    }

    class Acceptor implements Runnable{

        @Override
        public void run() {
            try {
                SocketChannel c = serverSocket.accept();
                if (c != null)
                    new Handler(selector, c);
            }
            catch(Exception ex) { /* ... */ }
        }
    }

    class Handler implements Runnable{
        final SocketChannel socket;
        final SelectionKey sk;
        ByteBuffer input = ByteBuffer.allocate(1024);
        ByteBuffer output = ByteBuffer.allocate(1024);
        static final int READING = 0, SENDING = 1;
        int state = READING;

        public Handler(Selector sl, SocketChannel socket) throws Exception {
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
            } else if (state == SENDING) {
                write();
            }
        }

        private void read() {
            process();
            //下一步处理写事件
            sk.interestOps(SelectionKey.OP_WRITE);
            this.state = SENDING;
        }

        private void write() {
            process();
            //下一步处理读事件
            sk.interestOps(SelectionKey.OP_READ);
            this.state = READING;
        }

        /**
         * task 业务处理
         */
        public void process() {
            //do something
        }
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
}
