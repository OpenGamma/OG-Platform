/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;

/**
 * 
 *
 * @author pietari
 */
public abstract class AbstractEventDispatcher extends TerminatableJob {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractEventDispatcher.class);
  
  private final long MAX_WAIT_MILLISECONDS = 1000;
  
  private AbstractLiveDataServer _server;
  private volatile boolean doDisconnect = false;
  
  public AbstractEventDispatcher(AbstractLiveDataServer server) {
    ArgumentChecker.checkNotNull(server, "Live Data Server");
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
    
    // do disconnect outside event dispatch so everything works cleanly
    if (doDisconnect) {
      _server.disconnect();
      doDisconnect = false;
    }
  }
  
  protected void disconnected() {
    s_logger.error("Lost connection to market data server.");
    doDisconnect = true;
  }
  
  protected abstract void dispatch(long maxWaitMilliseconds);

}
