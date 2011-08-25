package com.opengamma.web.server.push.subscription;

/**
 *
 */
public interface TimeoutListener {

  void timeout(String clientId);
}
