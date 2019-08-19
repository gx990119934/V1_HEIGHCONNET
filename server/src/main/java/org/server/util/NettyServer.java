package org.server.util;

import org.common.util.MarshallingCodeCFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyServer {

	public NettyServer() {
		
		//1.创建两个工作线程组，一个用于处理网络连接，一个用于处理实际业务
		EventLoopGroup bossGrop=new NioEventLoopGroup();
		EventLoopGroup workGroup=new NioEventLoopGroup();
		
		//2.辅助类
		ServerBootstrap serverBootstrap=new ServerBootstrap();
		try {
			//2.1将线程组添加进辅助类
			serverBootstrap.group(bossGrop,workGroup)
			//2.2设置Channle类型
			.channel(NioServerSocketChannel.class)
			//2.3缓冲区大小，DEFAULT表示自适应
			.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
			//2.4缓冲区池化操作
			.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
			//2.5日志级别
			.handler(new LoggingHandler(LogLevel.INFO))
			//2.6为channle添加handler,处理channle
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					//2.6.1第一个handlder解密数据
					ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
					//2.6.2第二个handlder加密数据
					ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
					//2.6.3第三个handlder处理真实业务
					ch.pipeline().addLast(new ServerHandler());
				}
			});
			
			//3.绑定端口，同步等到请求连接
			ChannelFuture cf=serverBootstrap.bind(9876).sync();
			System.err.println("Server Startup...");
			//4.关闭通道
			cf.channel().close().sync();
		}catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGrop.shutdownGracefully();
			workGroup.shutdownGracefully();
			System.err.println("Sever ShutDown...");
		}
		
	}
}
