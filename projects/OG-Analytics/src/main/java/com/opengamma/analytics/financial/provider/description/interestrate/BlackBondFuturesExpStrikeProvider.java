/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Implementation of a provider of Black smile for options on bond futures. The volatility is time to expiration/delay dependent.
 * The delay is the time difference between the last notice and the option expiration.
 */
public class BlackBondFuturesExpStrikeProvider implements BlackBondFuturesProviderInterface {

  /**
   * The multicurve provider.
   */
  private final IssuerProviderInterface _issuerProvider;
  /**
   * The Black volatility surface. Not null. The dimensions are expiration and strike.
   */
  private final Surface<Double, Double, Double> _parameters;
  /**
   * The legal entity of the bonds underlying the futures for which the volatility data is valid.
   */
  private final LegalEntity _legalEntity;

  /**
   * Constructor.
   * @param issuerProvider The issuer and multi-curve provider, not null
   * @param parameters The Black parameters, not null
   * @param legalEntity The legal entity of the bonds underlying the futures for which the volatility data is valid.
   */
  public BlackBondFuturesExpStrikeProvider(final IssuerProviderInterface issuerProvider, 
      Surface<Double, Double, Double> parameters, final LegalEntity legalEntity) {
    ArgumentChecker.notNull(issuerProvider, "issuerProvider");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(legalEntity, "legal entity");
    _issuerProvider = issuerProvider;
    _parameters = parameters;
    _legalEntity = legalEntity;
  }

  @Override
  public BlackBondFuturesExpStrikeProvider copy() {
    final IssuerProviderInterface multicurveProvider = _issuerProvider.copy();
    return new BlackBondFuturesExpStrikeProvider(multicurveProvider, _parameters, _legalEntity);
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
  /**
   * Gets the Black volatility at a given expiry-delay-dtrike-futures price point. 
   * The delay and futures price dimensions are ignored.
   * @param expiry The time to expiration.
   * @param delay The delay between the option expiry and the futures expiry. Dimension ignored.
   * @param strike The option strike.
   * @param futuresPrice The price of the underlying futures. Dimension ignored.
   * @return The volatility.
   */
  public double getVolatility(final double expiry, final double delay, final double strike, final double futuresPrice) {
    ArgumentChecker.isTrue(futuresPrice > 0, "negative futures price");
    ArgumentChecker.isTrue(strike > 0, "negative strike");
    final double logMoneyness = Math.log(strike / futuresPrice);
    return _parameters.getZValue(expiry, logMoneyness);
  }

  /**
   * Returns the Black parameters.
   * @return The parameters.
   */
  public Surface<Double, Double, Double> getBlackParameters() {
    return _parameters;
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
    result = prime * result + _parameters.hashCode();
    result = prime * result + _legalEntity.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackBondFuturesExpStrikeProvider)) {
      return false;
    }
    final BlackBondFuturesExpStrikeProvider other = (BlackBondFuturesExpStrikeProvider) obj;
    if (!ObjectUtils.equals(_issuerProvider, other._issuerProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    if (!ObjectUtils.equals(_legalEntity, other._legalEntity)) {
      return false;
    }
    return true;
  }

}
