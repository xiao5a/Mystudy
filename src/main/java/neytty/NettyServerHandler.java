package neytty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 自定义Handler需要继承netty规定好的某个HandlerAdapter(规范)
 */
public  class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channelGroup.writeAndFlush("客户端"+ctx.channel().remoteAddress()+"上线了");
        System.out.println("客户端"+ctx.channel().remoteAddress()+"上线了");
        channelGroup.add(ctx.channel());
    }


    /**
     * 读取客户端发送的数据
     *
     * @param ctx 上下文对象, 含有通道channel，管道pipeline
     * @param msg 就是客户端发送的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("服务器读取线程 " + Thread.currentThread().getName());
        //Channel channel = ctx.channel();
        //ChannelPipeline pipeline = ctx.pipeline(); //本质是一个双向链接, 出站入站
        //将 msg 转成一个 ByteBuf，类似NIO 的 ByteBuffer

//            ByteBuf buf = (ByteBuf) msg;
        Channel channel = ctx.channel();

        channelGroup.forEach(ch -> {
                    if (channel != ch) { //不是当前的 channel,转发消息
                        ch.writeAndFlush("[ 客户端 ]" + channel.remoteAddress() + " 发送了消息：" + msg + "\n");
                    } else {//回显自己发送的消息给自己
                        ch.writeAndFlush("[ 自己 ]发送了消息：" + msg + "\n");
                    }
                }
        );
        System.out.println("客户端发送消息是:" + msg);
    }

    /**
     * 数据读取完毕处理方法
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//            ByteBuf buf = Unpooled.copiedBuffer("HelloClient", CharsetUtil.UTF_8);
//            ctx.writeAndFlush("HelloClient");
    }

    /**
     * 处理异常, 一般是需要关闭通道
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
