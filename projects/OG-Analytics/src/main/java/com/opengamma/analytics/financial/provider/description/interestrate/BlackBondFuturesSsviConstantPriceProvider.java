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
 * constant eho and eta.
 */
public class BlackBondFuturesSsviConstantPriceProvider implements BlackBondFuturesSsviPriceProvider {

  /**
   * The multicurve provider.
   */
  private final IssuerProviderInterface _issuerProvider;
  /** The Black ATM implied volatility curve. */
  private final DoublesCurve _volatilityAtm;
  /** The rho parameter of the SSVI formula. **/
  private final double _rho;
  /** The eta parameter of the SSVI formula. **/
  private final double _eta;
  /**
   * The legal entity of the bonds underlying the futures for which the volatility data is valid.
   */
  private final LegalEntity _legalEntity;

  /**
   * Constructor.
   * @param issuerProvider The issuer and multi-curve provider, not null
   * @param volatilityAtm The ATM imoplied volatility curve.
   * @param rho The rho parameter.
   * @param eta The eta parameter.
   * @param legalEntity The legal entity of the bonds underlying the futures for which the volatility data is valid.
   */
  public BlackBondFuturesSsviConstantPriceProvider(
      final IssuerProviderInterface issuerProvider, 
      DoublesCurve volatilityAtm,
      double rho,
      double eta,
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
  public BlackBondFuturesSsviConstantPriceProvider copy() {
    final IssuerProviderInterface multicurveProvider = _issuerProvider.copy();
    return new BlackBondFuturesSsviConstantPriceProvider(multicurveProvider, _volatilityAtm, _rho, _eta, _legalEntity);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _issuerProvider.getMulticurveProvider();
  }

  @Override
  public IssuerProviderInterface getIssuerProvider() {
    return _issuerProvider;
  }
  
  /**
   * Gets the Black volatility at a given expiry-delay point. The delay dimension is ignored.
   * @param expiry The time to expiration.
   * @param delay The delay between the option expiry and the futures expiry.
   * @param strike The option strike.
   * @param futuresPrice The price of the underlying futures.
   * @return The volatility.
   */
  @Override
  public double getVolatility(final double expiry, final double delay, final double strike, final double futuresPrice) {
    return SSVIVolatilityFunction
        .volatility(futuresPrice, strike, expiry, _volatilityAtm.getYValue(expiry), _rho, _eta);
  }
  
  /**
   * Computes the volatility and its derivative with respect to the inputs.
   * @param expiry The option time to expiration.
   * @param delay The delay between expiration of the option and last trading date of the underlying futures.
   * @param strikePrice The strike price (not the strike rate).
   * @param futuresPrice The price of the underlying futures.
   * @return  The volatility and its derivatives with respect to the inputs. In the {@link ValueDerivatives} object,
   * the order of the derivatives are: [0] price, [1] strike, [2] expiry, [3] ATM vol, [4] rho, [5] eta.
   */
  @Override
  public ValueDerivatives volatilityAdjoint(
      double expiry, double delay, double strikePrice, double futuresPrice) {
    return SSVIVolatilityFunction
        .volatilityAdjoint(futuresPrice, strikePrice, expiry, _volatilityAtm.getYValue(expiry), _rho, _eta);
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
    if (!(obj instanceof BlackBondFuturesSsviConstantPriceProvider)) {
      return false;
    }
    final BlackBondFuturesSsviConstantPriceProvider other = (BlackBondFuturesSsviConstantPriceProvider) obj;
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
