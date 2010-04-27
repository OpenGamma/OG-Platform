/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;

/**
 * 
 *
 * @author pietari
 */
public abstract class AbstractEventDispatcher extends TerminatableJob {
  
  private final long MAX_WAIT_MILLISECONDS = 1000;
  
  private AbstractLiveDataServer _server;
  
  public AbstractEventDispatcher(AbstractLiveDataServer server) {
    ArgumentChecker.notNull(server, "Live Data Server");
    _server = server;
  }
  
  /**
   * @return the server
   */
  public AbstractLiveDataServer getServer() {
    return _server;
  }

  @Override
  protected void runOneCycle() {
    dispatch(MAX_WAIT_MILLISECONDS);
  }
  
  protected void disconnected() {
    _server.setConnectionStatus(AbstractLiveDataServer.ConnectionStatus.NOT_CONNECTED);
    terminate();
  }
  
  protected abstract void dispatch(long maxWaitMilliseconds);

}
