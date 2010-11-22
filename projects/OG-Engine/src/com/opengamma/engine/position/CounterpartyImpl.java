/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code Counterparty}.
 */
public class CounterpartyImpl implements Counterparty, Serializable {
  
  private Identifier _identifier;
  
  /**
   * @param identifier the identifier not -null
   */
  public CounterpartyImpl(Identifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifier = identifier;
  }

  @Override
  public Identifier getIdentifier() {
    return _identifier;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof CounterpartyImpl) {
      CounterpartyImpl other = (CounterpartyImpl) obj;
      return ObjectUtils.equals(_identifier, other._identifier);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hashCode = 65;
    hashCode += _identifier.hashCode();
    return hashCode;
  }

  @Override
  public String toString() {
    return new StrBuilder().append("Counterparty[id:").append(getIdentifier().toString()).append(']').toString();
  }

}
