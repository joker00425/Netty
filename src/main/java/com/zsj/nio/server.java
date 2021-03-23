package com.zsj.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @author 51567
 * NIO入门案例：实现服务器和客户端的简单通讯
 */
public class server {

    public static void main(String[] args) throws Exception  {

        //创建ServerSocketChannel用于处理客户端连接请求
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //selector用于遍历 socket连接 NIO应用事件驱动
        Selector selector = Selector.open();
        //服务端绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));
        //设置非阻塞
        serverSocketChannel.configureBlocking(false);
        //把 serverSocketChannel 注册到 selector ，关心事件为：OP_ACCEPT，有新的客户端连接

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //循环等待客户端连接
        while (true){
            if(selector.select(1000) == 0){
                System.out.println("系统等待1s, 无连接");
                continue;
            }
            //如果返回的 > 0,表示已经获取到关注的事件
            // 就获取到相关的 selectionKey 集合，反向获取通道
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            SelectionKey curKey = null;

            for(SelectionKey key : selectionKeys){
                curKey = key;
                //如果是 OP_ACCEPT，有新的客户端连接
                if (key.isAcceptable()) {
                    //该客户端生成一个 SocketChannel
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("客户端连接成功，生成了一个SocketChannel：" + socketChannel.hashCode());
                    //将SocketChannel设置为非阻塞
                    socketChannel.configureBlocking(false);
                    //将socketChannel注册到selector，关注事件为 OP_READ，同时给SocketChannel关联一个Buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                if (key.isReadable()) {
                    //通过key，反向获取到对应的Channel
                    SocketChannel channel = (SocketChannel) key.channel();
                    //获取到该channel关联的Buffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    channel.read(buffer);
                    System.out.println("from 客户端：" + new String(buffer.array()));
                }
                //手动从集合中移除当前的 selectionKey，防止重复操作
            }
            selectionKeys.remove(curKey);

        }




    }
}
