/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the present value SABR sensitivity.
 */
public class PresentValueSABRSensitivityDataBundle {

  //TODO: Should the currency/instrument (swaption, cap) be included in the map description?
  /**
   * The map containing the alpha sensitivity. The map linked the pair (expiry time, tenor) to a double (sensitivity).
   */
  private final Map<DoublesPair, Double> _alpha;
  /**
  * The map containing the rho (correlation) sensitivity. The map linked the pair (expiry time, tenor) to a double (sensitivity).
  */
  private final Map<DoublesPair, Double> _rho;
  /**
    * The map containing the nu (volatility of volatility) sensitivity. The map linked the pair (expiry time, tenor) to a double (sensitivity).
    */
  private final Map<DoublesPair, Double> _nu;

  /**
   * Constructor with empty sensitivities.
   */
  public PresentValueSABRSensitivityDataBundle() {
    this._alpha = new HashMap<DoublesPair, Double>();
    this._rho = new HashMap<DoublesPair, Double>();
    this._nu = new HashMap<DoublesPair, Double>();
  }

  /**
   * Constructor from parameter sensitivities.
   * @param alpha The alpha sensitivity.
   * @param rho The rho sensitivity.
   * @param nu The nu sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle(final Map<DoublesPair, Double> alpha, final Map<DoublesPair, Double> rho, final Map<DoublesPair, Double> nu) {
    Validate.notNull(alpha, "alpha");
    Validate.notNull(rho, "rho");
    Validate.notNull(nu, "nu");
    this._alpha = new HashMap<DoublesPair, Double>(alpha);
    this._rho = new HashMap<DoublesPair, Double>(rho);
    this._nu = new HashMap<DoublesPair, Double>(nu);
  }

  /**
   * Add one sensitivity to the alpha sensitivity.
   * @param expiryMaturity The expirytime/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addAlpha(final DoublesPair expiryMaturity, final double sensitivity) {
    _alpha.put(expiryMaturity, sensitivity);
  }

  /**
   * Add one sensitivity to the rho sensitivity.
   * @param expiryMaturity The expirytime/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addRho(final DoublesPair expiryMaturity, final double sensitivity) {
    _rho.put(expiryMaturity, sensitivity);
  }

  /**
   * Add one sensitivity to the nu sensitivity.
   * @param expiryMaturity The expirytime/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addNu(final DoublesPair expiryMaturity, final double sensitivity) {
    _nu.put(expiryMaturity, sensitivity);
  }

  /**
   * Multiply all the sensitivities by a common factor. 
   * @param factor The multiplicative factor.
   */
  public void multiply(final double factor) {
    for (final DoublesPair p : _alpha.keySet()) {
      _alpha.put(p, _alpha.get(p) * factor);
    }
    for (final DoublesPair p : _rho.keySet()) {
      _rho.put(p, _rho.get(p) * factor);
    }
    for (final DoublesPair p : _nu.keySet()) {
      _nu.put(p, _nu.get(p) * factor);
    }
  }

  /**
   * Return the sum of to sensitivities in a ne one. The original sensitivities are unchanged.
   * @param other Another SABR sensitivity.
   * @return The sum sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle plus(final PresentValueSABRSensitivityDataBundle other) {
    Map<DoublesPair, Double> alpha = new HashMap<DoublesPair, Double>(_alpha);
    Map<DoublesPair, Double> rho = new HashMap<DoublesPair, Double>(_rho);
    Map<DoublesPair, Double> nu = new HashMap<DoublesPair, Double>(_nu);
    for (final DoublesPair p : other.getAlpha().keySet()) {
      if (_alpha.containsKey(p)) {
        alpha.put(p, other.getAlpha().get(p) + _alpha.get(p));
      } else {
        alpha.put(p, other.getAlpha().get(p));
      }
    }
    for (final DoublesPair p : other.getRho().keySet()) {
      if (_rho.containsKey(p)) {
        rho.put(p, other.getRho().get(p) + _rho.get(p));
      } else {
        rho.put(p, other.getRho().get(p));
      }
    }
    for (final DoublesPair p : other.getNu().keySet()) {
      if (_nu.containsKey(p)) {
        nu.put(p, other.getNu().get(p) + _nu.get(p));
      } else {
        nu.put(p, other.getNu().get(p));
      }
    }
    return new PresentValueSABRSensitivityDataBundle(alpha, rho, nu);
  }

  /**
   * Gets the _alpha field.
   * @return The alpha sensitivity.
   */
  public Map<DoublesPair, Double> getAlpha() {
    return _alpha;
  }

  /**
   * Gets the _rho field.
   * @return The rho sensitivity.
   */
  public Map<DoublesPair, Double> getRho() {
    return _rho;
  }

  /**
   * Gets the _nu field.
   * @return The nu sensitivity.
   */
  public Map<DoublesPair, Double> getNu() {
    return _nu;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _alpha.hashCode();
    result = prime * result + _nu.hashCode();
    result = prime * result + _rho.hashCode();
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
    final PresentValueSABRSensitivityDataBundle other = (PresentValueSABRSensitivityDataBundle) obj;
    if (!ObjectUtils.equals(_alpha, other._alpha)) {
      return false;
    }
    if (!ObjectUtils.equals(_nu, other._nu)) {
      return false;
    }
    return ObjectUtils.equals(_rho, other._rho);
  }

}
