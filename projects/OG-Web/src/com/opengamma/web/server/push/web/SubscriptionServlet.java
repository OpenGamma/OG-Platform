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
 * This manages long-polling http requests.  It assumes the URL will be {@code <servlet path>/{clientId}}.
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
    String results = (String) request.getAttribute(RESULTS);
    if (results == null) {
      Continuation continuation = ContinuationSupport.getContinuation(request);

      if (continuation.isExpired()) {
        response.getWriter().write("TIMEOUT"); // TODO what is the best approach here?
        return;
      }
      // suspend the request
      continuation.suspend(); // always suspend before registration
      String userId = request.getRemoteUser(); // TODO is this right?
      // this is the portion of the URL after the part used to direct it to this servlet
      // i.e. if the full URL is http://host/subscription/abcd then suffix=/abcd
      String suffix = request.getRequestURI().substring(request.getServletPath().length());
      boolean connected;
      // get the client ID from the URL suffix and pass the continuation to the connection manager for the next updates
      if (suffix.length() > 1) {
        String clientId = suffix.substring(1);
        connected = _connectionManager.connect(userId, clientId, continuation);
      } else {
        connected = false;
      }
      if (!connected) {
        // couldn't get the client ID from the URL
        response.sendError(404);
        continuation.complete();
      }
    } else {
      // Send the results
      response.getWriter().write(results);
    }
  }
}
