/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.LiveDataValueUpdateBeanFudgeBuilder;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Listens to Fudge updates containing {@link CogdaLiveDataUpdateBean} instances and
 * dispatches them to a {@link CogdaLiveDataServer}.
 */
public class CogdaLiveDataServerUpdateListener implements FudgeMessageReceiver {
  private final CogdaLiveDataServer _liveDataServer;
  
  public CogdaLiveDataServerUpdateListener(CogdaLiveDataServer liveDataServer) {
    ArgumentChecker.notNull(liveDataServer, "liveDataServer");
    _liveDataServer = liveDataServer;
  }

  /**
   * Gets the liveDataServer.
   * @return the liveDataServer
   */
  public CogdaLiveDataServer getLiveDataServer() {
    return _liveDataServer;
  }

  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    // TODO kirk 2012-08-13 -- Check schema version.
    FudgeMsg msg = msgEnvelope.getMessage();
    LiveDataValueUpdateBean updateBean = LiveDataValueUpdateBeanFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(fudgeContext), msg);
    getLiveDataServer().liveDataReceived(updateBean);
  }

}
