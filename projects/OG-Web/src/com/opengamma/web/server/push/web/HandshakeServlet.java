/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class HandshakeServlet extends HttpServlet {

  private LongPollingConnectionManager _connectionManager;

  // TODO isn't there a spring base class that does this so I can just annotate the field with @Injet, @Resource or something?
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
    _connectionManager = (LongPollingConnectionManager) ctx.getBean("longPollingConnectionManager");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String userId = req.getRemoteUser(); // TODO is this right?
    String clientId = _connectionManager.handshake(userId);
    resp.getWriter().write(clientId);
  }
}
