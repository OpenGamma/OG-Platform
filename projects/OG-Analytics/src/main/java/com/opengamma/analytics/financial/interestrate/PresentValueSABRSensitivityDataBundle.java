/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the present value SABR sensitivity.
 */
public class PresentValueSABRSensitivityDataBundle {

  /**
   * The object containing the alpha sensitivity.
   */
  private final SurfaceValue _alpha;
  /**
   * The object containing the beta sensitivity.
   */
  private final SurfaceValue _beta;
  /**
   * The object containing the rho (correlation) sensitivity.
   */
  private final SurfaceValue _rho;
  /**
   * The object containing the nu (vol of vol) sensitivity.
   */
  private final SurfaceValue _nu;

  /**
   * Constructor with empty sensitivities.
   */
  public PresentValueSABRSensitivityDataBundle() {
    _alpha = new SurfaceValue();
    _beta = new SurfaceValue();
    _rho = new SurfaceValue();
    _nu = new SurfaceValue();
  }

  /**
   * Constructor from parameter sensitivities.
   * @param alpha The alpha sensitivity.
   * @param beta The beta sensitivity.
   * @param rho The rho sensitivity.
   * @param nu The nu sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle(final Map<DoublesPair, Double> alpha, final Map<DoublesPair, Double> beta, final Map<DoublesPair, Double> rho, final Map<DoublesPair, Double> nu) {
    ArgumentChecker.notNull(alpha, "alpha");
    ArgumentChecker.notNull(beta, "beta");
    ArgumentChecker.notNull(rho, "rho");
    ArgumentChecker.notNull(nu, "nu");
    _alpha = SurfaceValue.from(alpha);
    _beta = SurfaceValue.from(beta);
    _rho = SurfaceValue.from(rho);
    _nu = SurfaceValue.from(nu);
  }

  /**
   * Constructor from existing SurfaceValue for alpha, rho and nu. The SurfaceValue are not copied but used directly.
   * @param alpha The alpha sensitivities.
   * @param beta The beta sensitivities.
   * @param rho The rho sensitivities.
   * @param nu The nu sensitivities.
   */
  public PresentValueSABRSensitivityDataBundle(final SurfaceValue alpha, final SurfaceValue beta, final SurfaceValue rho, final SurfaceValue nu) {
    ArgumentChecker.notNull(alpha, "alpha");
    ArgumentChecker.notNull(beta, "beta");
    ArgumentChecker.notNull(rho, "rho");
    ArgumentChecker.notNull(nu, "nu");
    _alpha = alpha;
    _beta = SurfaceValue.from(beta);
    _rho = rho;
    _nu = nu;
  }

  /**
   * Add one sensitivity to the alpha sensitivity. The existing object is modified. If the point is not in the existing points of the sensitivity, it is put in the map.
   * If a point is already in the existing points of the object, the value is added to the existing value.
   * @param expiryMaturity The expirytime/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addAlpha(final DoublesPair expiryMaturity, final double sensitivity) {
    _alpha.add(expiryMaturity, sensitivity);
  }

  /**
   * Add one sensitivity to the beta sensitivity. The existing object is modified. If the point is not in the existing points of the sensitivity, it is put in the map.
   * If a point is already in the existing points of the object, the value is added to the existing value.
   * @param expiryMaturity The expirytime/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addBeta(final DoublesPair expiryMaturity, final double sensitivity) {
    _beta.add(expiryMaturity, sensitivity);
  }

  /**
   * Add one sensitivity to the rho sensitivity.
   * @param expiryMaturity The expirytime/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addRho(final DoublesPair expiryMaturity, final double sensitivity) {
    _rho.add(expiryMaturity, sensitivity);
  }

  /**
   * Add one sensitivity to the nu sensitivity.
   * @param expiryMaturity The expirytime/maturity pair.
   * @param sensitivity The sensitivity.
   */
  public void addNu(final DoublesPair expiryMaturity, final double sensitivity) {
    _nu.add(expiryMaturity, sensitivity);
  }

  /**
   * Create a new sensitivity object with all the sensitivities multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle multiplyBy(final double factor) {
    return new PresentValueSABRSensitivityDataBundle(SurfaceValue.multiplyBy(_alpha, factor), SurfaceValue.multiplyBy(_beta, factor), SurfaceValue.multiplyBy(_rho, factor), SurfaceValue.multiplyBy(
        _nu, factor));
  }

  /**
   * Return the sum of to sensitivities in a new one. The original sensitivities are unchanged.
   * @param other The other SABR sensitivity.
   * @return The sum sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle plus(final PresentValueSABRSensitivityDataBundle other) {
    return new PresentValueSABRSensitivityDataBundle(SurfaceValue.plus(_alpha, other._alpha), SurfaceValue.plus(_beta, other._beta), SurfaceValue.plus(_rho, other._rho), SurfaceValue.plus(_nu,
        other._nu));
  }

  /**
   * Gets the alpha sensitivity.
   * @return The alpha sensitivity.
   */
  public SurfaceValue getAlpha() {
    return _alpha;
  }

  /**
   * Gets the beta sensitivity.
   * @return The beta sensitivity.
   */
  public SurfaceValue getBeta() {
    return _beta;
  }

  /**
   * Gets the rho sensitivity.
   * @return The rho sensitivity.
   */
  public SurfaceValue getRho() {
    return _rho;
  }

  /**
   * Gets the nu sensitivity.
   * @return The nu sensitivity.
   */
  public SurfaceValue getNu() {
    return _nu;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _alpha.hashCode();
    result = prime * result + _beta.hashCode();
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
    if (!ObjectUtils.equals(_beta, other._beta)) {
      return false;
    }
    if (!ObjectUtils.equals(_nu, other._nu)) {
      return false;
    }
    return ObjectUtils.equals(_rho, other._rho);
  }

}
