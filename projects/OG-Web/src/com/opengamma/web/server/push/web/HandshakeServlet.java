/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Sets up a connection for a client.  A connection corresponds to one view, e.g. a single browser tab or
 * window.  A user can have multiple simultaneous client connections.  The client ID that identifies the
 * connection is sent in the response.  This ID must be passed to all other operations for this client.
 */
public class HandshakeServlet extends SpringConfiguredServlet {

  @Autowired
  private LongPollingConnectionManager _connectionManager;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String userId = req.getRemoteUser(); // TODO is this right?
    String clientId = _connectionManager.handshake(userId);
    resp.getWriter().write(clientId);
  }
}
