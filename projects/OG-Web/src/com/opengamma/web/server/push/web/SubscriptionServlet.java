/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * this manages long-polling http requests
 * TODO use JAX-RS instead? /subscriptions/{clientId} instead of query param?
 */
public class SubscriptionServlet extends HttpServlet {

  public static final String RESULTS = "RESULTS";

  private LongPollingConnectionManager _connectionManager;

  // TODO isn't there a spring base class that does this so I can just annotate the field with @Injet, @Resource or something?
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
    _connectionManager = (LongPollingConnectionManager) ctx.getBean("longPollingConnectionManager");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // if we need to get asynchronous results
    Object results = request.getAttribute("results");
    if (results == null) {
      final Continuation continuation = ContinuationSupport.getContinuation(request);

      if (continuation.isExpired()) {
        response.getWriter().write("TIMEOUT"); // TODO do this properly
        return;
      }
      // suspend the request
      continuation.suspend(); // always suspend before registration
      String userId = request.getRemoteUser(); // TODO is this right?
      String clientId = request.getParameter("clientId");
      if (clientId == null) {
        response.sendError(404);
      } else {
        _connectionManager.connectionEstablished(userId, clientId, continuation);
      }
    } else {
      // Send the results
      //sendMyResultResponse(response, results);
    }
  }
}
