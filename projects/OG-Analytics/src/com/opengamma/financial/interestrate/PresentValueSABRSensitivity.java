/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the present value SABR sensitivity.
 */
public class PresentValueSABRSensitivity {

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
  public PresentValueSABRSensitivity() {
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
  public PresentValueSABRSensitivity(Map<DoublesPair, Double> alpha, Map<DoublesPair, Double> rho, Map<DoublesPair, Double> nu) {
    Validate.notNull(alpha, "alpha");
    Validate.notNull(rho, "rho");
    Validate.notNull(nu, "nu");
    this._alpha = alpha;
    this._rho = rho;
    this._nu = nu;
  }

  /**
   * Add one sensitivity to the alpha sensitivity.
   * @param expiryMaturity The expirytime/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addAlpha(DoublesPair expiryMaturity, Double sensitivity) {
    _alpha.put(expiryMaturity, sensitivity);
  }

  /**
   * Add one sensitivity to the rho sensitivity.
   * @param expiryMaturity The expirytime/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addRho(DoublesPair expiryMaturity, Double sensitivity) {
    _rho.put(expiryMaturity, sensitivity);
  }

  /**
   * Add one sensitivity to the nu sensitivity.
   * @param expiryMaturity The expirytime/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addNu(DoublesPair expiryMaturity, Double sensitivity) {
    _nu.put(expiryMaturity, sensitivity);
  }

  /**
   * Multiply all the sensitivities by a common factor. 
   * @param factor The multiplicative factor.
   */
  public void multiply(double factor) {
    for (DoublesPair p : _alpha.keySet()) {
      _alpha.put(p, _alpha.get(p) * factor);
    }
    for (DoublesPair p : _rho.keySet()) {
      _rho.put(p, _rho.get(p) * factor);
    }
    for (DoublesPair p : _nu.keySet()) {
      _nu.put(p, _nu.get(p) * factor);
    }
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

}
