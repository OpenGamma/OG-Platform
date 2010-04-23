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

import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * A simple JavaBean-based implementation of {@link Position}.
 *
 * @author kirk
 */
public class PositionBean implements Position, Serializable {
  private final BigDecimal _quantity;
  private final IdentifierBundle _securityKey;
  private Security _security;
  private Identifier _identityKey;
  
  public PositionBean(BigDecimal quantity, Identifier identifier) {
    _quantity = quantity;
    _securityKey = new IdentifierBundle(identifier);
    _security = null;
  }
  
  public PositionBean(BigDecimal quantity, IdentifierBundle securityKey) {
    _quantity = quantity;
    _securityKey = securityKey;
    _security = null;
  }
  
  public PositionBean(BigDecimal quantity, IdentifierBundle securityKey, Security security) {
    _quantity = quantity;
    _securityKey = securityKey;
    _security = security;
  }
  
  public PositionBean(BigDecimal quantity, Security security) {
    _quantity = quantity;
    _security = security;
    _securityKey = security.getIdentifiers() != null ? new IdentifierBundle(security.getIdentifiers()) : null;
  }

  @Override
  public BigDecimal getQuantity() {
    return _quantity;
  }

  @Override
  public IdentifierBundle getSecurityKey() {
    return _securityKey;
  }
  
  @Override
  public Security getSecurity() {
    return _security;
  }
  
  public void setSecurity(Security security) {
    _security = security;
  }

  /**
   * @return the identityKey
   */
  public Identifier getIdentityKey() {
    return _identityKey;
  }

  /**
   * @param identityKey the identityKey to set
   */
  public void setIdentityKey(String identityKey) {
    _identityKey = new Identifier(POSITION_IDENTITY_KEY_DOMAIN, identityKey);
  }
  
  public void setIdentityKey(Identifier identityKey) {
    ArgumentChecker.checkNotNull(identityKey, "Identity key");
    if (!POSITION_IDENTITY_KEY_DOMAIN.equals(identityKey.getScheme())) {
      throw new IllegalArgumentException("Wrong domain specified:" + identityKey.getScheme());
    }
    _identityKey = identityKey; 
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof PositionBean)) {
      return false;
    }
    PositionBean other = (PositionBean) obj;
    // Use comparison here to deal with scale issues with BigDecimal comparisons.
    if(CompareUtils.compareWithNull(getQuantity(), other.getQuantity()) != 0) {
      return false;
    }
    if(!ObjectUtils.equals(getSecurityKey(), other.getSecurityKey())) {
      return false;
    }
    if(!ObjectUtils.equals(getSecurity(), other.getSecurity())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 65;
    hashCode += getQuantity().hashCode();
    if(getSecurityKey() != null) {
      hashCode <<= 5;
      hashCode += getSecurityKey().hashCode();
    } else if(getSecurity() != null) {
      hashCode <<= 5;
      hashCode += getSecurity().hashCode();
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
