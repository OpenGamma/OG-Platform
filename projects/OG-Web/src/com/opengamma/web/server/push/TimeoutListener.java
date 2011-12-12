package com.opengamma.web.server.push;

/**
 * Listens for notifications that a client connection has been idle for too long and has timed out.
 * TODO redundant
 */
/* package */ interface TimeoutListener {

  /**
   * Invoked after the client connection has been idle too long and has timed out.
   * @param clientId The ID of the connection
   */
  void timeout(String clientId);
}
