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
import com.opengamma.analytics.math.integration.Integrator1D;

/**
 * 
 */
public class FourierOptionModel implements OptionModel<EuropeanVanillaOptionDefinition, BlackOptionDataBundle> {
  private static final Logger s_logger = LoggerFactory.getLogger(FourierOptionModel.class);
  private static final double DEFAULT_ALPHA = -0.5;
  private static final double DEFAULT_LIMIT_TOLERANCE = 1e-8;
  private static final boolean DEFAULT_USE_VARIANCE_REDUCTION = false;
  private final MartingaleCharacteristicExponent _characteristicExponent;
  private final FourierPricer _pricer;
  private final boolean _useVarianceReduction;
  private final double _limitTolerance;
  private final double _alpha;

  public FourierOptionModel(final MartingaleCharacteristicExponent characteristicExponent) {
    this(characteristicExponent, DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE, DEFAULT_USE_VARIANCE_REDUCTION);
  }

  public FourierOptionModel(final MartingaleCharacteristicExponent characteristicExponent, final boolean useVarianceReduction) {
    this(characteristicExponent, DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE, useVarianceReduction);
  }

  public FourierOptionModel(final MartingaleCharacteristicExponent characteristicExponent, final Integrator1D<Double, Double> integrator) {
    this(characteristicExponent, integrator, DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE, DEFAULT_USE_VARIANCE_REDUCTION);
  }

  public FourierOptionModel(final MartingaleCharacteristicExponent characteristicExponent, final Integrator1D<Double, Double> integrator, final boolean useVarianceReduction) {
    this(characteristicExponent, integrator, DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE, useVarianceReduction);
  }

  public FourierOptionModel(final MartingaleCharacteristicExponent characteristicExponent, final double alpha, final double limitTolerance, final boolean useVarianceReduction) {
    Validate.notNull(characteristicExponent, "characteristic exponent");
    Validate.isTrue(alpha != 0 && alpha != -1, "alpha cannot be equal to -1 or 0");
    Validate.isTrue(limitTolerance > 0, "limit tolerance > 0");
    _characteristicExponent = characteristicExponent;
    _pricer = new FourierPricer();
    _alpha = alpha;
    _limitTolerance = limitTolerance;
    _useVarianceReduction = useVarianceReduction;
  }

  public FourierOptionModel(final MartingaleCharacteristicExponent characteristicExponent, final Integrator1D<Double, Double> integrator, final double alpha,
      final double limitTolerance, final boolean useVarianceReduction) {
    Validate.notNull(characteristicExponent, "characteristic exponent");
    Validate.notNull(integrator, "integrator");
    Validate.isTrue(alpha != 0 && alpha != -1, "alpha cannot be equal to -1 or 0");
    Validate.isTrue(limitTolerance > 0, "limit tolerance > 0");
    _characteristicExponent = characteristicExponent;
    _pricer = new FourierPricer(integrator);
    _alpha = alpha;
    _limitTolerance = limitTolerance;
    _useVarianceReduction = useVarianceReduction;
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
    final double price = _pricer.price(data, option, _characteristicExponent, _alpha, _limitTolerance, _useVarianceReduction);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.FAIR_PRICE, price);
    return result;
  }

}
