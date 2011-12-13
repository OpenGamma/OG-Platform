/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.surface.SurfaceValue;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the present value SABR sensitivity.
 */
public class PresentValueSABRSensitivityDataBundle {

  //TODO: Should the currency/instrument (swaption, cap) be included in the map description?
  /**
   * The object containing the alpha sensitivity.
   */
  private final SurfaceValue _alpha;
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
    _rho = new SurfaceValue();
    _nu = new SurfaceValue();
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
    _alpha = SurfaceValue.from(alpha);
    _rho = SurfaceValue.from(rho);
    _nu = SurfaceValue.from(nu);
  }

  /**
   * Constructor from existing SurfaceValue for alpha, rho and nu. The SurfaceValue are not copied but used directly.
   * @param alpha The alpha sensitivities.
   * @param rho The rho sensitivities.
   * @param nu The nu sensitivities.
   */
  public PresentValueSABRSensitivityDataBundle(final SurfaceValue alpha, final SurfaceValue rho, final SurfaceValue nu) {
    Validate.notNull(alpha, "alpha");
    Validate.notNull(rho, "rho");
    Validate.notNull(nu, "nu");
    _alpha = alpha;
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
   * @param sensi The SABR sensitivity.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public static PresentValueSABRSensitivityDataBundle multiplyBy(final PresentValueSABRSensitivityDataBundle sensi, final double factor) {
    return new PresentValueSABRSensitivityDataBundle(SurfaceValue.multiplyBy(sensi._alpha, factor), SurfaceValue.multiplyBy(sensi._rho, factor), SurfaceValue.multiplyBy(sensi._nu, factor));
  }

  /**
   * Return the sum of to sensitivities in a new one. The original sensitivities are unchanged. 
   * @param sensi1 The first SABR sensitivity. 
   * @param sensi2 The second SABR sensitivity.
   * @return The sum sensitivity.
   */
  public static PresentValueSABRSensitivityDataBundle plus(final PresentValueSABRSensitivityDataBundle sensi1, final PresentValueSABRSensitivityDataBundle sensi2) {
    return new PresentValueSABRSensitivityDataBundle(SurfaceValue.plus(sensi1._alpha, sensi2._alpha), SurfaceValue.plus(sensi1._rho, sensi2._rho), SurfaceValue.plus(sensi1._nu, sensi2._nu));
  }

  /**
   * Gets the alpha sensitivity.
   * @return The alpha sensitivity.
   */
  public SurfaceValue getAlpha() {
    return _alpha;
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
