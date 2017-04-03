package com.example;

import java.util.Timer;
import java.util.TimerTask;

/**
 * StopClientsTimer
 */
public class StopClientsTimer extends TimerTask {
  private PassThruServer passThruServer;
  private Timer timer;

  public StopClientsTimer(PassThruServer passThruServer) {
    this.passThruServer = passThruServer;
  }

  public void start() {
    timer = new Timer();
    timer.scheduleAtFixedRate(this, 60000, 60000);
  }

  public void stop() {
    timer.cancel();
  }

  public void run() {
    passThruServer.stopClients();
  }
}
