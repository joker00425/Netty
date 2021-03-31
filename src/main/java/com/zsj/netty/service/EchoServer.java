package com.zsj.netty.service;

import com.zsj.netty.service.handler.EchoServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;


public class EchoServer {

    private  int port;

    public EchoServer(int port){
        this.port = port;
    }
    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt("8080");
        new EchoServer(port).start();
    }

    public void start() throws Exception{
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            System.out.println(EchoServer.class.getName() + " started and listen on" + f.channel().localAddress());
            f.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            group.shutdownGracefully().sync();
        }
    }
}
