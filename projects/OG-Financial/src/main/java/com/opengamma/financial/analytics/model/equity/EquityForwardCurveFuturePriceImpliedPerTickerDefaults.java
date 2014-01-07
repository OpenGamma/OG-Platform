/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * Populate {@link EquityForwardCurveFromFutureCurveFunction} with defaults for tickers provided.<p>
 * The intention is that, for Tickers (Symbols) with futures traded on them,  the Forward Curve will be bootstrapped from these.
 */
public class EquityForwardCurveFuturePriceImpliedPerTickerDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityForwardCurveFuturePriceImpliedPerTickerDefaults.class);
  /** The value requirements for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FORWARD_CURVE,
    ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA
  };
  /** The priority of this set of defaults */
  private final PriorityClass _priority;
  /** Map from ticker to curve configuration, curve name and currency */
  private final Map<String, String[]> _perTickerConfig;

  /**
   * @param priority The priority, not null
   * @param perTickerConfig The default values per equity, not null
   */
  public EquityForwardCurveFuturePriceImpliedPerTickerDefaults(final String priority, final String... perTickerConfig) {
    super(ComputationTargetType.PRIMITIVE, true); // REVIEW Andrew 2012-11-06 -- Is PRIMITIVE correct, shouldn't it be SECURITY or even EquitySecurity?
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perTickerConfig, "per ticker config");
    final int nPairs = perTickerConfig.length;
    ArgumentChecker.isTrue(nPairs % 6 == 0, "Must have a single curve, forward curve calculation and instrument name per ticker");
    _priority = PriorityClass.valueOf(priority);
    _perTickerConfig = new HashMap<>();
    for (int i = 0; i < perTickerConfig.length; i += 6) {
      final String[] config = new String[] {perTickerConfig[i + 1], perTickerConfig[i + 2], perTickerConfig[i + 3], perTickerConfig[i + 4], perTickerConfig[i + 5]};
      _perTickerConfig.put(perTickerConfig[i].toUpperCase(), config);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getValue() instanceof ExternalIdentifiable)) {
      return false;
    }
    ExternalId id = ((ExternalIdentifiable) target.getValue()).getExternalId();
    final String ticker = EquitySecurityUtils.getIndexOrEquityName(id);
    if (ticker == null) {
      return false;
    }
    return _perTickerConfig.containsKey(ticker.toUpperCase());
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    // Properties For all ValueRequirement's
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
    }  
    // Properties specific to FORWARD_CURVE
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.CURVE);
    //  Properties specific to STANDARD_VOLATILITY_SURFACE_DATA
    defaults.addValuePropertyName(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ValuePropertyNames.FORWARD_CURVE_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ValuePropertyNames.CURVE_CURRENCY);
    defaults.addValuePropertyName(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ValuePropertyNames.DISCOUNTING_CURVE_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    defaults.addValuePropertyName(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ValuePropertyNames.SURFACE);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    final String tickerId = EquitySecurityUtils.getIndexOrEquityName(((ExternalIdentifiable) target.getValue()).getExternalId());
    if (!_perTickerConfig.containsKey(tickerId)) {
      s_logger.error("Could not get config for ticker " + tickerId + "; should never happen");
      return null;
    }
    final String[] config = _perTickerConfig.get(tickerId);
    switch (propertyName) {
      case ValuePropertyNames.CURVE:
        return Collections.singleton(config[0]);
      case ValuePropertyNames.FORWARD_CURVE_NAME:
        return Collections.singleton(config[0]);
      case ValuePropertyNames.CURVE_CURRENCY:
        return Collections.singleton(config[1]);
      case ValuePropertyNames.DISCOUNTING_CURVE_NAME:
        return Collections.singleton(config[2]);
      case ValuePropertyNames.CURVE_CALCULATION_CONFIG:
        return Collections.singleton(config[3]);
      case ValuePropertyNames.SURFACE:
        return Collections.singleton(config[4]);
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD:
        return Collections.singleton(ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD);
      case InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE:
        return Collections.singleton(InstrumentTypeProperties.EQUITY_FUTURE_PRICE);
      default:
        s_logger.error("Cannot get a default value for {}", propertyName);
        return null;
    }
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_FORWARD_CURVE_DEFAULTS;
  }

}
