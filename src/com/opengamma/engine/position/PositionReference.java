/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.Security;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A loose specification for a {@link Position} in a particular {@link Security},
 * identified by a unique identifier.
 */
public class PositionReference implements Serializable, Cloneable {

  /**
   * Fudge message key for the quantity.
   */
  public static final String QUANTITY_FIELD_NAME = "quantity";
  /**
   * Fudge message key for the identifier.
   */
  public static final String SECURITY_IDENTITY_KEY_FIELD_NAME = "securityIdentityKey";

  /**
   * The quantity of the security.
   */
  private final BigDecimal _quantity;
  /**
   * The identifier of the security.
   */
  private final UniqueIdentifier _securityIdentityKey;

  public PositionReference(BigDecimal quantity, UniqueIdentifier securityIdentityKey) {
    ArgumentChecker.notNull(quantity, "Quantity");
    ArgumentChecker.notNull(securityIdentityKey, "Security IdentityKey");
    _quantity = quantity;
    _securityIdentityKey = securityIdentityKey;
  }
  
  public PositionReference(Position position) {
    ArgumentChecker.notNull(position, "Position");
    ArgumentChecker.notNull(position.getSecurity(), "Position's Security");
    ArgumentChecker.notNull(position.getSecurity().getUniqueIdentifier(), "Position's Security's UniqueIdentifier");
    _quantity = position.getQuantity();
    _securityIdentityKey = position.getSecurity().getUniqueIdentifier();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the quantity of the security.
   * @return the quantity
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Gets the identifier.
   * @return the identifier
   */
  public UniqueIdentifier getSecurityIdentityKey() {
    return _securityIdentityKey;
  }

  //-------------------------------------------------------------------------
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_quantity == null) ? 0 : _quantity.hashCode());
    result = prime
        * result
        + ((_securityIdentityKey == null) ? 0 : _securityIdentityKey.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof PositionReference)) {
      return false;
    }
    PositionReference other = (PositionReference) obj;
    if(getQuantity().compareTo(other.getQuantity()) != 0) {
      return false;
    }
    if(!ObjectUtils.equals(getSecurityIdentityKey(), other.getSecurityIdentityKey())) {
      return false;
    }
    return true;
  }

  @Override
  public PositionReference clone() {
    try {
      return (PositionReference) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new OpenGammaRuntimeException("yes, it is supported.");
    }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  //-------------------------------------------------------------------------
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory fudgeMessageFactory) {
    MutableFudgeFieldContainer msg = fudgeMessageFactory.newMessage();
    msg.add(QUANTITY_FIELD_NAME, getQuantity().toString());
    FudgeFieldContainer identifier = _securityIdentityKey.toFudgeMsg(fudgeMessageFactory); 
    msg.add(SECURITY_IDENTITY_KEY_FIELD_NAME, identifier);
    return msg;
  }

  public static PositionReference fromFudgeMsg(FudgeMsgEnvelope envelope) {
    FudgeFieldContainer msg = envelope.getMessage();
    BigDecimal quantity = new BigDecimal(msg.getString(QUANTITY_FIELD_NAME));
    FudgeFieldContainer identifierMsg = msg.getMessage(SECURITY_IDENTITY_KEY_FIELD_NAME);
    UniqueIdentifier identifier = UniqueIdentifier.fromFudgeMsg(identifierMsg);
    return new PositionReference(quantity, identifier);
  }

}
