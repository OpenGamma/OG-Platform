/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.option.definition.BlackOptionDataBundle;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.pricing.OptionModel;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public class FFTOptionModel implements OptionModel<EuropeanVanillaOptionDefinition, BlackOptionDataBundle> {
  private static final Logger s_logger = LoggerFactory.getLogger(FFTOptionModel.class);
  private static final int DEFAULT_STRIKES = 1;
  private static final double DEFAULT_MAX_DELTA_MONEYNESS = 0.1;
  private static final double DEFAULT_ALPHA = -0.5;
  private static final double DEFAULT_TOLERANCE = 1e-8;
  private static final FFTPricer1 PRICER = new FFTPricer1();
  private final CharacteristicExponent1 _characteristicExponent;
  private final int _nStrikes;
  private final double _maxDeltaMoneyness;
  private final double _alpha;
  private final double _tolerance;

  public FFTOptionModel(final CharacteristicExponent1 characteristicExponent) {
    this(characteristicExponent, DEFAULT_STRIKES, DEFAULT_MAX_DELTA_MONEYNESS, DEFAULT_ALPHA, DEFAULT_TOLERANCE);
  }

  public FFTOptionModel(final CharacteristicExponent1 characteristicExponent, final int nStrikes, final double maxDeltaMoneyness, final double alpha, final double tolerance) {
    Validate.notNull(characteristicExponent, "characteristic exponent");
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
    final double[][] price = PRICER.price(data, option, _characteristicExponent, _nStrikes, _maxDeltaMoneyness, _alpha, _tolerance);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.FAIR_PRICE, price[0][1]);
    return result;
  }

}
