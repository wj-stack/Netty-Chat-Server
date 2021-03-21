import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.channel.Channel;

public class MyServerHandler extends SimpleChannelInboundHandler<String> {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 每当服务端收到新的客户端连接时,客户端的channel存入ChannelGroup列表中,并通知列表中其他客户端channel
     * 
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        channels.add(ctx.channel());
        // 获取连接的channel
        Channel incomming = ctx.channel();
        // 通知所有已经连接到服务器的客户端，有一个新的通道加入
        for (Channel channel : channels) {
            channel.writeAndFlush("[SERVER]-" + incomming.remoteAddress() + "加入\n");
        }
    }

    /**
     * 每当服务端断开客户端连接时,客户端的channel从ChannelGroup中移除,并通知列表中其他客户端channel
     * 
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 获取连接的channel
        Channel incomming = ctx.channel();
        for (Channel channel : channels) {
            channel.writeAndFlush("[SERVER]-" + incomming.remoteAddress() + "离开\n");
        }
        // 从服务端的channelGroup中移除当前离开的客户端
        channels.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 打印出客户端地址
        System.out.println(ctx.channel().remoteAddress() + ", " + msg);
        // ctx.channel().writeAndFlush("form server: " + UUID.randomUUID());

        Channel incomming = ctx.channel();
        // 将收到的信息转发给全部的客户端channel
        for (Channel channel : channels) {
            if (channel != incomming) {
                channel.writeAndFlush("[" + incomming.remoteAddress() + "]" + msg + "\n");
            } else {
                channel.writeAndFlush("[You]" + msg + "\n");
            }
        }
    }

    /**
     * 服务端监听到客户端不活动
     * 
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 服务端接收到客户端掉线通知
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "掉线");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "异常");
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 服务端监听到客户端活动
     * 
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 服务端接收到客户端上线通知
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:" + incoming.remoteAddress() + "在线");
    }

}