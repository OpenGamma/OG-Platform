/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

/**
 *
 */
@Path("subscriptions")
public class Subscriptions {

  /*@GET
  @PathParam("{clientId}")
  public String longPoll(@Context HttpServletRequest request) {
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
  }*/
}
