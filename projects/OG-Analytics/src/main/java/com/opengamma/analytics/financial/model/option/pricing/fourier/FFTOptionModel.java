/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.definition.BlackOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.pricing.OptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;

/**
 * 
 */
public class FFTOptionModel implements OptionModel<EuropeanVanillaOptionDefinition, BlackOptionDataBundle> {
  private static final Logger s_logger = LoggerFactory.getLogger(FFTOptionModel.class);
  private static final int DEFAULT_STRIKES = 256;
  private static final double DEFAULT_MAX_DELTA_MONEYNESS = 0.1;
  private static final double DEFAULT_ALPHA = -0.5;
  private static final double DEFAULT_TOLERANCE = 1e-8;
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR = new DoubleQuadraticInterpolator1D();
  private static final FFTPricer PRICER = new FFTPricer();
  private final MartingaleCharacteristicExponent _characteristicExponent;
  private final int _nStrikes;
  private final double _maxDeltaMoneyness;
  private final double _alpha;
  private final double _tolerance;

  //TODO add interpolator as input
  public FFTOptionModel(final MartingaleCharacteristicExponent characteristicExponent) {
    this(characteristicExponent, DEFAULT_STRIKES, DEFAULT_MAX_DELTA_MONEYNESS, DEFAULT_ALPHA, DEFAULT_TOLERANCE);
  }

  public FFTOptionModel(final MartingaleCharacteristicExponent characteristicExponent, final int nStrikes, final double maxDeltaMoneyness, final double alpha, final double tolerance) {
    Validate.notNull(characteristicExponent, "characteristic exponent");
    Validate.isTrue(nStrikes > 0, "number of strikes must be > 0");
    Validate.isTrue(maxDeltaMoneyness > 0, "max delta moneyness must be > 0");
    Validate.isTrue(alpha != 0 && alpha != -1, "alpha cannot be -1 or 0");
    Validate.isTrue(tolerance > 0, "tolerance must be > 0");
    _characteristicExponent = characteristicExponent;
    _nStrikes = nStrikes;
    _maxDeltaMoneyness = maxDeltaMoneyness;
    _alpha = alpha;
    _tolerance = tolerance;
  }

  @Override
  public GreekResultCollection getGreeks(final EuropeanVanillaOptionDefinition definition, final BlackOptionDataBundle dataBundle, final Set<Greek> requiredGreeks) {
    Validate.notNull(definition, "definition");
    Validate.notNull(dataBundle, "data bundle");
    Validate.notNull(requiredGreeks, "required greeks");
    if (!requiredGreeks.contains(Greek.FAIR_PRICE)) {
      throw new NotImplementedException("Can only calculate fair price at the moment: asked for " + requiredGreeks);
    }
    if (requiredGreeks.size() > 1) {
      s_logger.warn("Can only calculate fair price - ignoring other greeks");
    }
    final ZonedDateTime date = dataBundle.getDate();
    final EuropeanVanillaOption option = EuropeanVanillaOption.fromDefinition(definition, date);
    final BlackFunctionData data = BlackFunctionData.fromDataBundle(dataBundle, definition);

    double fwd = data.getForward();
    double df = data.getDiscountFactor();
    double t = option.getTimeToExpiry();
    boolean isCall = option.isCall();
    double limitSigma = data.getBlackVolatility(); //TODO This is a tuning parameter of the algorithm and has no business being passed in a BlackOptionDataBundle

    final double[][] prices = PRICER.price(fwd, df, t, isCall, _characteristicExponent, _nStrikes, _maxDeltaMoneyness, limitSigma, _alpha, _tolerance);
    final int n = prices.length;
    final double[] k = new double[n];
    final double[] price = new double[n];
    for (int i = 0; i < n; i++) {
      k[i] = prices[i][0];
      price[i] = prices[i][1];
    }
    final double fairValue = INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundleFromSortedArrays(k, price), definition.getStrike());
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.FAIR_PRICE, fairValue);
    return result;
  }

}
