/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.Security;
import com.opengamma.util.ArgumentChecker;

/**
 * A lose specification for a {@link Position} in a particular {@link Security},
 * identified by its Identity Key.
 *
 * @author kirk
 */
public class PositionReference implements Serializable, Cloneable {
  public static final String QUANTITY_FIELD_NAME = "quantity";
  public static final String SECURITY_IDENTITY_KEY_FIELD_NAME = "securityIdentityKey";
  private final BigDecimal _quantity;
  private final String _securityIdentityKey;
  
  public PositionReference(BigDecimal quantity, String securityIdentityKey) {
    ArgumentChecker.checkNotNull(quantity, "Quantity");
    ArgumentChecker.checkNotNull(securityIdentityKey, "Security IdentityKey");
    _quantity = quantity;
    _securityIdentityKey = securityIdentityKey;
  }
  
  public PositionReference(Position position) {
    ArgumentChecker.checkNotNull(position, "Position");
    ArgumentChecker.checkNotNull(position.getSecurity(), "Position's Security");
    ArgumentChecker.checkNotNull(position.getSecurity().getIdentityKey(), "Position's Security's IdentityKey");
    _quantity = position.getQuantity();
    _securityIdentityKey = position.getSecurity().getIdentityKey();
  }

  /**
   * @return the quantity
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * @return the securityIdentityKey
   */
  public String getSecurityIdentityKey() {
    return _securityIdentityKey;
  }

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
  
  public FudgeFieldContainer toFudgeMsg(FudgeContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    msg.add(QUANTITY_FIELD_NAME, getQuantity().toString());
    msg.add(SECURITY_IDENTITY_KEY_FIELD_NAME, getSecurityIdentityKey());
    return msg;
  }

  public static PositionReference fromFudgeMsg(FudgeMsgEnvelope envelope) {
    FudgeFieldContainer msg = envelope.getMessage();
    BigDecimal quantity = new BigDecimal(msg.getString(QUANTITY_FIELD_NAME));
    String identityKey = msg.getString(SECURITY_IDENTITY_KEY_FIELD_NAME);
    return new PositionReference(quantity, identityKey);
  }
}
