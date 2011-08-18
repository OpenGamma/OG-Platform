/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.web.server.push.subscription.RestUpdateManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Receives the REST URLs for the objects which the client is interested in seeing updates for and sets up 
 * subscriptions.  Updates are pushed to the client over the long-polling connection set up
 * by {@link LongPollingServlet}.  This servlet requires the URL to be {@code <servlet path>/{clientId}}
 * and returns 404 if the client ID isn't in the URL or doesn't correspond to an existing client connection.
 * @see LongPollingServlet
 * @see HandshakeServlet
 * TODO this only applies to viewports now, FIX
 */
public class SubscriptionsServlet extends SpringConfiguredServlet {

  @Autowired
  private RestUpdateManager _restUpdateManager;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String userId = request.getRemoteUser(); // TODO is this right?
    String clientId = LongPollingServlet.getClientId(request);
    // if the client ID can't be found it's an error, nothing else we can do
    if (clientId == null) {
      response.sendError(404);
      return;
    }
    // get URLs the client is interested in and subscribe to them
    BufferedReader reader = request.getReader();
    String url;
    List<String> urls = new ArrayList<String>();
    while ((url = reader.readLine()) != null) {
      urls.add(url);
    }
    // TODO sort this
    /*if (!_restUpdateManager.subscribe(userId, clientId, urls)) {
      response.sendError(404);
    }*/
  }
}
