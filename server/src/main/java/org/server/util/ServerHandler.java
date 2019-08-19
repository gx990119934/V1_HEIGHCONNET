package org.server.util;

import org.common.disruptor.MessageProducer;
import org.common.entity.TranslatorData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//1.将message转换为公共对象
		TranslatorData request = (TranslatorData)msg;
		//自已的应用服务应该有一个ID生成规则
    	String producerId = "code:sessionId:001";
    	MessageProducer messageProducer = RingBufferWorkerPoolFactory.getInstance().getMessageProducer(producerId);
    	messageProducer.onData(request, ctx);
    }

}
