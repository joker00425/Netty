package com.zsj.groupChat;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * 抽象群聊服务器
 */
public class GroupChatServer {

    private static final int PORT = 6667;
    //选择器
    private Selector selector;
    //处理客户端连接
    private ServerSocketChannel listenChannel;

    public GroupChatServer(){
        try{
            selector = Selector.open();
            listenChannel = ServerSocketChannel.open();
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            //设置非阻塞
            listenChannel.configureBlocking(false);
            //将该listenChannel注册到selector上
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    //服务器端监听
    public void listen(){
        try {
            while (true){
                int count = selector.select(2000);
                //有事件处理
                if(count > 0){
                    //遍历得到的SelectionKey集合
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();

                        if(key.isAcceptable()){
                            SocketChannel sc = listenChannel.accept();
                            sc.configureBlocking(false);
                            sc.register(selector, SelectionKey.OP_READ);
                            System.out.println(sc.getRemoteAddress() + "此人已上线");
                        }
                        //通道发生read事件
                        else if(key.isReadable()){
                            readData(key);
                        }
                        //处理完当前Key后进行删除，防止重复处理
                        iterator.remove();;
                    }

                }else {
                    System.out.println("等待中");

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }
    }

    //读取客户端消息
    private void readData(SelectionKey key){
        //定义socketChannel
        SocketChannel channel = null;
        try{
            channel = (SocketChannel) key.channel();
            //创建Buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int readCount = channel.read(buffer);
            if(readCount > 0){
                String msg = new String(buffer.array());
                System.out.println("获取来自客户端的消息" + msg);

                //向其他客户端转发消息
                sendMsgToOtherClients(msg, channel);
            }

        }catch (IOException e){
            try {
                //发生离线了就会出现异常
                System.out.println(channel.getRemoteAddress() + "离线了");
                //离线处理
                key.channel();
                channel.close();;
            }catch (Exception error){
                e.printStackTrace();
            }
        }
    }

    private void sendMsgToOtherClients (String msg, SocketChannel self) throws  IOException{
        System.out.println("服务器转发消息中");
        //遍历所有selector 上的 socketChannel
        for(SelectionKey key : selector.keys()){
            //通过key取出对应的SocketChannel
            Channel targetChannel = key.channel();
            //排除转发自己
            if (targetChannel instanceof SocketChannel && targetChannel != self) {
                //转型
                SocketChannel dest = (SocketChannel) targetChannel;
                //将msg，存储到buffer
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                //将buffer的数据写入通道
                dest.write(buffer);
            }
        }
    }
    public static void main(String[] args) {

        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.listen();
    }
}
