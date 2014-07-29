/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.sensitivity;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.util.amount.CubeValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Triple;

/**
 * Class describing the present value Black volatility sensitivity (Black volatilities dependent of expiration/delay/strike).
 */
public class PresentValueBlackBondFuturesCubeSensitivity {

  /** The object containing the volatility sensitivity. Not null. 
   * The dimension of the cube are expiration/delay/strike        */
  private final CubeValue _sensitivity;
  /** The currency of the sensitivity. Not null. */
  private final Currency _currency;
  /** The legal entity of the bonds underlying the futures for which the volatility data is valid. */
  private final LegalEntity _legalEntity;

  /**
   * Constructor with empty sensitivity.
   * @param currency The currency of the sensitivity.
   * @param legalEntity The legal entity of the bonds underlying the futures for which the volatility data is valid.
   */
  public PresentValueBlackBondFuturesCubeSensitivity(final Currency currency, final LegalEntity legalEntity) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(legalEntity, "legal entiry");
    _sensitivity = new CubeValue();
    _currency = currency;
    _legalEntity = legalEntity;
  }

  /**
   * Constructor from parameter sensitivities.
   * @param sensitivity The volatility sensitivity as a map.
   * @param currency The currency of the sensitivity.
   * @param legalEntity The legal entity of the bonds underlying the futures for which the volatility data is valid.
   */
  public PresentValueBlackBondFuturesCubeSensitivity(final Map<Triple<Double, Double, Double>, Double> sensitivity,
      final Currency currency, final LegalEntity legalEntity) {
    ArgumentChecker.notNull(sensitivity, "Sensitivity");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(legalEntity, "legal entiry");
    _sensitivity = CubeValue.from(sensitivity);
    _currency = currency;
    _legalEntity = legalEntity;
  }

  /**
   * Constructor from parameter sensitivities. The SurfaceValue are not copied but used directly.
   * @param sensitivity The volatility sensitivity as a SurfaceValue.
   * @param currency The currency of the sensitivity.
   * @param legalEntity The legal entity of the bonds underlying the futures for which the volatility data is valid.
   */
  public PresentValueBlackBondFuturesCubeSensitivity(final CubeValue sensitivity,
      final Currency currency, final LegalEntity legalEntity) {
    ArgumentChecker.notNull(sensitivity, "Sensitivity");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(legalEntity, "legal entiry");
    _sensitivity = sensitivity;
    _currency = currency;
    _legalEntity = legalEntity;
  }

  /**
   * Add one sensitivity to the volatility sensitivity. The existing object is modified. If the point is not in the existing points of the sensitivity, it is put in the map.
   * If a point is already in the existing points of the object, the value is added to the existing value.
   * @param expiryDelayStrike The expiration time/delay time/strike triple.
   * @param sensitivity The sensitivity.
   */
  public void addSensitivity(final Triple<Double, Double, Double> expiryDelayStrike, final double sensitivity) {
    _sensitivity.add(expiryDelayStrike, sensitivity);
  }

  /**
   * Create a new sensitivity object with all the sensitivities multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public PresentValueBlackBondFuturesCubeSensitivity multipliedBy(final double factor) {
    return new PresentValueBlackBondFuturesCubeSensitivity(CubeValue.multiplyBy(_sensitivity, factor), _currency, _legalEntity);
  }

  /**
   * Return the sum of to sensitivities in a new one. The original sensitivities are unchanged. The associated swap generators should be identical.
   * @param sensi The Black sensitivity to add.
   * @return The sum sensitivity.
   */
  public PresentValueBlackBondFuturesCubeSensitivity plus(final PresentValueBlackBondFuturesCubeSensitivity sensi) {
    ArgumentChecker.isTrue(_currency.equals(sensi._currency), "Swap generators should be equal to add sensitivities");
    return new PresentValueBlackBondFuturesCubeSensitivity(CubeValue.plus(_sensitivity, sensi._sensitivity), _currency, _legalEntity);
  }

  /**
   * Gets the volatility sensitivity.
   * @return The sensitivity.
   */
  public CubeValue getSensitivity() {
    return _sensitivity;
  }

  /**
   * Returns the sensitivity currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Returns legal entity of the bonds underlying the futures for which the volatility data is valid.
   * @return The legal entity.
   */
  public LegalEntity getLegalEntiry() {
    return _legalEntity;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sensitivity.hashCode();
    result = prime * result + _currency.hashCode();
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PresentValueBlackBondFuturesCubeSensitivity other = (PresentValueBlackBondFuturesCubeSensitivity) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
