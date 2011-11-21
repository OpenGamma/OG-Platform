/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class ForexForwardFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _payFundingCurveName;
  private final String _payForwardCurveName;
  private final String _receiveFundingCurveName;
  private final String _receiveForwardCurveName;
  private ForexSecurityConverter _visitor;
  private SecuritySource _securitySource;

  public ForexForwardFunction(final String payFundingCurveName, final String payForwardCurveName, final String receiveFundingCurveName, String receiveForwardCurveName) {
    Validate.notNull(payFundingCurveName, "pay funding curve name");
    Validate.notNull(receiveFundingCurveName, "pay forward curve name");
    Validate.notNull(payFundingCurveName, "receive funding curve name");
    Validate.notNull(receiveFundingCurveName, "receive forward curve name");
    _payFundingCurveName = payFundingCurveName;
    _payForwardCurveName = payForwardCurveName;
    _receiveFundingCurveName = receiveFundingCurveName;
    _receiveForwardCurveName = receiveForwardCurveName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    _visitor = new ForexSecurityConverter(_securitySource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final FXForwardSecurity security = (FXForwardSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = _visitor.visitFXForwardSecurity(security);
    final FXSecurity fx = (FXSecurity) _securitySource.getSecurity(ExternalIdBundle.of(security.getUnderlyingId()));
    final Currency payCurrency = fx.getPayCurrency();
    final Currency receiveCurrency = fx.getReceiveCurrency();
    final String payFundingCurveName = _payFundingCurveName + "_" + payCurrency.getCode();
    final String payForwardCurveName = _payForwardCurveName + "_" + payCurrency.getCode();
    final String receiveFundingCurveName = _receiveFundingCurveName + "_" + receiveCurrency.getCode();
    final String receiveForwardCurveName = _receiveForwardCurveName + "_" + receiveCurrency.getCode();
    final String[] curveNames = new String[] {payFundingCurveName, receiveFundingCurveName};
    final Object payFundingCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(payCurrency, _payFundingCurveName, _payForwardCurveName, _payFundingCurveName));
    if (payFundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + payFundingCurveName + " curve");
    }
    final YieldAndDiscountCurve payFundingCurve = (YieldAndDiscountCurve) payFundingCurveObject;
    final Object payForwardCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(payCurrency, _payForwardCurveName, _payForwardCurveName, _payFundingCurveName));
    if (payForwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + payForwardCurveName + " curve");
    }
    final YieldAndDiscountCurve payForwardCurve = (YieldAndDiscountCurve) payForwardCurveObject;
    final Object receiveFundingCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(receiveCurrency, _receiveFundingCurveName, _receiveForwardCurveName, _receiveFundingCurveName));
    if (receiveFundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + receiveFundingCurveName + " curve");
    }
    final YieldAndDiscountCurve receiveFundingCurve = (YieldAndDiscountCurve) receiveFundingCurveObject;
    final Object receiveForwardCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(receiveCurrency, _receiveForwardCurveName, _receiveForwardCurveName, _receiveFundingCurveName));
    if (receiveForwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + receiveForwardCurveName + " curve");
    }
    final YieldAndDiscountCurve receiveForwardCurve = (YieldAndDiscountCurve) receiveForwardCurveObject;
    final YieldAndDiscountCurve[] curves = new YieldAndDiscountCurve[] {payFundingCurve, payForwardCurve, receiveFundingCurve, receiveForwardCurve};
    final Forex fxForward = (Forex) definition.toDerivative(now, curveNames);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(new String[] {payFundingCurveName, payForwardCurveName, receiveFundingCurveName, receiveForwardCurveName}, curves);
    return getResult(fxForward, yieldCurves, inputs, target);
  }

  protected abstract Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final FunctionInputs inputs, final ComputationTarget target);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXForwardSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
    final FXSecurity fx = (FXSecurity) _securitySource.getSecurity(ExternalIdBundle.of(fxForward.getUnderlyingId()));
    final ValueRequirement payFundingCurve = YieldCurveFunction.getCurveRequirement(fx.getPayCurrency(), _payFundingCurveName, _payForwardCurveName, _payFundingCurveName);
    final ValueRequirement payForwardCurve = YieldCurveFunction.getCurveRequirement(fx.getPayCurrency(), _payForwardCurveName, _payForwardCurveName, _payFundingCurveName);
    final ValueRequirement receiveFundingCurve = YieldCurveFunction.getCurveRequirement(fx.getReceiveCurrency(), _receiveFundingCurveName, _receiveForwardCurveName, _receiveFundingCurveName);
    final ValueRequirement receiveForwardCurve = YieldCurveFunction.getCurveRequirement(fx.getReceiveCurrency(), _receiveForwardCurveName, _receiveForwardCurveName, _receiveFundingCurveName);
    final ExternalId spotIdentifier = FXUtils.getSpotIdentifier(fx, true);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    return Sets.newHashSet(payFundingCurve, payForwardCurve, receiveFundingCurve, receiveForwardCurve, spotRequirement);
  }

  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

  protected String getPayFundingCurveName() {
    return _payFundingCurveName;
  }

  protected String getPayForwardCurveName() {
    return _payForwardCurveName;
  }

  protected String getReceiveFundingCurveName() {
    return _receiveFundingCurveName;
  }

  protected String getReceiveForwardCurveName() {
    return _receiveForwardCurveName;
  }

}
