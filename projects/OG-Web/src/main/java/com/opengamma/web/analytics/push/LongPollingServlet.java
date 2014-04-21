/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opengamma.util.auth.AuthUtils;

/**
 * Manages long-polling http requests using Jetty continuations.  Requests to this servlet block until there
 * is new data available for the client or until the connection times out.  The URL is assumed to be
 * {@code <servlet path>/{clientId}}.
 */
public class LongPollingServlet extends HttpServlet {

  private static final Logger s_logger = LoggerFactory.getLogger(LongPollingServlet.class);

  /** Key for storing the results as an attribute of the continuation. */
  /* package */ static final String RESULTS = "RESULTS";
  /** Name of the HTTP query parameter for the client ID */
  public static final String CLIENT_ID = "clientId";

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /** Request parameter name. */
  private static final String METHOD = "method";
  /** Request parameter value. */
  private static final String GET = "GET";

  /**
   * Manages connections for each client.
   */
  @Autowired
  private LongPollingConnectionManager _connectionManager;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    _connectionManager = WebPushServletContextUtils.getLongPollingConnectionManager(config.getServletContext());
  }

  //-------------------------------------------------------------------------
  // this is a hack to get round a problem with browsers caching GET requests even when they're told not to
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String method = req.getParameter(METHOD);
    if (GET.equals(method)) {
      doGet(req, resp);
    } else {
      resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Continuation continuation = ContinuationSupport.getContinuation(request);
    if (continuation.isExpired()) {
      // timeout - just send a blank response and tell the connection that its continuation has timed out
      String clientId = (String) continuation.getAttribute(CLIENT_ID);
      if (clientId != null) {
        // TODO will this always get the correct continuation?
        _connectionManager.longPollHttpTimeout(clientId, continuation);
      }
      return;
    }
    String results = (String) request.getAttribute(RESULTS);
    // if this is the first time the request has been dispatched the results will be null. if the request has been
    // dispatched before and is being dispatched again after its continuation was resumed the results will be populated
    if (results == null) {
      setUpConnection(continuation, request, response);
    } else {
      // Send the results
      s_logger.debug("Writing results to HTTP response {}", results);
      response.getWriter().write(results);
    }
  }

  private void setUpConnection(Continuation continuation, HttpServletRequest request, HttpServletResponse response) throws IOException {
    // suspend the request
    continuation.suspend(); // always suspend before registration
    String userName = (AuthUtils.isPermissive() ? null : AuthUtils.getUserName());
    // get the client ID from the URL and pass the continuation to the connection manager for the next updates
    String clientId = getClientId(request);
    boolean connected = (clientId != null) && _connectionManager.longPollHttpConnect(userName, clientId, continuation);
    if (!connected) {
      // couldn't get the client ID from the URL or the client ID didn't correspond to a known client
      // TODO how do I send something other than jetty's standard HTML error page?
      response.sendError(404, "Problem accessing " + request.getRequestURI() + ".  Reason: Unknown client ID " + clientId);
      continuation.complete();
    }
    continuation.setAttribute(CLIENT_ID, clientId);
  }

  /**
   * Extracts the client ID from the URL.  If the URL is {@code http://<host>/<servlet path>/12345} the client ID is 12345.
   * @param request The request
   * @return The client ID from {@code request}'s URL or null if it's missing
   */
  /* package */ static String getClientId(HttpServletRequest request) {
    // this is the portion of the URL after the part used to direct it to this servlet
    // i.e. if the full URL is http://host/subscription/abcd then suffix=/abcd
    String suffix = request.getRequestURI().substring(request.getServletPath().length());
    String clientId;
    if (suffix.length() > 1) {
      clientId = suffix.substring(1);
    } else {
      clientId = null;
    }
    return clientId;
  }
}
