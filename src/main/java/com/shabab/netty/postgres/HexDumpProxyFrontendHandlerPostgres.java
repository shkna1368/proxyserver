package com.shabab.netty.postgres;

import com.shabab.netty.HexDumpProxyBackendHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HexDumpProxyFrontendHandlerPostgres extends ChannelInboundHandlerAdapter {
StringBuilder stringBuilder=new StringBuilder();
         private final String remoteHost;
      private final int remotePort;

          // As we use inboundChannel.eventLoop() when building the Bootstrap this does not need to be volatile as
          // the outboundChannel will use the same EventLoop (and therefore Thread) as the inboundChannel.
          private Channel outboundChannel;

          public HexDumpProxyFrontendHandlerPostgres(String remoteHost, int remotePort) {
              this.remoteHost = remoteHost;
              this.remotePort = remotePort;
          }

          @Override
      public void channelActive(ChannelHandlerContext ctx) {
                final Channel inboundChannel = ctx.channel();

                // Start the connection attempt.
                Bootstrap b = new Bootstrap();
                b.group(inboundChannel.eventLoop())
                 .channel(ctx.channel().getClass())
                 .handler(new HexDumpProxyBackendHandler(inboundChannel))
                 .option(ChannelOption.AUTO_READ, false);
                ChannelFuture f = b.connect(remoteHost, remotePort);
                outboundChannel = f.channel();
                f.addListener(new ChannelFutureListener() {
             @Override
             public void operationComplete(ChannelFuture future) {
                                 if (future.isSuccess()) {
                                        // connection complete start to read first data
                                        inboundChannel.read();
                                    } else {
                                        // Close the connection if the connection attempt has failed.
                                        inboundChannel.close();
                                    }
                             }
          });
              }

             @Override
     public void channelRead(final ChannelHandlerContext ctx, Object msg) throws DecoderException {


                 ByteBuf buf = (ByteBuf) msg;

                 byte[] bytes = new byte[buf.readableBytes()];
                 int readerIndex = buf.readerIndex();
                 buf.getBytes(readerIndex, bytes);




                 byte b[] = Arrays.copyOfRange(bytes, 6, bytes.length-2);
                 List<Byte> nonZeo=new ArrayList<>();

                 for(int i=0;i<b.length;i++){

                     if (b[i]!=0){

                         nonZeo.add(b[i]);
                     }

                 }

                 byte[] byteArrayNonZero = new byte[nonZeo.size()];
                 for (int index = 0; index < nonZeo.size(); index++) {
                     byteArrayNonZero[index] = nonZeo.get(index);
                 }





                 String hex36=Hex.encodeHexString(byteArrayNonZero);
                 String str2=new String(Hex.decodeHex(hex36));
                 System.out.println("query=*****************\n" +str2+"\n***********************");




                 String st = String.format("%02X", bytes[0]);






                 String hex=Hex.encodeHexString(bytes);



                 System.out.println("hex="+hex);





                 String str=new String(Hex.decodeHex(hex));

                 System.out.println( str);


         ;

              //   System.out.print((char) buffer.readByte());

     /*     byte x=buffer.getByte(0x79);




                 for (int i = 0; i < buffer.capacity(); i++) {
                     byte b = buffer.getByte(i);
                     System.out.println((char) b);
                 }


                 String str = ((ByteBuf)msg).toString(CharsetUtil.UTF_8);*/

               if (outboundChannel.isActive()) {
                       outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                  @Override
                 public void operationComplete(ChannelFuture future) {

                      System.out.println("finish");
                                          if (future.isSuccess()) {
                                                 // was able to flush out data, start to read the next chunk
                                                  ctx.channel().read();
                                             } else {
                                                future.channel().close();
                                              }
                                      }
             });
                    }
             }

              @Override
      public void channelInactive(ChannelHandlerContext ctx) {






                  System.out.println("value="+stringBuilder.toString());


                  if (outboundChannel != null) {
                       closeOnFlush(outboundChannel);
                     }
             }

             @Override
     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                 cause.printStackTrace();
                 closeOnFlush(ctx.channel());
            }

              /**
 98       * Closes the specified channel after all queued write requests are flushed.
 99       */
          static void closeOnFlush(Channel ch) {

                if (ch.isActive()) {

                      ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                     }
           }







}