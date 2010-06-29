/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A position that can be managed.
 */
public final class ManagedPosition {

  /**
   * The portfolio unique identifier.
   */
  private UniqueIdentifier _portfolioUid;
  /**
   * The parent node unique identifier.
   */
  private UniqueIdentifier _parentNodeUid;
  /**
   * The unique identifier.
   */
  private UniqueIdentifier _uid;
  /**
   * The amount of the position.
   */
  private BigDecimal _quantity;
  /**
   * The identifiers specifying the security.
   */
  private IdentifierBundle _securityKey;

  /**
   * Creates an instance.
   */
  public ManagedPosition() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio unique identifier of the position.
   * @return the portfolio unique identifier
   */
  public UniqueIdentifier getPortfolioUid() {
    return _portfolioUid;
  }

  /**
   * Sets the portfolio unique identifier of the position.
   * @param uid  the portfolio unique identifier
   */
  public void setPortfolioUid(UniqueIdentifier uid) {
    _portfolioUid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent node unique identifier of the position.
   * @return the parent node unique identifier
   */
  public UniqueIdentifier getParentNodeUid() {
    return _parentNodeUid;
  }

  /**
   * Sets the parent node unique identifier of the position.
   * @param uid  the parent node unique identifier
   */
  public void setParentNodeUid(UniqueIdentifier uid) {
    _parentNodeUid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the position.
   * @return the unique identifier
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  /**
   * Sets the unique identifier of the position.
   * @param uid  the unique identifier
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    _uid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the amount of the position held in terms of the security.
   * @return the amount of the position
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the amount of the position held in terms of the security.
   * @param quantity  the amount of the position
   */
  public void setQuantity(BigDecimal quantity) {
    _quantity = quantity;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a key to the security being held.
   * <p>
   * This allows the security to be referenced without actually loading the security itself.
   * @return the security key
   */
  public IdentifierBundle getSecurityKey() {
    return _securityKey;
  }

  /**
   * Sets the key to the security being held.
   * @param securityKey  the security key
   */
  public void setSecurityKey(IdentifierBundle securityKey) {
    _securityKey = securityKey;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String UID_FIELD_NAME = "uid";
  /** Field name. */
  private static final String PORTFOLIO_UID_FIELD_NAME = "portfolioUid";
  /** Field name. */
  private static final String PARENT_NODE_UID_FIELD_NAME = "parentNodeUid";
  /** Field name. */
  private static final String QUANTITY_FIELD_NAME = "quantity";
  /** Field name. */
  private static final String SECURITY_KEY_FIELD_NAME = "securityKey";

  /**
   * Serializes to a Fudge message.
   * @param context  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    MutableFudgeFieldContainer msg = context.newMessage();
    if (_uid != null) {
      msg.add(UID_FIELD_NAME, _uid.toFudgeMsg(context));
    }
    if (_uid != null) {
      msg.add(PORTFOLIO_UID_FIELD_NAME, _portfolioUid.toFudgeMsg(context));
    }
    if (_uid != null) {
      msg.add(PARENT_NODE_UID_FIELD_NAME, _parentNodeUid.toFudgeMsg(context));
    }
    if (_quantity != null) {
      msg.add(QUANTITY_FIELD_NAME, _quantity);
    }
    if (_securityKey != null) {
      msg.add(SECURITY_KEY_FIELD_NAME, _securityKey.toFudgeMsg(context));
    }
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static ManagedPosition fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    ManagedPosition mp = new ManagedPosition();
    if (msg.hasField(UID_FIELD_NAME)) {
      mp.setUniqueIdentifier(UniqueIdentifier.fromFudgeMsg(msg.getMessage(UID_FIELD_NAME)));
    }
    if (msg.hasField(PORTFOLIO_UID_FIELD_NAME)) {
      mp.setPortfolioUid(UniqueIdentifier.fromFudgeMsg(msg.getMessage(PORTFOLIO_UID_FIELD_NAME)));
    }
    if (msg.hasField(PARENT_NODE_UID_FIELD_NAME)) {
      mp.setParentNodeUid(UniqueIdentifier.fromFudgeMsg(msg.getMessage(PARENT_NODE_UID_FIELD_NAME)));
    }
    if (msg.hasField(QUANTITY_FIELD_NAME)) {
      mp.setQuantity(msg.getValue(BigDecimal.class, QUANTITY_FIELD_NAME));
    }
    if (msg.hasField(SECURITY_KEY_FIELD_NAME)) {
      mp.setSecurityKey(IdentifierBundle.fromFudgeMsg(msg.getMessage(SECURITY_KEY_FIELD_NAME)));
    }
    return mp;
  }

}
