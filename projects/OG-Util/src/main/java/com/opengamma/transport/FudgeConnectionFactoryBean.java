/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.concurrent.ExecutorService;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.transport.socket.SocketEndPointDescriptionProvider;
import com.opengamma.transport.socket.SocketFudgeConnection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a {@link FudgeConnection} based on an end-point description. Typically an end-point is determined by a REST query to the host.
 */
public class FudgeConnectionFactoryBean extends SingletonFactoryBean<FudgeConnection> {

  private FudgeContext _fudgeContext;
  private ExecutorService _executorService;
  private EndPointDescriptionProvider _endPointDescriptionProvider;

  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setExecutorService(final ExecutorService executorService) {
    _executorService = executorService;
  }

  public ExecutorService getExecutorService() {
    return _executorService;
  }

  public void setEndPointDescription(final EndPointDescriptionProvider endPoint) {
    _endPointDescriptionProvider = endPoint;
  }

  public EndPointDescriptionProvider getEndPointDescription() {
    return _endPointDescriptionProvider;
  }

  private FudgeMsg resolveEndPointDescription() {
    if (_endPointDescriptionProvider != null) {
      ArgumentChecker.notNull(getFudgeContext(), "fudgeContext");
      return _endPointDescriptionProvider.getEndPointDescription(getFudgeContext());
    } else {
      return null;
    }
  }

  @Override
  protected FudgeConnection createObject() {
    final FudgeMsg endPoint = resolveEndPointDescription();
    ArgumentChecker.notNull(endPoint, "endPointDescription");
    if (SocketEndPointDescriptionProvider.TYPE_VALUE.equals(endPoint.getString(SocketEndPointDescriptionProvider.TYPE_KEY))) {
      final SocketFudgeConnection connection = (getExecutorService() != null) ? new SocketFudgeConnection(getFudgeContext(), getExecutorService()) : new SocketFudgeConnection(getFudgeContext());
      connection.setServer(endPoint);
      return connection;
    }
    throw new IllegalArgumentException("Don't know how to create end-point " + endPoint);
  }

}
