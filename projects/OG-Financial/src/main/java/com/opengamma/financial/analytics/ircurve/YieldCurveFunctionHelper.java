/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class YieldCurveFunctionHelper {
  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveFunctionHelper.class);

  private final Currency _currency;
  private final String _curveName;
  private InterpolatedYieldCurveSpecificationBuilder.AtVersionCorrection _curveSpecificationBuilder;
  private YieldCurveDefinition _definition;

  public YieldCurveFunctionHelper(final Currency currency, final String curveName) {
    Validate.notNull(currency, "curve currency");
    Validate.notNull(curveName, "curve name");
    _currency = currency;
    _curveName = curveName;

  }

  public YieldCurveDefinition init(final FunctionCompilationContext context, final FunctionDefinition defnToReInit) {
    _curveSpecificationBuilder = InterpolatedYieldCurveSpecificationBuilder.AtVersionCorrection.init(context, defnToReInit);
    if (_curveSpecificationBuilder == null) {
      throw new UnsupportedOperationException("An interpolated yield curve specification builder is required");
    }
    _definition = ConfigSourceQuery.init(context, defnToReInit, YieldCurveDefinition.class).get(_curveName + "_" + _currency.getCode());
    if (_definition == null) {
      throw new UnsupportedOperationException("No curve definition for " + _curveName + " on " + _currency);
    }
    return _definition;
  }

  public Triple<Instant, Instant, InterpolatedYieldCurveSpecification> compile(final FunctionCompilationContext context, final Instant atInstant, final FunctionDefinition functionDefinition) {
    init(context, functionDefinition);

    //TODO: avoid doing this compile twice all the time
    final ZonedDateTime atInstantZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final LocalDate curveDate = atInstantZDT.toLocalDate();
    final InterpolatedYieldCurveSpecification specification = buildCurve(curveDate);
    final Instant expiry = findCurveExpiryDate(context.getSecuritySource(), atInstant, specification, atInstantZDT.with(LocalTime.MIDNIGHT).plusDays(1).minusNanos(1000000).toInstant());
    return new Triple<>((expiry != null) ? atInstantZDT.with(LocalTime.MIDNIGHT).toInstant() : null, expiry, specification);
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
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, ComputationTargetSpecification.of(_currency), ValueProperties.with(ValuePropertyNames.CURVE, _curveName).get());
  }

  public SnapshotDataBundle getMarketDataMap(final FunctionInputs inputs) {
    return (SnapshotDataBundle) inputs.getValue(getMarketDataValueRequirement());
  }

}
