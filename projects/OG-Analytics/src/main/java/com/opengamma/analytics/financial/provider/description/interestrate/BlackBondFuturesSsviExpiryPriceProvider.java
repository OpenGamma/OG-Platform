/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.model.volatility.smile.function.SSVIVolatilityFunction;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.differentiation.ValueDerivatives;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Implementation of Black implied volatility for bond futures with volatility given by a SSVI formula and 
 * expiration time dependent rho and eta.
 */
public class BlackBondFuturesSsviExpiryPriceProvider implements BlackBondFuturesSsviPriceProvider {

  /**
   * The multicurve provider.
   */
  private final IssuerProviderInterface _issuerProvider;
  /** The Black ATM implied volatility curve. */
  private final DoublesCurve _volatilityAtm;
  /** The rho parameter of the SSVI formula. **/
  private final DoublesCurve _rho;
  /** The eta parameter of the SSVI formula. **/
  private final DoublesCurve _eta;
  /** The legal entity of the bonds underlying the futures for which the volatility data is valid. */
  private final LegalEntity _legalEntity;

  /**
   * Constructor.
   * @param issuerProvider The issuer and multi-curve provider, not null
   * @param volatilityAtm The ATM imoplied volatility curve.
   * @param rho The rho parameter.
   * @param eta The eta parameter.
   * @param legalEntity The legal entity of the bonds underlying the futures for which the volatility data is valid.
   */
  public BlackBondFuturesSsviExpiryPriceProvider(
      final IssuerProviderInterface issuerProvider, 
      DoublesCurve volatilityAtm,
      DoublesCurve rho,
      DoublesCurve eta,
      final LegalEntity legalEntity) {
    ArgumentChecker.notNull(issuerProvider, "issuerProvider");
    ArgumentChecker.notNull(volatilityAtm, "volatility ATM");
    ArgumentChecker.notNull(legalEntity, "legal entity");
    _issuerProvider = issuerProvider;
    this._volatilityAtm = volatilityAtm;
    this._rho = rho;
    this._eta = eta;
    _legalEntity = legalEntity;
  }

  @Override
  public BlackBondFuturesSsviExpiryPriceProvider copy() {
    final IssuerProviderInterface multicurveProvider = _issuerProvider.copy();
    return new BlackBondFuturesSsviExpiryPriceProvider(multicurveProvider, _volatilityAtm, _rho, _eta, _legalEntity);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _issuerProvider.getMulticurveProvider();
  }

  @Override
  public IssuerProviderInterface getIssuerProvider() {
    return _issuerProvider;
  }
  
  @Override
  public double getVolatility(final double expiry, final double delay, final double strikePrice, final double futuresPrice) {
    return SSVIVolatilityFunction.volatility(futuresPrice, strikePrice, expiry, 
        _volatilityAtm.getYValue(expiry), _rho.getYValue(expiry), _eta.getYValue(expiry));
  }
  
  @Override
  public ValueDerivatives volatilityAdjoint(double expiry, double delay, double strikePrice, double futuresPrice) {
    return SSVIVolatilityFunction.volatilityAdjoint(futuresPrice, strikePrice, expiry, 
        _volatilityAtm.getYValue(expiry), _rho.getYValue(expiry), _eta.getYValue(expiry));
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _issuerProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _issuerProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _issuerProvider.getAllCurveNames();
  }

  @Override
  public LegalEntity getLegalEntity() {
    return _legalEntity;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _issuerProvider.hashCode();
    result = prime * result + _volatilityAtm.hashCode();
    result = prime * result + _legalEntity.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackBondFuturesSsviExpiryPriceProvider)) {
      return false;
    }
    final BlackBondFuturesSsviExpiryPriceProvider other = (BlackBondFuturesSsviExpiryPriceProvider) obj;
    if (!ObjectUtils.equals(_issuerProvider, other._issuerProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatilityAtm, other._volatilityAtm)) {
      return false;
    }
    if (!ObjectUtils.equals(_legalEntity, other._legalEntity)) {
      return false;
    }
    return true;
  }

}
