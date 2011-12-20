/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
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

  public YieldCurveFunctionHelper(final Currency currency, final String curveName) {
    Validate.notNull(currency, "curve currency");
    Validate.notNull(curveName, "curve name");
    _currency = currency;
    _curveName = curveName;

  }

  public YieldCurveDefinition init(final FunctionCompilationContext context, final FunctionDefinition defnToReInit) {
    _curveSpecificationBuilder = OpenGammaCompilationContext.getInterpolatedYieldCurveSpecificationBuilder(context);

    _definition = getDefinition(context);
    if (_definition == null) {
      s_logger.warn("No curve definition for {} on {}", _curveName, _currency);
    } else {
      if (_definition.getUniqueId() != null) {
        context.getFunctionReinitializer().reinitializeFunction(defnToReInit, _definition.getUniqueId());
      } else {
        s_logger.warn("Curve {} on {} has no identifier - cannot subscribe to updates", _curveName, _currency);
      }
    }
    return _definition;
  }

  public Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification> compile(
      final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    //TODO: avoid doing this compile twice all the time
    final Instant atInstant = Instant.of(atInstantProvider);
    final ZonedDateTime atInstantZDT = ZonedDateTime.ofInstant(atInstant, TimeZone.UTC);
    final LocalDate curveDate = atInstantZDT.toLocalDate();
    final InterpolatedYieldCurveSpecification specification = buildCurve(curveDate);
    Instant expiry = findCurveExpiryDate(context.getSecuritySource(), atInstant, specification, atInstantZDT.withTime(0, 0).plusDays(1).minusNanos(1000000).toInstant());
    return new Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification>((expiry != null) ? atInstantZDT.withTime(0, 0) : null, expiry, specification);
  }

  private YieldCurveDefinition getDefinition(final FunctionCompilationContext context) {
    final InterpolatedYieldCurveDefinitionSource curveDefinitionSource = OpenGammaCompilationContext
        .getInterpolatedYieldCurveDefinitionSource(context);
    return curveDefinitionSource.getDefinition(_currency, _curveName);
  }

  private Instant findCurveExpiryDate(final SecuritySource securitySource, final Instant curveDate, final InterpolatedYieldCurveSpecification specification, final Instant eod) {
    // ENG-252; logic is wrong so always go for EOD
    return eod;
    /*
    boolean useEOD = false;
    for (final FixedIncomeStripWithIdentifier strip : specification.getStrips()) {
      if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
        if (strip.getNumberOfFuturesAfterTenor() == 1) {
          //ENG-252 This logic may be wrong
          final FutureSecurity future = (FutureSecurity) securitySource.getSecurity(ExternalIdBundle.of(strip.getSecurity()));
          final Instant futureExpiry = future.getExpiry().toInstant();
          final Instant tenor = curveDate.plus(strip.getMaturity().getPeriod().toDuration());
          // Duration ahead of the tenor that the first future expires. The curve is valid for this duration - after
          // this, the first future to expire will be something else.
          final Duration d = Duration.between(tenor, futureExpiry);
          final Instant expiry = curveDate.plus(d);
          if (expiry.isBefore(eod)) {
            return eod;
          } else {
            return expiry;
          }
        }
        useEOD = true;
      }
    }
    // useEOD is set if there are futures but not the first after a tenor that we can calculate the expiry from
    return useEOD ? eod : null;
    */
  }

  public boolean canApplyTo(final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return ObjectUtils.equals(target.getUniqueId(), _currency.getUniqueId());
  }
  
  public Currency getCurrency() {
    return _currency;
  }
  
  public String getCurveName() {
    return _curveName;
  }

  public InterpolatedYieldCurveSpecification buildCurve(final LocalDate curveDate) {
    return _curveSpecificationBuilder.buildCurve(curveDate, _definition);
  }

  public ValueRequirement getMarketDataValueRequirement() {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA,
        new ComputationTargetSpecification(_currency),
        ValueProperties.with(ValuePropertyNames.CURVE, _curveName).get());
  }

  public Map<ExternalId, Double> buildMarketDataMap(final FunctionInputs inputs) {
    final SnapshotDataBundle marketDataBundle = (SnapshotDataBundle) inputs.getValue(getMarketDataValueRequirement());
    return buildMarketDataMap(marketDataBundle);
  }

  public static Map<ExternalId, Double> buildMarketDataMap(final SnapshotDataBundle marketDataBundle) {
    final Map<UniqueId, Double> dataPoints = marketDataBundle.getDataPoints();
    final HashMap<ExternalId, Double> ret = new HashMap<ExternalId, Double>();
    for (Entry<UniqueId, Double> entry : dataPoints.entrySet()) {
      final UniqueId uid = entry.getKey();
      final ExternalId identifier = getIdentifier(uid);
      ret.put(identifier, entry.getValue());
    }
    return ret;
  }

  private static ExternalId getIdentifier(UniqueId uid) {
    ExternalId identifier = new ComputationTargetSpecification(ComputationTargetType.SECURITY, uid).getIdentifier(); // TODO hack after PLAT-966, should the analytics be using UIDs?
    return identifier;
  }

}
