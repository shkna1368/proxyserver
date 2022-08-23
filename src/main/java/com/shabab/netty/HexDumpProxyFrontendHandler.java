package com.shabab.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.*;
import io.netty.channel.Channel;
  import io.netty.channel.ChannelFuture;
  import io.netty.channel.ChannelFutureListener;
  import io.netty.channel.ChannelHandlerContext;
  import io.netty.channel.ChannelInboundHandlerAdapter;
  import io.netty.channel.ChannelOption;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayOutputStream;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HexDumpProxyFrontendHandler extends ChannelInboundHandlerAdapter {
StringBuilder stringBuilder=new StringBuilder();
         private final String remoteHost;
      private final int remotePort;

          // As we use inboundChannel.eventLoop() when building the Bootstrap this does not need to be volatile as
          // the outboundChannel will use the same EventLoop (and therefore Thread) as the inboundChannel.
          private Channel outboundChannel;

          public HexDumpProxyFrontendHandler(String remoteHost, int remotePort) {
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
     public void channelRead(final ChannelHandlerContext ctx, Object msg) throws DecoderException, JSQLParserException {


                 ByteBuf buf = (ByteBuf) msg;

                 byte[] bytes = new byte[buf.readableBytes()];
                 int readerIndex = buf.readerIndex();
                 buf.getBytes(readerIndex, bytes);


                 String st = String.format("%02X", bytes[0]);

                 String st2 = String.format("%02X", bytes[1]);
                 String st3 = String.format("%02X", bytes[2]);
                 String st4 = String.format("%02X", bytes[3]);
                 String st5 = String.format("%02X", bytes[4]);
                 String st6 = String.format("%02X", bytes[5]);


                switch (st){

                    case "12" :
                        System.out.println("preLogin");
                         break;
                    case "03":
                        System.out.println("RPC");
                     break;

                    case "01":
                        byte b[] = Arrays.copyOfRange(bytes, 30, bytes.length-1);
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

                        System.out.println("sqlBatch");

                        sqlPars(str2);
                         break;



                }



/*
                 byte[] bs=new byte[buf.capacity()];



                 for (int i = 0; i < buf.capacity(); i++) {
                     bs[i] = buf.getByte(i);

                 }*/








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


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


public void sqlPars(String query) throws JSQLParserException {

    Statement statement = CCJSqlParserUtil.parse(query);
    if (statement instanceof Update) {

    } else if (statement instanceof Delete) {

    } else if (statement instanceof Insert) {
        System.out.println("Query is INSER");
        Insert selectStatement = (Insert) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);

        System.out.println("table list:+"+tableList);


    } else if (statement instanceof Select) {

        System.out.println("Query is select");

        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);

        System.out.println("table list:+"+tableList);

    }


else if (statement instanceof Alter) {

        System.out.println("Query is Alter");
    }



}

}