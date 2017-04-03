package com.example;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ShutdownChannelGroupException;
import java.nio.channels.WritePendingException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PassThruSocket
 */
public class PassThruSocket {
  public enum Direction {
    S2C,
    C2S
  }
  private static final Logger logger = LoggerFactory.getLogger(PassThruSocket.class);
  private AsynchronousSocketChannel socket;
  private Direction direction;
  private CompletionHandler<Void, PassThruSocket> connectHandler;
  private CompletionHandler<Integer, PassThruSocket> readHandler;
  private CompletionHandler<Integer, PassThruSocket> writeHandler;
  private Queue<ByteBuffer> pending = new LinkedList<ByteBuffer>();
  private ByteBuffer readBuffer;
  private ByteBuffer writeBuffer;

  public PassThruSocket(AsynchronousChannelGroup group, 
      CompletionHandler<Void, PassThruSocket> connectHandler,
      CompletionHandler<Integer, PassThruSocket> readHandler,
      CompletionHandler<Integer, PassThruSocket> writeHandler) throws IOException {
    socket = AsynchronousSocketChannel.open(group);
    direction = Direction.S2C;
    this.connectHandler = connectHandler;
    this.readHandler = readHandler;
    this.writeHandler = writeHandler;
  }

  public PassThruSocket(AsynchronousSocketChannel socket,
      CompletionHandler<Integer, PassThruSocket> readHandler,
      CompletionHandler<Integer, PassThruSocket> writeHandler) {
    this.socket = socket;
    direction = Direction.C2S;
    this.connectHandler = null;
    this.readHandler = readHandler;
    this.writeHandler = writeHandler;
  }

  public void connect(SocketAddress address) {
    socket.connect(address, this, connectHandler);
  }

  public Direction getDirection() {
    return direction;
  }

  public ByteBuffer getReadBuffer() {
    return readBuffer;
  }

  public ByteBuffer getWriteBuffer() {
    return writeBuffer;
  }

  public void read() {
    try {
      readBuffer.clear();
      socket.read(readBuffer, this, readHandler);
    } catch (ShutdownChannelGroupException ex) {
      logger.debug("channel group down: " + ex.getMessage());
    }
  }

  public void read(ByteBuffer buffer) {
    readBuffer = buffer;
    read();
  }

  public void write() {
    try {
      if (writeBuffer != null && writeBuffer.hasRemaining() && writeBuffer.remaining() > 0) {
        writeBuffer.compact();
        socket.write(writeBuffer, this, writeHandler);
      } else if (!pending.isEmpty()) {
        writeBuffer = pending.remove();
        socket.write(writeBuffer, this, writeHandler);
      } else {
        writeBuffer = null;
      }
    } catch (WritePendingException ex) {
      logger.debug("write pending: " + ex.getMessage());
    } catch (NoSuchElementException ex) {
      logger.debug("no such element: " + ex.getMessage());
    } catch (ShutdownChannelGroupException ex) {
      logger.debug("channel group down: " + ex.getMessage());
    }
  }

  public void write(ByteBuffer buffer) {
    pending.add(buffer);
    write();
  }

  public void stop() {
    try {
      socket.close();
    } catch (IOException ex) {
      logger.debug(ex.getMessage());
    }
  }
}

