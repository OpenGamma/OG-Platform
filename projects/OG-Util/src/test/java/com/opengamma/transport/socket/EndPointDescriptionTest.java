/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class EndPointDescriptionTest {

  private static final Logger s_logger = LoggerFactory.getLogger(EndPointDescriptionTest.class);

  private void testEndPoints(boolean bind) throws IOException {
    final AbstractServerSocketProcess server = new AbstractServerSocketProcess() {

      @Override
      protected void socketOpened(Socket socket) {
      }

    };
    if (bind) {
      server.setBindAddress(InetAddress.getLocalHost());
    }
    server.start();
    final FudgeMsg serverEndPoint = server.getEndPointDescription(FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(serverEndPoint);
    s_logger.info("Server end point {}", serverEndPoint);
    final AbstractSocketProcess client = new AbstractSocketProcess() {

      @Override
      protected void socketOpened(Socket socket, BufferedOutputStream os, BufferedInputStream is) {
      }

    };
    client.setInetAddress(InetAddress.getLocalHost());
    client.setPortNumber(server.getPortNumber());
    client.start();
    final FudgeMsg clientEndPoint = client.getEndPointDescription(FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(clientEndPoint);
    s_logger.info("Client end point {} ", clientEndPoint);
    assertEquals(serverEndPoint.getString(SocketEndPointDescriptionProvider.TYPE_KEY), clientEndPoint.getString(SocketEndPointDescriptionProvider.TYPE_KEY));
    assertEquals(serverEndPoint.getInt(SocketEndPointDescriptionProvider.PORT_KEY), clientEndPoint.getInt(SocketEndPointDescriptionProvider.PORT_KEY));
    client.stop();
    server.stop();
  }

  public void testEndPointsBound() throws IOException {
    testEndPoints(true);
  }

  public void testEndPointsUnbound() throws IOException {
    testEndPoints(false);
  }

  public void testConnectToEndPoint() throws IOException {
    final AbstractServerSocketProcess server = new AbstractServerSocketProcess() {

      @Override
      protected void socketOpened(Socket socket) {
      }

    };
    server.start();
    final FudgeMsg serverEndPoint = server.getEndPointDescription(FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(serverEndPoint);
    s_logger.info("Server end point {}", serverEndPoint);
    final AbstractSocketProcess client = new AbstractSocketProcess() {

      @Override
      protected void socketOpened(Socket socket, BufferedOutputStream os, BufferedInputStream is) {
      }

    };
    client.setServer(serverEndPoint);
    client.start();
    final FudgeMsg clientEndPoint = client.getEndPointDescription(FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(clientEndPoint);
    s_logger.info("Client end point {} ", clientEndPoint);
    client.stop();
    server.stop();
  }

  public void testConnectToStaticEndPoint() throws IOException {
    final AbstractServerSocketProcess server = new AbstractServerSocketProcess() {

      @Override
      protected void socketOpened(Socket socket) {
      }

    };
    server.start();
    final SocketEndPointDescriptionProvider serverEndPointDescriptor = new SocketEndPointDescriptionProvider();
    serverEndPointDescriptor.setAddress("localhost");
    serverEndPointDescriptor.setPort(server.getPortNumber());
    final FudgeMsg serverEndPoint = serverEndPointDescriptor.getEndPointDescription(FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(serverEndPoint);
    s_logger.info("Server end point {}", serverEndPoint);
    final AbstractSocketProcess client = new AbstractSocketProcess() {

      @Override
      protected void socketOpened(Socket socket, BufferedOutputStream os, BufferedInputStream is) {
      }

    };
    client.setServer(serverEndPoint);
    client.start();
    final FudgeMsg clientEndPoint = client.getEndPointDescription(FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(clientEndPoint);
    s_logger.info("Client end point {} ", clientEndPoint);
    client.stop();
    server.stop();
  }

}
