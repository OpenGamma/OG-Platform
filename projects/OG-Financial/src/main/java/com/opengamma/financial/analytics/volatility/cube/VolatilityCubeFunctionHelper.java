/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.lambdava.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class VolatilityCubeFunctionHelper {

  private static final Logger s_logger = LoggerFactory.getLogger(VolatilityCubeFunctionHelper.class);

  private final Currency _currency;
  private final String _definitionName;
  private VolatilityCubeDefinition _definition;


  public VolatilityCubeFunctionHelper(Currency currency, String definitionName) {
    _definitionName = definitionName;
    _currency = currency;
  }

  public VolatilityCubeDefinition init(final FunctionCompilationContext context, final FunctionDefinition defnToReInit) {
    _definition = getDefinition(context);
    if (_definition == null) {
      s_logger.warn("No cube definition for {} on {}", _definitionName, _currency);
    } else {
      if (_definition.getUniqueId() != null) {
        context.getFunctionReinitializer().reinitializeFunction(defnToReInit, Pair.of(_definition.getUniqueId().getObjectId(), VersionCorrection.LATEST));
      } else {
        s_logger.warn("Cube {} on {} has no identifier - cannot subscribe to updates", _definitionName, _currency);
      }
    }
    return _definition;
  }

  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return ObjectUtils.equals(target.getUniqueId(), _currency.getUniqueId());
  }

  public Currency getCurrency() {
    return _currency;
  }

  public String getDefinitionName() {
    return _definitionName;
  }

  public Triple<InstantProvider, InstantProvider, VolatilityCubeSpecification> compile(
    final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    //TODO: avoid doing this compile twice all the time
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final LocalDate curveDate = atInstant.toLocalDate();
    final VolatilityCubeSpecification specification = buildSpecification(curveDate);

    // ENG-252 expiry logic is wrong so make it valid for the current day only
    final Instant eod = atInstant.withTime(0, 0).plusDays(1).minusNanos(1000000).toInstant();
    Instant expiry = null;
    // expiry = findCurveExpiryDate(context.getSecuritySource(), fundingCurveSpecification, expiry);
    // expiry = findCurveExpiryDate(context.getSecuritySource(), forwardCurveSpecification, expiry);
    // if (expiry.isBefore(eod)) {
    expiry = eod;
    // }
    return new Triple<InstantProvider, InstantProvider, VolatilityCubeSpecification>(atInstant.withTime(0, 0),
      expiry, specification);
  }

  private VolatilityCubeDefinition getDefinition(FunctionCompilationContext context) {
    final VolatilityCubeDefinitionSource defnSource = OpenGammaCompilationContext
      .getVolatilityCubeDefinitionSource(context);
    return defnSource.getDefinition(_currency, _definitionName);
  }

  private VolatilityCubeSpecification buildSpecification(LocalDate curveDate) {
    return null; //TODO this when we know how to resolve
  }

}
