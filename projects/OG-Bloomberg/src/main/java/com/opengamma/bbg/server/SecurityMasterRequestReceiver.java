/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.server;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.model.SecurityMasterRequestMessage;
import com.opengamma.bbg.model.SecurityMasterRequestMessage.MessageType;
import com.opengamma.bbg.model.SecurityMasterResponseMessage;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.transport.ByteArrayRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Receiver of security requests.
 */
public class SecurityMasterRequestReceiver implements ByteArrayRequestReceiver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SecurityMasterRequestReceiver.class);

  /**
   * The security source.
   */
  private final SecuritySource _securitySource;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Constructor.
   * 
   * @param secSource the source of securities, not null
   */
  public SecurityMasterRequestReceiver(SecuritySource secSource) {
    this(secSource, new FudgeContext());
  }

  /**
   * Constructor.
   * 
   * @param secSource the source of securities, not null
   * @param fudgeContext the Fudge context, not null
   */
  public SecurityMasterRequestReceiver(SecuritySource secSource, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(secSource, "secSource");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _securitySource = secSource;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying source of securities.
   * 
   * @return the underlying source of securities, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  @Override
  public byte[] requestReceived(byte[] message) {
    FudgeMsgEnvelope requestEnvelope = _fudgeContext.deserialize(message);
    FudgeMsg requestFudgeMsg = requestEnvelope.getMessage();
    SecurityMasterRequestMessage secMasterRequest = SecurityMasterRequestMessage.fromFudgeMsg(new FudgeDeserializer(_fudgeContext), requestFudgeMsg);
    MessageType messageType = secMasterRequest.getMessageType();
    String secDes = secMasterRequest.getUniqueId() != null ? secMasterRequest.getUniqueId().getValue() : secMasterRequest.getSecKey().toString();
    s_logger.debug("Received {} request for {} ", new Object[] {secMasterRequest.getMessageType(), secDes });
    Security sec = null;
    SecurityMasterResponseMessage responseMessage = new SecurityMasterResponseMessage();
    switch (messageType) {
      case GET_SECURITIES_BY_KEY:
        Collection<? extends Security> securities = _securitySource.get(secMasterRequest.getSecKey());
        responseMessage.setSecurities(Collections.unmodifiableCollection(securities));
        break;
      case GET_SECURITY_BY_KEY:
        sec = _securitySource.getSingle(secMasterRequest.getSecKey());
        responseMessage.setSecurity(sec);
        break;
      case GET_SECURITY_BY_IDENTITY:
        sec = _securitySource.get(secMasterRequest.getUniqueId());
        responseMessage.setSecurity(sec);
        break;
      default:
        s_logger.warn("Unsupported SecurityMasterRequest {}", messageType);
        throw new OpenGammaRuntimeException("Unsupported SecurityMasterRequest");
    }
    return toByteArray(responseMessage);
  }

  /**
   * Converts a message to a byte array.
   * 
   * @param message the message to convert, null returns null
   * @return the converted message, null if null input
   */
  private byte[] toByteArray(final SecurityMasterResponseMessage message) {
    if (message == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    _fudgeContext.writeObject(message, baos);
    return baos.toByteArray();
  }

}
