/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;

/**
 * This class contains the logic for resolving a yield curve to specification and securities
 */
public abstract class MarketInstrumentImpliedYieldCurveFunctionHelper extends AbstractFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(MarketInstrumentImpliedYieldCurveFunctionHelper.class);

  private final Currency _currency;
  private final String _fundingCurveDefinitionName;
  private final String _forwardCurveDefinitionName;
  
  private YieldCurveDefinition _fundingCurveDefinition;
  private YieldCurveDefinition _forwardCurveDefinition;
  private InterpolatedYieldCurveSpecificationBuilder _curveSpecificationBuilder;

  public MarketInstrumentImpliedYieldCurveFunctionHelper(final Currency currency,
      final String fundingCurveDefinitionName, final String forwardCurveDefinitionName) {
    Validate.notNull(currency, "curve currency");
    Validate.notNull(fundingCurveDefinitionName, "funding curve name");
    Validate.notNull(forwardCurveDefinitionName, "forward curve name");
    _currency = currency;
    _fundingCurveDefinitionName = fundingCurveDefinitionName;
    _forwardCurveDefinitionName = forwardCurveDefinitionName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _fundingCurveDefinition = getDefinition(context, _fundingCurveDefinitionName);
    _forwardCurveDefinition = getDefinition(context, _forwardCurveDefinitionName);
    _curveSpecificationBuilder = OpenGammaCompilationContext.getInterpolatedYieldCurveSpecificationBuilder(context);
  }

  private YieldCurveDefinition getDefinition(final FunctionCompilationContext context, final String name) {
    final InterpolatedYieldCurveDefinitionSource curveDefinitionSource = OpenGammaCompilationContext
        .getInterpolatedYieldCurveDefinitionSource(context);
    final YieldCurveDefinition definition = curveDefinitionSource.getDefinition(_currency, name);
    if (definition == null) {
      s_logger.warn("No curve definition for {} on {}", name, _currency);
    } else {
      if (definition.getUniqueId() != null) {
        context.getFunctionReinitializer().reinitializeFunction(this, definition.getUniqueId());
      } else {
        s_logger.warn("Curve {} on {} has no identifier - cannot subscribe to updates", name, _currency);
      }
    }
    return definition;
  }

 
  @Override
  public Compiled compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    //TODO: avoid doing this compile twice all the time
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final LocalDate curveDate = atInstant.toLocalDate();
    final InterpolatedYieldCurveSpecification fundingCurveSpecification = _curveSpecificationBuilder.buildCurve(
        curveDate, _fundingCurveDefinition);
    final InterpolatedYieldCurveSpecification forwardCurveSpecification = _curveSpecificationBuilder.buildCurve(
        curveDate, _forwardCurveDefinition);
    final Set<ValueRequirement> fundingCurveRequirements = buildRequirements(fundingCurveSpecification, context);
    final Set<ValueRequirement> forwardCurveRequirements = buildRequirements(forwardCurveSpecification, context);
    // ENG-252 expiry logic is wrong so make it valid for the current day only
    final Instant eod = atInstant.withTime(0, 0).plusDays(1).minusNanos(1000000).toInstant();
    Instant expiry = null;
    // expiry = findCurveExpiryDate(context.getSecuritySource(), fundingCurveSpecification, expiry);
    // expiry = findCurveExpiryDate(context.getSecuritySource(), forwardCurveSpecification, expiry);
    // if (expiry.isBefore(eod)) {
    expiry = eod;
    // }
    return compileImpl(atInstant.withTime(0, 0), expiry,
        fundingCurveSpecification, fundingCurveRequirements, forwardCurveSpecification, forwardCurveRequirements);
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

  protected abstract Compiled compileImpl(final InstantProvider earliest, final InstantProvider latest,
      InterpolatedYieldCurveSpecification fundingCurveSpecification, Set<ValueRequirement> fundingCurveRequirements,
      InterpolatedYieldCurveSpecification forwardCurveSpecification, Set<ValueRequirement> forwardCurveRequirements);

  /**
  *
  */
  public abstract class Compiled extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final Currency _targetCurrency;
    private final Set<ValueRequirement> _fundingCurveRequirements;
    private final Set<ValueRequirement> _forwardCurveRequirements;
    
    protected Compiled(final InstantProvider earliest, final InstantProvider latest, final Currency targetCurrency,
        final Set<ValueRequirement> fundingCurveRequirements, final Set<ValueRequirement> forwardCurveRequirements) {
      super(earliest, latest);
      _targetCurrency = targetCurrency;
      _fundingCurveRequirements = fundingCurveRequirements;
      _forwardCurveRequirements = forwardCurveRequirements;
    }

    @Override
    public abstract Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
        final ComputationTarget target, final Set<ValueRequirement> desiredValues);

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      if (target.getType() != ComputationTargetType.PRIMITIVE) {
        return false;
      }
      return ObjectUtils.equals(target.getUniqueId(), _targetCurrency.getUniqueId());
    }

    @Override
    public abstract Set<ValueRequirement> getRequirements(final FunctionCompilationContext context,
        final ComputationTarget target, final ValueRequirement desiredValue);

    @Override
    public abstract Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target);

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE;
    }

    protected Currency getTargetCurrency() {
      return _targetCurrency;
    }

    protected Set<ValueRequirement> getFundingCurveRequirements() {
      return _fundingCurveRequirements;
    }

    protected Set<ValueRequirement> getForwardCurveRequirements() {
      return _forwardCurveRequirements;
    }

    
  }

  
  public static Set<ValueRequirement> buildRequirements(final InterpolatedYieldCurveSpecification specification,
      final FunctionCompilationContext context) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final FixedIncomeStripWithIdentifier strip : specification.getStrips()) {
      result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, strip.getSecurity()));
    }
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext
        .getConventionBundleSource(context);
    final ConventionBundle conventionBundle = conventionBundleSource.getConventionBundle(Identifier.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, specification.getCurrency().getCode() + "_SWAP"));
    final ConventionBundle referenceRateConvention = conventionBundleSource.getConventionBundle(IdentifierBundle
        .of(conventionBundle.getSwapFloatingLegInitialRate()));
    final Identifier initialRefRateId = SecurityUtils.bloombergTickerSecurityId(referenceRateConvention
        .getIdentifiers().getIdentifier(SecurityUtils.BLOOMBERG_TICKER));
    result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, initialRefRateId));
    return Collections.unmodifiableSet(result);
  }
  
  @Override
  public String getShortName() {
    return "[" + _fundingCurveDefinitionName + ", " + _forwardCurveDefinitionName + ", " + _currency + "]"
        + "_MarketInstrumentImpliedYieldCurveMarketDataFunctionHelper";
  }

  protected YieldCurveDefinition getFundingCurveDefinition() {
    return _fundingCurveDefinition;
  }

  protected void setFundingCurveDefinition(YieldCurveDefinition fundingCurveDefinition) {
    _fundingCurveDefinition = fundingCurveDefinition;
  }

  protected YieldCurveDefinition getForwardCurveDefinition() {
    return _forwardCurveDefinition;
  }

  protected void setForwardCurveDefinition(YieldCurveDefinition forwardCurveDefinition) {
    _forwardCurveDefinition = forwardCurveDefinition;
  }

  protected InterpolatedYieldCurveSpecificationBuilder getCurveSpecificationBuilder() {
    return _curveSpecificationBuilder;
  }

  protected void setCurveSpecificationBuilder(InterpolatedYieldCurveSpecificationBuilder curveSpecificationBuilder) {
    _curveSpecificationBuilder = curveSpecificationBuilder;
  }

  protected Currency getCurrency() {
    return _currency;
  }

  protected String getFundingCurveDefinitionName() {
    return _fundingCurveDefinitionName;
  }

  protected String getForwardCurveDefinitionName() {
    return _forwardCurveDefinitionName;
  }

}
