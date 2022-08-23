package com.shabab.netty.postgres;


import com.shabab.netty.HexDumpProxyFrontendHandler;
import com.shabab.netty.TimeDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HexDumpProxyInitializerPostgres extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(HexDumpProxyInitializerPostgres.class);

              private final String remoteHost;
      private final int remotePort;

              public HexDumpProxyInitializerPostgres(String remoteHost, int remotePort) {
                 this.remoteHost = remoteHost;
                 this.remotePort = remotePort;
             }

             @Override
      public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(
                                  new LoggingHandler(LogLevel.INFO),
                                 new HexDumpProxyFrontendHandlerPostgres(remoteHost, remotePort),
                           new TimeDecoder()




                );




      /*      new ReplayingDecoder() {
                     @Override
                     protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
                             throws Exception {
                         short magicHeader = in.readShort();
                         logger.debug("Receive magic header: {}.", magicHeader);
                    *//*     if (magicHeader != HEADER) {
                             logger.error("Receive illegal magic header: {}, close channel: {}.",
                                     magicHeader, ctx.channel().remoteAddress());
                             ctx.close();
                         }
*//*
                         short dataLen = in.readShort();
                         logger.debug("Receive message data length: {}.", dataLen);
                         if (dataLen < 0) {
                             logger.error("Data length is negative, close channel: {}.",
                                     ctx.channel().remoteAddress());
                             ctx.close();
                         }

                         ByteBuf payload = in.readBytes(dataLen);
                         String cloudMsg = payload.toString(CharsetUtil.UTF_8);
                         logger.debug("Receive data: {}.", cloudMsg);
                         out.add(cloudMsg);
                     }
                 }).addLast(new MessageToByteEncoder<String>() {
                     @Override
                     protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out)
                             throws Exception {
                         out.writeBytes(msg.getBytes());
                     }
                 }).addLast(new ChannelInboundHandlerAdapter() {
                     @Override
                     public void channelActive(ChannelHandlerContext ctx) throws Exception {
                         logger.info("start receive msg...");
                     }

                     @Override
                     public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                         logger.info("receive msg: {}", msg);
                         logger.info("echo msg");
                         ctx.writeAndFlush(msg);
                     }
                 });*/
  }}