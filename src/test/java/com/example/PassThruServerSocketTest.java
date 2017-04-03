package com.example;

import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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
 * Unit test for PassThruServerSocket
 */
public class PassThruServerSocketTest extends TestCase {
  private static final int Port = 5000;
  private ExecutorService service;
  private int connectionCount;
  private CountDownLatch failedTriggered;
  private boolean caughtFailed;

  public PassThruServerSocketTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(PassThruServerSocketTest.class);
  }

  @Override
  protected void setUp() {
    service = Executors.newSingleThreadExecutor();
  }

  @Override
  protected void tearDown() {
    service.shutdown();
  }

  public void testPassThruServerSocketClose() {
    connectionCount = 0;
    failedTriggered = new CountDownLatch(1);
    caughtFailed = false;
    CompletionHandler handler =
      new CompletionHandler<AsynchronousSocketChannel, PassThruServerSocket>() {
        public void completed(AsynchronousSocketChannel ch, PassThruServerSocket attachment) {
          try {
            ++connectionCount;
            ch.close();
          } catch (IOException ex) {
            System.out.println(ex.getMessage());
          }
          attachment.accept();
        }

        public void failed(Throwable ex, PassThruServerSocket attachment) {
          caughtFailed = true;
          failedTriggered.countDown();
        }
      };
    PassThruServerSocket serverSocket = new PassThruServerSocket(handler);
    try {
      serverSocket.start(AsynchronousChannelGroup.withThreadPool(service), Port);
      serverSocket.accept();
      serverSocket.stop();
      failedTriggered.await();
      assertEquals(0, connectionCount);
      assertTrue(caughtFailed);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    } catch (InterruptedException ex) {
      System.out.println(ex.getMessage());
    }
  }

  public void testPassThruServerSocket() {
    connectionCount = 0;
    failedTriggered = new CountDownLatch(1);
    caughtFailed = false;
    CompletionHandler handler = 
      new CompletionHandler<AsynchronousSocketChannel, PassThruServerSocket>() {
        public void completed(AsynchronousSocketChannel ch, PassThruServerSocket attachment) {
          try {
            ++connectionCount;
            ch.close();
          } catch (IOException ex) {
            System.out.println(ex.getMessage());
          }
          attachment.accept();
        }

        public void failed(Throwable ex, PassThruServerSocket attachment) {
          caughtFailed = true;
          failedTriggered.countDown();
        }
      };
    PassThruServerSocket serverSocket = new PassThruServerSocket(handler);
    try {
      serverSocket.start(AsynchronousChannelGroup.withThreadPool(service), Port);
      serverSocket.accept();
      Socket socket = new Socket(InetAddress.getLocalHost(), Port);
      InputStream is = socket.getInputStream();
      assertEquals(-1, is.read());
      is.close();
      socket.close();
      socket = new Socket(InetAddress.getLocalHost(), Port);
      is = socket.getInputStream();
      assertEquals(-1, is.read());
      is.close();
      socket.close();
      serverSocket.stop();
      failedTriggered.await();
      assertEquals(2, connectionCount);
      assertTrue(caughtFailed);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    } catch (InterruptedException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
