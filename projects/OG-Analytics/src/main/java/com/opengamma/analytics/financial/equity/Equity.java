/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class Equity implements InstrumentDerivative, Serializable{
  /** The entity */
  private final LegalEntity _entity;
  /** The currency */
  private final Currency _currency;
  /** The number of shares */
  private final double _numberOfShares;

  /**
   * @param entity The entity, not null
   * @param currency The currency, not null
   * @param numberOfShares The number of shares
   */
  public Equity(final LegalEntity entity, final Currency currency, final double numberOfShares) {
    ArgumentChecker.notNull(entity, "entity");
    ArgumentChecker.notNull(currency, "currency");
    _entity = entity;
    _currency = currency;
    _numberOfShares = numberOfShares;
  }

  /**
   * Gets the entity.
   * @return The entity
   */
  public LegalEntity getEntity() {
    return _entity;
  }

  /**
   * The currency.
   * @return The currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * The number of shares.
   * @return The number of shares
   */
  public double getNumberOfShares() {
    return _numberOfShares;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return null;
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _entity.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_numberOfShares);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Equity)) {
      return false;
    }
    final Equity other = (Equity) obj;
    if (Double.compare(_numberOfShares, other._numberOfShares) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_entity, other._entity)) {
      return false;
    }
    return true;
  }

}
