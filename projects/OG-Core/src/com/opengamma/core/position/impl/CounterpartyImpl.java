/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.core.position.Counterparty;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code Counterparty}.
 */
public class CounterpartyImpl implements Counterparty, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The counterparty identifier.
   */
  private ExternalId _counterpartyId;

  /**
   * Creates an instance.
   * 
   * @param counterpartyId  the identifier, not null
   */
  public CounterpartyImpl(ExternalId counterpartyId) {
    ArgumentChecker.notNull(counterpartyId, "counterpartyId");
    _counterpartyId = counterpartyId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the identifier of the counterparty.
   * 
   * @return the identifier, not null
   */
  @Override
  public ExternalId getExternalId() {
    return _counterpartyId;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof CounterpartyImpl) {
      CounterpartyImpl other = (CounterpartyImpl) obj;
      return ObjectUtils.equals(getExternalId(), other.getExternalId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hashCode = 65;
    hashCode += getExternalId().hashCode();
    return hashCode;
  }

  @Override
  public String toString() {
    return new StrBuilder().append("Counterparty[id:").append(getExternalId().toString()).append(']').toString();
  }

}
