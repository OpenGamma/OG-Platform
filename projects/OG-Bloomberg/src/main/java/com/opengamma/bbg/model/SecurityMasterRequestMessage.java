/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleFudgeBuilder;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdFudgeBuilder;
import com.opengamma.util.ArgumentChecker;

/**
 * Message sent to access the security master.
 */
public class SecurityMasterRequestMessage implements Serializable {
  /** Fudge field key. */
  public static final String SECURITY_KEY_FIELD_NAME = "securityKey";
  /** Fudge field key. */
  public static final String MESSAGE_TYPE_FIELD_NAME = "messageType";
  /** Fudge field key. */
  public static final String SECURITY_IDENTITY_FIELD_NAME = "securityIdentity";
  /** Fudge field key. */
  public static final String BOND_ISSUER_TYPE_FIELD_NAME = "bondIssuerType";
  
  private MessageType _messageType;
  private ExternalIdBundle _secKey;
  private UniqueId _uid;
  private String _bondIssuerType;

  /**
   * @return the messageType
   */
  public MessageType getMessageType() {
    return _messageType;
  }

  /**
   * @param messageType the messageType to set
   */
  public void setMessageType(MessageType messageType) {
    _messageType = messageType;
  }

  /**
   * @return the secKey
   */
  public ExternalIdBundle getSecKey() {
    return _secKey;
  }

  /**
   * @param secKey the secKey to set
   */
  public void setSecKey(ExternalIdBundle secKey) {
    _secKey = secKey;
  }
  
  /**
   * Gets the unique identifier.
   * @return the unique identifier
   */
  public UniqueId getUniqueId() {
    return _uid;
  }

  /**
   * Sets the unique identifier.
   * @param uid  the unique identifier
   */
  public void setUniqueId(UniqueId uid) {
    _uid = uid;
  }
  
  /**
   * Gets the bond issuer type
   * @return the bond issuer type
   */
  public String getBondIssuerType() {
    return _bondIssuerType;
  }
  
  /** 
   * Sets the bond issuer type
   * @param bondIssuerType the bond issuer type
   */
  public void setBondIssuerType(String bondIssuerType) {
    _bondIssuerType = bondIssuerType;
  }

  public FudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    ArgumentChecker.notNull(serializer, "FudgeSerializer");
    if (getMessageType() == null) {
      return null; 
    }
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(MESSAGE_TYPE_FIELD_NAME, getMessageType().name());
    switch(getMessageType()) {
      case GET_SECURITIES_BY_KEY:
      case GET_SECURITY_BY_KEY:
        if (_secKey == null) {
          throw new IllegalStateException("Security Key cannot be null for get_security_by_key message");
        }
        msg.add(SECURITY_KEY_FIELD_NAME, ExternalIdBundleFudgeBuilder.toFudgeMsg(serializer, _secKey));
        break;
      case GET_OPTION_CHAIN:
      case GET_SECURITY_BY_IDENTITY:
        if (_uid == null) {
          throw new IllegalStateException("Identity key cannot be null for get_security_by_identity message");
        }
        FudgeMsg identityKeyMsg = UniqueIdFudgeBuilder.toFudgeMsg(serializer, _uid);
        msg.add(SECURITY_IDENTITY_FIELD_NAME, identityKeyMsg);
        break;
      case GET_SECURITIES_BY_BOND_ISSUER_TYPE:
        if (_bondIssuerType == null) {
          throw new IllegalStateException("Bond issuer type cannot be null for get_securities_by_bond_issuer_type message");
        }
        msg.add(BOND_ISSUER_TYPE_FIELD_NAME, _bondIssuerType);
        break;
      default:
        throw new OpenGammaRuntimeException("Unknow SecurityMasterRequest message type");
    }
    return msg;
  }

  public static SecurityMasterRequestMessage fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    SecurityMasterRequestMessage request = new SecurityMasterRequestMessage();
    String msgTypeStr = (String) msg.getByName(MESSAGE_TYPE_FIELD_NAME).getValue();
    MessageType msgType = MessageType.valueOf(msgTypeStr);
    request.setMessageType(msgType);
    switch(msgType) {
      case GET_SECURITIES_BY_KEY:
      case GET_SECURITY_BY_KEY:
      case GET_OPTION_CHAIN:
        request.setSecKey(decodeSecurityKeyFromFudgeMsg(deserializer, msg));
        break;
      case GET_SECURITY_BY_IDENTITY:
        FudgeMsg identityKeyMsg = msg.getMessage(SECURITY_IDENTITY_FIELD_NAME);
        UniqueId uid = UniqueIdFudgeBuilder.fromFudgeMsg(deserializer, identityKeyMsg);
        request.setUniqueId(uid);
        break;
      case GET_SECURITIES_BY_BOND_ISSUER_TYPE:
        String bondIssuerType = msg.getString(BOND_ISSUER_TYPE_FIELD_NAME);
        request.setBondIssuerType(bondIssuerType);
        break;
    }
    return request;
  }

  /**
   * Decodes an identifier bundle from the message.
   * 
   * @param msg  the message to decode, not null
   * @return the bundle, not null
   */
  private static ExternalIdBundle decodeSecurityKeyFromFudgeMsg(final FudgeDeserializer deserializer, FudgeMsg msg) {
    ArgumentChecker.notNull(msg, "FudgeMsg");
    FudgeMsg secKeyMsg = (FudgeMsg) msg.getByName(SECURITY_KEY_FIELD_NAME).getValue();
    return ExternalIdBundleFudgeBuilder.fromFudgeMsg(deserializer, secKeyMsg);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  //-------------------------------------------------------------------------
  /**
   * Type of message.
   */
  public static enum MessageType {
    /** Option chain message. */
    GET_OPTION_CHAIN,
    /** Get securities by key. */
    GET_SECURITIES_BY_KEY,
    /** Get security by key. */
    GET_SECURITY_BY_KEY,
    /** Get securities by bond issuer type. */
    GET_SECURITIES_BY_BOND_ISSUER_TYPE,
    /** Get security by identifier. */
    GET_SECURITY_BY_IDENTITY;
  }

}
