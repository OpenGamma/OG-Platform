/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class YieldCurveFunctionHelper {
  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveFunctionHelper.class);
  
  private final Currency _currency;
  private final String _curveName;
  private InterpolatedYieldCurveSpecificationBuilder _curveSpecificationBuilder;
  private YieldCurveDefinition _definition;

  public YieldCurveFunctionHelper(Currency currency, String curveName) {
    Validate.notNull(currency, "curve currency");
    Validate.notNull(curveName, "curve name");
    _currency = currency;
    _curveName = curveName;

  }

  public YieldCurveDefinition init(final FunctionCompilationContext context, final FunctionDefinition defnToReInit) {
    _curveSpecificationBuilder = OpenGammaCompilationContext.getInterpolatedYieldCurveSpecificationBuilder(context);

    _definition = getDefinition(context);
    if (_definition != null && _definition.getUniqueId() != null) {
      context.getFunctionReinitializer().reinitializeFunction(defnToReInit, _definition.getUniqueId());
    } else {
      s_logger.warn("Curve {} on {} has no identifier - cannot subscribe to updates", _curveName, _currency);
    }
    return _definition;
  }

  public Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification> compile(
      final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    //TODO: avoid doing this compile twice all the time
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final LocalDate curveDate = atInstant.toLocalDate();
    final InterpolatedYieldCurveSpecification fundingCurveSpecification = buildCurve(curveDate);

    // ENG-252 expiry logic is wrong so make it valid for the current day only
    final Instant eod = atInstant.withTime(0, 0).plusDays(1).minusNanos(1000000).toInstant();
    Instant expiry = null;
    // expiry = findCurveExpiryDate(context.getSecuritySource(), fundingCurveSpecification, expiry);
    // expiry = findCurveExpiryDate(context.getSecuritySource(), forwardCurveSpecification, expiry);
    // if (expiry.isBefore(eod)) {
    expiry = eod;
    // }
    return new Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification>(atInstant.withTime(0, 0),
        expiry, fundingCurveSpecification);
  }

  private YieldCurveDefinition getDefinition(final FunctionCompilationContext context) {
    final InterpolatedYieldCurveDefinitionSource curveDefinitionSource = OpenGammaCompilationContext
        .getInterpolatedYieldCurveDefinitionSource(context);
    final YieldCurveDefinition definition = curveDefinitionSource.getDefinition(_currency, _curveName);
    if (definition == null) {
      s_logger.warn("No curve definition for {} on {}", _curveName, _currency);
    }
    return definition;
  }

  //ENG-252 This logic is wrong
  @SuppressWarnings("unused")
  private Instant findCurveExpiryDate(final SecuritySource securitySource,
      final InterpolatedYieldCurveSpecification specification, Instant expiry) {
    for (final FixedIncomeStripWithIdentifier strip : specification.getStrips()) {
      if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
        final FutureSecurity future = (FutureSecurity) securitySource.getSecurity(IdentifierBundle.of(strip
            .getSecurity()));
        final Instant futureInvalidAt = future.getExpiry().getExpiry().minus(strip.getMaturity().getPeriod())
            .toInstant();
        if (expiry == null) {
          expiry = futureInvalidAt;
        } else {
          if (futureInvalidAt.isBefore(expiry)) {
            expiry = futureInvalidAt;
          }
        }
      }
    }
    return expiry;
  }

  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return ObjectUtils.equals(target.getUniqueId(), _currency.getUniqueId());
  }
  
  
  public InterpolatedYieldCurveSpecification buildCurve(LocalDate curveDate) {
    return _curveSpecificationBuilder.buildCurve(curveDate, _definition);
  }
  
  public YieldCurveKey getYieldCurveKey() {
    return new YieldCurveKey(_currency, _curveName);
  }
  
  public ValueRequirement getMarketDataValueRequirement() {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, 
        new ComputationTargetSpecification(_currency),
        ValueProperties.with(ValuePropertyNames.CURVE, _curveName).get());
  }
  
  @SuppressWarnings("unchecked")
  public SnapshotDataBundle buildMarketDataMap(final FunctionInputs inputs) {
    Object marketDataBundle = inputs.getValue(getMarketDataValueRequirement());
    return (SnapshotDataBundle) marketDataBundle;
  }


}
