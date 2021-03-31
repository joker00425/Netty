package com.zsj.bilibiliNetty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {

    public static void main(String[] args)  throws Exception {

        EventLoopGroup bossGroup  = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128) //设置队列得到的连接个数
                .childOption(ChannelOption.SO_KEEPALIVE, true) //设置活动连接状态
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    //创建一个通道初始化对象，给pipeline设置处理器
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new NettyServerHandler());
                    }
                })//workerGroup的EventLoop 对应的管道设置处理器
        ;
        //绑定端口并且同步，并且生成了ChannelFuture对象
        ChannelFuture sync = bootstrap.bind(6668).sync();

        //对关闭通道进行监听
        sync.channel().closeFuture().sync();
    }
}
