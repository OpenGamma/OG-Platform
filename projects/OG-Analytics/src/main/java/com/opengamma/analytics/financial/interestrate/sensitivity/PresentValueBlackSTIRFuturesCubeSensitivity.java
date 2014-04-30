/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.sensitivity;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.util.amount.CubeValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Class describing the present value Black volatility sensitivity (Black volatilities dependent of expiration/delay/strike).
 */
public class PresentValueBlackSTIRFuturesCubeSensitivity {

  /** The object containing the volatility sensitivity. Not null. 
   * The dimension of the cube are expiration/delay/strike price */
  private final CubeValue _sensitivity;
  /** The Ibor Index of the futures on for which the Black data is valid, i.e. the data is calibrated to futures on the given index. Not null. */
  private final IborIndex _index;

  /**
   * Constructor with empty sensitivity.
   * @param index The Ibor Index of the futures on for which the Black data is valid.
   */
  public PresentValueBlackSTIRFuturesCubeSensitivity(final IborIndex index) {
    ArgumentChecker.notNull(index, "index");
    _sensitivity = new CubeValue();
    _index = index;
  }

  /**
   * Constructor from parameter sensitivities.
   * @param sensitivity The volatility sensitivity as a map.
   * @param index The Ibor Index of the futures on for which the Black data is valid.
   */
  public PresentValueBlackSTIRFuturesCubeSensitivity(final Map<Triple<Double, Double, Double>, Double> sensitivity,
      final IborIndex index) {
    ArgumentChecker.notNull(sensitivity, "Sensitivity");
    ArgumentChecker.notNull(index, "Ibor index");
    _sensitivity = CubeValue.from(sensitivity);
    _index = index;
  }

  /**
   * Constructor from parameter sensitivities. The SurfaceValue are not copied but used directly.
   * @param sensitivity The volatility sensitivity as a SurfaceValue.
   * @param index The Ibor Index of the futures on for which the Black data is valid.
   */
  public PresentValueBlackSTIRFuturesCubeSensitivity(final CubeValue sensitivity, final IborIndex index) {
    ArgumentChecker.notNull(sensitivity, "Sensitivity");
    ArgumentChecker.notNull(index, "Ibor index");
    _sensitivity = sensitivity;
    _index = index;
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
  public PresentValueBlackSTIRFuturesCubeSensitivity multipliedBy(final double factor) {
    return new PresentValueBlackSTIRFuturesCubeSensitivity(CubeValue.multiplyBy(_sensitivity, factor), _index);
  }

  /**
   * Return the sum of to sensitivities in a new one. The original sensitivities are unchanged. The associated swap generators should be identical.
   * @param sensi The Black sensitivity to add.
   * @return The sum sensitivity.
   */
  public PresentValueBlackSTIRFuturesCubeSensitivity plus(final PresentValueBlackSTIRFuturesCubeSensitivity sensi) {
    ArgumentChecker.isTrue(_index.equals(sensi._index), "Swap generators should be equal to add sensitivities");
    return new PresentValueBlackSTIRFuturesCubeSensitivity(CubeValue.plus(_sensitivity, sensi._sensitivity), _index);
  }

  /**
   * Gets the volatility sensitivity.
   * @return The sensitivity.
   */
  public CubeValue getSensitivity() {
    return _sensitivity;
  }

  /**
   * Returns the Ibor Index of the futures on for which the Black data is valid.
   * @return The index.
   */
  public IborIndex getIborIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sensitivity.hashCode();
    result = prime * result + _index.hashCode();
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
    final PresentValueBlackSTIRFuturesCubeSensitivity other = (PresentValueBlackSTIRFuturesCubeSensitivity) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
