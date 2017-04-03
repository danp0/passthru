package com.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for PassThruSocket
 */
public class PassThruSocketTest extends TestCase {
  private static final int Port = 5000;
  private ExecutorService service;
  private CountDownLatch failedTriggered = new CountDownLatch(1);
  private boolean caughtFailed = false;
  private PassThruSocket echoSocket;
  private CountDownLatch connectTriggered = new CountDownLatch(1);
  private CountDownLatch exitTriggered = new CountDownLatch(1);
  private StringBuilder received = new StringBuilder();
  private CompletionHandler acceptHandler =
    new CompletionHandler<AsynchronousSocketChannel, PassThruServerSocket>() {
      public void completed(AsynchronousSocketChannel ch, PassThruServerSocket attachment) {
        echoSocket = new PassThruSocket(ch, echoHandler, writeHandler);
        echoSocket.read(ByteBuffer.allocate(256));
        attachment.accept();
      }

      public void failed(Throwable ex, PassThruServerSocket attachment) {
        caughtFailed = true;
        failedTriggered.countDown();
      }
    };
  private CompletionHandler connectHandler =
    new CompletionHandler<Void, PassThruSocket>() {
      public void completed(Void v, PassThruSocket attachment) {
        connectTriggered.countDown();
        attachment.read(ByteBuffer.allocate(256));
      }

      public void failed(Throwable ex, PassThruSocket attachment) {
        System.out.println("connect failed: " + ex.getMessage());
      }
    };
  private CompletionHandler echoHandler =
    new CompletionHandler<Integer, PassThruSocket>() {
      public void completed(Integer count, PassThruSocket attachment) {
        attachment.getReadBuffer().flip();
        byte[] contents = new byte[attachment.getReadBuffer().remaining()];
        attachment.getReadBuffer().get(contents);
        attachment.write(ByteBuffer.wrap(contents));
        attachment.read();
      }

      public void failed(Throwable ex, PassThruSocket attachment) {
        System.out.println("echo failed: " + ex.getMessage());
      }
    };
  private CompletionHandler readHandler =
    new CompletionHandler<Integer, PassThruSocket>() {
      public void completed(Integer count, PassThruSocket attachment) {
        attachment.getReadBuffer().flip();
        byte[] contents = new byte[attachment.getReadBuffer().remaining()];
        attachment.getReadBuffer().get(contents);
        String message = new String(contents);
        received.append(message);
        attachment.read();
        if (message.endsWith("exit")) {
          exitTriggered.countDown();
        }
      }

      public void failed(Throwable ex, PassThruSocket attachment) {
        System.out.println("read failed: " + ex.getMessage());
      }
    };
  private CompletionHandler writeHandler =
    new CompletionHandler<Integer, PassThruSocket>() {
      public void completed(Integer count, PassThruSocket attachment) {
        attachment.write();
      }

      public void failed(Throwable ex, PassThruSocket attachment) {
        System.out.println("write failed: " + ex.getMessage());
      }
    };

  public PassThruSocketTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(PassThruSocketTest.class);
  }

  @Override
  protected void setUp() {
    service = Executors.newSingleThreadExecutor();
  }

  @Override
  protected void tearDown() {
    service.shutdown();
  }

  public void testPassThruSocket() {
    PassThruServerSocket serverSocket = new PassThruServerSocket(acceptHandler);
    try {
      serverSocket.start(AsynchronousChannelGroup.withThreadPool(service), Port);
      serverSocket.accept();
      PassThruSocket socket = new PassThruSocket(AsynchronousChannelGroup.withThreadPool(service),
          connectHandler,
          readHandler,
          writeHandler);
      socket.connect(new InetSocketAddress("localhost", Port));
      connectTriggered.await();
      service.execute(new Runnable() {
        public void run() {
          for (int i = 0; i < 10; ++i) {
            socket.write(ByteBuffer.wrap(Integer.toString(i).getBytes()));
          }
          socket.write(ByteBuffer.wrap("exit".getBytes()));
        }
      });
      exitTriggered.await();
      serverSocket.stop();
      failedTriggered.await();
      assertTrue(caughtFailed);
      socket.stop();
      echoSocket.stop();
      assertEquals("0123456789exit", received.toString());
    } catch (IOException ex) {
      System.out.println("IOException: " + ex.getMessage());
    } catch (InterruptedException ex) {
      System.out.println("InterruptedException: " + ex.getMessage());
    }
  }
}
