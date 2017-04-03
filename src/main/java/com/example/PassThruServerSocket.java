package com.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * PassThruServerSocket
 */
public class PassThruServerSocket {
  private AsynchronousServerSocketChannel listener;
  private CompletionHandler<AsynchronousSocketChannel, PassThruServerSocket> handler;

  public PassThruServerSocket(CompletionHandler<AsynchronousSocketChannel, PassThruServerSocket> handler) {
    this.handler = handler;
  }

  public void start(AsynchronousChannelGroup group, int port) throws IOException {
    listener = AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress(port));
  }

  public void accept() {
    listener.accept(this, handler);
  }

  public void stop() throws IOException {
    listener.close();
  }
}
