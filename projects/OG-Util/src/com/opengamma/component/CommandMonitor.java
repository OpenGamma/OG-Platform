/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public final class CommandMonitor extends Thread {
  private static final Logger s_logger = LoggerFactory.getLogger(ComponentManager.class);
  private static final String SECRET_PROPERTY = "commandmonitor.secret";
  private static final String PORT_PROPERTY = "commandmonitor.port";
  private static final String DEFAULT_SECRET = "OpenGamma";
  private static final int DEFAULT_PORT = 8079;
  private static final int COMMAND_LENGTH = 256;

  private ComponentRepository _repo;
  private String _secret;
  private int _port;
  private DatagramSocket _socket;

  private CommandMonitor(ComponentRepository repo, String secret, int port) {
    _repo = repo;
    _secret = secret;
    _port = port;

    setDaemon(true);
    setName("CommandMonitor");
    try {
      _socket = new DatagramSocket(port, InetAddress.getByName("127.0.0.1"));
    } catch (Exception e) {
      s_logger.warn("Failed to create listening socket, CommandMonitor will not be available");
      return;
    }

    this.start();
  }

  @Override
  public void run() {
    while (true) {
      byte[] buffer = new byte[COMMAND_LENGTH];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
      try {
        _socket.receive(packet);
      } catch (IOException e) {
        s_logger.warn("Error while receiving command packet");
        continue;
      }
      String received = new String(packet.getData(), 0, packet.getLength());

      if (!received.matches("secret:" + _secret + "\\s+command:\\w+\\s*")) {
        s_logger.debug("Malformed command or wrong secret");
        continue;
      }

      String command = received.replaceAll("secret:" + _secret + "\\s+command:(\\w+)\\s*", "$1");
      s_logger.debug("Received command \"{}\"", command);

      if (command.equals("stop")) {
        /* What do we do if the repo isn't running? */
        if (_repo.isRunning()) {
          _repo.stop();
        }
      } else if (command.equals("exit")) {
        if (_repo.isRunning()) {
          _repo.stop();
        }
        System.exit(0);
      } else {
        s_logger.debug("Unknown command \"{}\"", command);
      }
    }
  }

  public static void create(ComponentRepository repo) {
    String secret = System.getProperty(SECRET_PROPERTY, DEFAULT_SECRET);
    String port = System.getProperty(PORT_PROPERTY, Integer.toString(DEFAULT_PORT));
    new CommandMonitor(repo, secret, Integer.parseInt(port));
  }

  public static void create(ComponentRepository repo, String secret, int port) {
    new CommandMonitor(repo, secret, port);
  }

  public static void main(String[] args) throws IOException { //CSIGNORE
    byte[] buffer = new byte[COMMAND_LENGTH];
    InetAddress address = InetAddress.getByName("127.0.0.1");

    String secret = System.getProperty(SECRET_PROPERTY, DEFAULT_SECRET);
    String port = System.getProperty(PORT_PROPERTY, Integer.toString(DEFAULT_PORT));
    String command = (args.length == 1) ? args[0] : "exit";

    String toSend = "secret:" + secret + " command:" + command + "\n";
    byte[] sBuffer = toSend.getBytes();

    System.out.println("Sending " + toSend);
    try {
      DatagramSocket socket = new DatagramSocket();
      DatagramPacket packet = new DatagramPacket(sBuffer, sBuffer.length, address, Integer.parseInt(port));
      socket.send(packet);
    } catch (Exception e) {
      System.out.println("Send failed...");
    }
  }
}
