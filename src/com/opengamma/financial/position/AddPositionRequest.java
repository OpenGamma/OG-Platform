/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.math.BigDecimal;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A request to add a position.
 */
public final class AddPositionRequest {

  /**
   * The parent node unique identifier.
   */
  private UniqueIdentifier _parentUid;
  /**
   * The quantity of the position.
   */
  private BigDecimal _quantity;
  /**
   * The security.
   */
  private IdentifierBundle _securityKey;

  /**
   * Creates an instance.
   */
  public AddPositionRequest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent node unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getParentNode() {
    return _parentUid;
  }

  /**
   * Sets the parent node unique identifier.
   * @param parentUid  the unique identifier, not null
   */
  public void setParentNode(UniqueIdentifier parentUid) {
    ArgumentChecker.notNull(parentUid, "UniqueIdentifier");
    _parentUid = parentUid;
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
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    Validate.notNull(getParentNode(), "Parent must not be null");
    Validate.notNull(getQuantity(), "Quantity must not be null");
    Validate.notNull(getSecurityKey(), "Security key must not be null");
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String PARENT_NODE_FIELD_NAME = "parentNode";
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
    if (_parentUid != null) {
      msg.add(PARENT_NODE_FIELD_NAME, _parentUid.toFudgeMsg(context));
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
  public static AddPositionRequest fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    AddPositionRequest req = new AddPositionRequest();
    if (msg.hasField(PARENT_NODE_FIELD_NAME)) {
      req.setParentNode(UniqueIdentifier.fromFudgeMsg(msg.getMessage(PARENT_NODE_FIELD_NAME)));
    }
    if (msg.hasField(QUANTITY_FIELD_NAME)) {
      req.setQuantity(msg.getValue(BigDecimal.class, QUANTITY_FIELD_NAME));
    }
    if (msg.hasField(SECURITY_KEY_FIELD_NAME)) {
      req.setSecurityKey(IdentifierBundle.fromFudgeMsg(msg.getMessage(SECURITY_KEY_FIELD_NAME)));
    }
    return req;
  }

}
