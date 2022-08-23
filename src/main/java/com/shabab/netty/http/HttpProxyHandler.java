package com.shabab.netty.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;

public class HttpProxyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
       if (request.getMethod() == HttpMethod.POST) {

           ByteBuf jsonBuf = request.content();
           String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
           System.out.println("body:"+jsonStr);
       }
        ReferenceCountUtil.retain(request);

        //Create client connection target machine
        connectToRemote(ctx, "localhost", 7071, 1000).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    //The proxy server successfully connected to the target server
                    //Send message to target server
                    //Close long connection
                    request.headers().set(HttpHeaderNames.CONNECTION, "close");

                    //Forward request to target server
                    channelFuture.channel().writeAndFlush(request).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                //Remove the client's http codec
                                /*channelFuture.channel().pipeline().remove(ChannelHandlerDefine.HTTP_CLIENT_CODEC);
                                //Remove the http codec and aggregator between the proxy service and the requester channel
                                ctx.channel().pipeline().remove(ChannelHandlerDefine.HTTP_CODEC);
                                ctx.channel().pipeline().remove(ChannelHandlerDefine.HTTP_AGGREGATOR);*/
                                //After removal, let the channel directly become a simple ByteBuf transmission
                            }
                        }
                    });
                } else {
                    ReferenceCountUtil.retain(request);
                    ctx.writeAndFlush(getResponse(HttpResponseStatus.BAD_REQUEST, "The proxy service failed to connect to the remote service"))
                            .addListener(ChannelFutureListener.CLOSE);
                }
            }
        });
    }

    private DefaultFullHttpResponse getResponse(HttpResponseStatus statusCode, String message) {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, statusCode, Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
    }

    private ChannelFuture connectToRemote(ChannelHandlerContext ctx, String targetHost, int targetPort, int timeout) {
        return new Bootstrap().group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //Add http encoder
                        pipeline.addLast( new HttpClientCodec());
                        //Add a data transmission channel
                        pipeline.addLast(new DataTransHandler(ctx.channel()));
                    }
                })
                .connect(targetHost, targetPort);
    }
}