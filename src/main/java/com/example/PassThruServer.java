package com.example;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PassThruServer
 */
public class PassThruServer {
  private static final Logger logger = LoggerFactory.getLogger(PassThruServer.class);
  private long listenPort;
  private String passthruAddress;
  private long passthruPort;
  private long delay = 0;
  private ExecutorService service;
  private PassThruServerSocket serverSocket;

  public PassThruServer(long listenPort, String passthruAddress, long passthruPort, long delay) {
    this.listenPort = listenPort;
    this.passthruAddress = passthruAddress;
    this.passthruPort = passthruPort;
    this.delay = delay;
  }

  public void start() {
    service = Executors.newSingleThreadExecutor();
    try {
      CompletionHandler handler =
        new CompletionHandler<AsynchronousSocketChannel, PassThruServerSocket>() {
          public void completed(AsynchronousSocketChannel ch, PassThruServerSocket attachment) {
            logger.debug("new connection");
            try {
              new PassThruClient(AsynchronousChannelGroup.withThreadPool(service), ch, passthruAddress, (int)passthruPort);
            } catch (IOException ex) {
              logger.debug(ex.getMessage());
            }
            attachment.accept();
          }

          public void failed(Throwable ex, PassThruServerSocket attachment) {
            logger.debug(ex.getMessage());
          }
        };
      serverSocket = new PassThruServerSocket(handler);
      serverSocket.start(AsynchronousChannelGroup.withThreadPool(service), (int)listenPort);
      serverSocket.accept();
    } catch (IOException ex) {
      logger.debug(ex.getMessage());
    }
  }

  public void stop() {
    service.shutdown();
    try {
      if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
        service.shutdownNow();
        if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
        }
      }
    } catch (InterruptedException ex) {
      service.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  public void stopClients() {
    service.submit(new Runnable() {
      public void run() {
        logger.debug("stopClients...");
        PassThruClient.shutdown();
      }
    });
  }
}

