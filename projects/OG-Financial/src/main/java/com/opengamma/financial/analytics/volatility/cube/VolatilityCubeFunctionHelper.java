/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.util.money.Currency;
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
        context.getFunctionReinitializer().reinitializeFunction(defnToReInit, _definition.getUniqueId().getObjectId());
      } else {
        s_logger.warn("Cube {} on {} has no identifier - cannot subscribe to updates", _definitionName, _currency);
      }
    }
    return _definition;
  }

  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return _currency.equals(target.getValue());
  }

  public Currency getCurrency() {
    return _currency;
  }

  public String getDefinitionName() {
    return _definitionName;
  }

  public Triple<Instant, Instant, VolatilityCubeSpecification> compile(
    final FunctionCompilationContext context, final Instant atInstant) {
    //TODO: avoid doing this compile twice all the time
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final LocalDate curveDate = atZDT.getDate();
    final VolatilityCubeSpecification specification = buildSpecification(curveDate);

    // ENG-252 expiry logic is wrong so make it valid for the current day only
    final Instant eod = atZDT.with(LocalTime.MIDNIGHT).plusDays(1).minusNanos(1000000).toInstant();
    Instant expiry = null;
    // expiry = findCurveExpiryDate(context.getSecuritySource(), fundingCurveSpecification, expiry);
    // expiry = findCurveExpiryDate(context.getSecuritySource(), forwardCurveSpecification, expiry);
    // if (expiry.isBefore(eod)) {
    expiry = eod;
    // }
    return new Triple<Instant, Instant, VolatilityCubeSpecification>(atZDT.with(LocalTime.MIDNIGHT).toInstant(),
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
