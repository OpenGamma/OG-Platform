/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.server;

import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataResult;
import com.opengamma.bbg.model.ReferenceDataRequestMessage;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Reference data request receiver.
 */
public class ReferenceDataProviderRequestReceiver implements FudgeRequestReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(ReferenceDataProviderRequestReceiver.class);
  private ReferenceDataProvider _underlying;
  
  /**
   * @param underlying  the reference data provider, not null
   */
  public ReferenceDataProviderRequestReceiver(ReferenceDataProvider underlying) {
    ArgumentChecker.notNull(underlying, "Reference Data Provider");
    _underlying = underlying;
  }

  @Override
  public FudgeMsg requestReceived(final FudgeDeserializer deserializer, final FudgeMsgEnvelope requestEnvelope) {
    ArgumentChecker.notNull(deserializer, "FudgeContext");
    ArgumentChecker.notNull(requestEnvelope, "FudgeMessageEnvelope");
    FudgeMsg requestFudgeMsg = requestEnvelope.getMessage();
    if (requestFudgeMsg == null) {
      throw new OpenGammaRuntimeException("Request fudgeMsg cannot be null");
    }
    ReferenceDataRequestMessage refDataRequest = ReferenceDataRequestMessage.fromFudgeMsg(deserializer, requestFudgeMsg);
    if (refDataRequest == null) {
      throw new OpenGammaRuntimeException("reference data request message from fudgeMsg cannot be null");
    }
    s_logger.debug("Received reference data request for {} ", refDataRequest.getSecurities());
    Set<String> securities = refDataRequest.getSecurities();
    Set<String> fields = refDataRequest.getFields();
    ReferenceDataResult refDataResult = _underlying.getFields(securities, fields);
    return refDataResult.toFudgeMsg(deserializer.getFudgeContext());
  }

  /**
   * @return the underlying
   */
  public ReferenceDataProvider getUnderlying() {
    return _underlying;
  }

}
