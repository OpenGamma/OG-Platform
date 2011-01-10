/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.transport.socket.AbstractServerSocketProcess;
import com.opengamma.transport.socket.SocketFudgeRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a {@link FudgeRequestSender} based on an end-point description. Typically an end-point is
 * determined by a REST query to the host.
 */
public class FudgeRequestSenderFactoryBean extends SingletonFactoryBean<FudgeRequestSender> {

  private FudgeContext _fudgeContext;
  private EndPointDescriptionProvider _endPointDescriptionProvider;

  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setEndPointDescription(final EndPointDescriptionProvider endPoint) {
    _endPointDescriptionProvider = endPoint;
  }

  public EndPointDescriptionProvider getEndPointDescription() {
    return _endPointDescriptionProvider;
  }

  private FudgeFieldContainer resolveEndPointDescription() {
    if (_endPointDescriptionProvider != null) {
      ArgumentChecker.notNull(getFudgeContext(), "fudgeContext");
      return _endPointDescriptionProvider.getEndPointDescription(getFudgeContext());
    } else {
      return null;
    }
  }

  @Override
  protected FudgeRequestSender createObject() {
    final FudgeFieldContainer endPoint = resolveEndPointDescription();
    ArgumentChecker.notNull(endPoint, "endPointDescription");
    if (AbstractServerSocketProcess.TYPE_VALUE.equals(endPoint.getString(AbstractServerSocketProcess.TYPE_KEY))) {
      final SocketFudgeRequestSender sender = new SocketFudgeRequestSender(getFudgeContext());
      sender.setServer(endPoint);
      return sender;
    }
    throw new IllegalArgumentException("Don't know how to create end-point " + endPoint);
  }

}
