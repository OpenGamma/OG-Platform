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
public final class UpdatePositionRequest {

  /**
   * The position uid.
   */
  private UniqueIdentifier _uid;
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
  public UpdatePositionRequest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  /**
   * Sets the position unique identifier.
   * @param uid  the unique identifier, not null
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "UniqueIdentifier");
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
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    Validate.notNull(getUniqueIdentifier(), "UniqueIdentifier must not be null");
    Validate.isTrue(getUniqueIdentifier().isVersioned(), "UniqueIdentifier must be versioned");
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
  private static final String UID_FIELD_NAME = "uid";
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
  public static UpdatePositionRequest fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    UpdatePositionRequest req = new UpdatePositionRequest();
    if (msg.hasField(UID_FIELD_NAME)) {
      req.setUniqueIdentifier(UniqueIdentifier.fromFudgeMsg(msg.getMessage(UID_FIELD_NAME)));
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
