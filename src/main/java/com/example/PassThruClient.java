package com.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PassThruClient
 */
public class PassThruClient {
  private static final Logger logger = LoggerFactory.getLogger(PassThruClient.class);
  private static LinkedList<PassThruClient> clients = new LinkedList<PassThruClient>();
  private PassThruSocket client;
  private PassThruSocket server;
  private CompletionHandler connectHandler =
    new CompletionHandler<Void, PassThruSocket>() {
      public void completed(Void v, PassThruSocket attachment) {
        client.read(ByteBuffer.allocate(256));
        attachment.read(ByteBuffer.allocate(256));
      }

      public void failed(Throwable ex, PassThruSocket attachment) {
        logger.debug("connect: " + ex.getMessage());
        stop();
      }
    };
  private CompletionHandler readHandler =
    new CompletionHandler<Integer, PassThruSocket>() {
      public void completed(Integer count, PassThruSocket attachment) {
        logger.debug("readHandler: " + count);
        if (count > 0) {
          attachment.getReadBuffer().flip();
          byte[] contents = new byte[attachment.getReadBuffer().remaining()];
          attachment.getReadBuffer().get(contents);
          if (attachment.getDirection() == PassThruSocket.Direction.C2S) {
            server.write(ByteBuffer.wrap(contents));
          } else {
            client.write(ByteBuffer.wrap(contents));
          }
          logger.debug("{}:", attachment.getDirection().toString());
          hexDump.dump(contents);
          for (int i = 0; i < hexDump.getHex().size(); ++i) {
            logger.debug(String.format("%-48s %s", hexDump.getHex().get(i), hexDump.getChr().get(i)));
          }
          attachment.read();
        } else {
          stop();
        }
      }

      public void failed(Throwable ex, PassThruSocket attachment) {
        logger.debug("read: " + ex.getMessage());
        stop();
      }
    };
  private CompletionHandler writeHandler =
    new CompletionHandler<Integer, PassThruSocket>() {
      public void completed(Integer count, PassThruSocket attachment) {
        attachment.write();
      }

      public void failed(Throwable ex, PassThruSocket attachment) {
        logger.debug("write: " + ex.getMessage());
        stop();
      }
    };
  private HexDump hexDump = new HexDump();

  public PassThruClient(AsynchronousChannelGroup group, AsynchronousSocketChannel clientSocket, String passThruAddress, int passThruPort) {
    try {
      client = new PassThruSocket(clientSocket, readHandler, writeHandler);
      server = new PassThruSocket(group, connectHandler, readHandler, writeHandler);
      server.connect(new InetSocketAddress(passThruAddress, passThruPort));
      clients.add(this);
    } catch (IOException ex) {
      logger.debug(ex.getMessage());
    }
  } 

  public void stop() {
    clients.remove(this);
    client.stop();
    server.stop();
  }

  public static void shutdown() {
    PassThruClient[] remaining = clients.toArray(new PassThruClient[0]);
    for (PassThruClient c : remaining) {
      c.stop();
    }
  }
}
