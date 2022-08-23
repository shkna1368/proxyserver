package com.shabab.netty.postgres;

import com.shabab.netty.HexDumpProxyFrontendHandler;
import io.netty.channel.*;

public class HexDumpProxyBackendHandlerPostgres extends ChannelInboundHandlerAdapter {

            private final Channel inboundChannel;

            public HexDumpProxyBackendHandlerPostgres(Channel inboundChannel) {
               this.inboundChannel = inboundChannel;
           }

             @Override
     public void channelActive(ChannelHandlerContext ctx) {
                 ctx.read();
           }

            @Override
     public void channelRead(final ChannelHandlerContext ctx, Object msg) {



                inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                               if (future.isSuccess()) {
                                        ctx.channel().read();
                                    } else {
                                        future.channel().close();
                                    }
                          }
        });
            }

            @Override
     public void channelInactive(ChannelHandlerContext ctx) {
                HexDumpProxyFrontendHandlerPostgres.closeOnFlush(inboundChannel);
             }

           @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                 HexDumpProxyFrontendHandlerPostgres.closeOnFlush(ctx.channel());
           }
 }