/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

/**
 * TODO does this need two different methods?
 * TODO impls of this interface need manage queues of updates. BUT - new listener instance for each new long polling connection?
 * TODO probably neater to hide that inside the listener impl and have one per client connection
 * TODO which means the servlet needs to know about the listeners? the manager? a new request will involve a new connection / listener
 * TODO but do we want that in the interface? or leave it open to impls where the connection is persistent? (web)sockets?
 */
public interface SubscriptionListener {

  void itemUpdated(SubscriptionEvent event);
}
