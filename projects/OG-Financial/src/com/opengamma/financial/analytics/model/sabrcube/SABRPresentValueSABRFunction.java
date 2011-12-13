/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.Sets;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class SABRPresentValueSABRFunction extends SABRFunction {
  private static final PresentValueSABRSensitivitySABRCalculator CALCULATOR = PresentValueSABRSensitivitySABRCalculator.getInstance();

  public SABRPresentValueSABRFunction(final String currency, final String definitionName, String forwardCurveName, String fundingCurveName) {
    this(Currency.of(currency), definitionName, forwardCurveName, fundingCurveName);
  }

  public SABRPresentValueSABRFunction(final Currency currency, final String definitionName, String forwardCurveName, String fundingCurveName) {
    super(currency, definitionName, false, forwardCurveName, fundingCurveName);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    return security instanceof SwaptionSecurity
        || (security instanceof SwapSecurity && (SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_FIXED_CMS
            || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_CMS_CMS || 
            SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_IBOR_CMS))
        || security instanceof CapFloorSecurity;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = security.accept(getVisitor());
    final SABRInterestRateDataBundle data = getModelParameters(target, inputs);
    final InstrumentDerivative derivative = getConverter().convert(security, definition, now, new String[] {getFundingCurveName(), getForwardCurveName()}, dataSource);
    final Currency ccy = FinancialSecurityUtils.getCurrency(security);
    final PresentValueSABRSensitivityDataBundle presentValue = CALCULATOR.visit(derivative, data);
    final ValueProperties resultProperties = createValueProperties().with(ValuePropertyNames.CURRENCY, ccy.getCode()).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName())
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName()).with(ValuePropertyNames.CUBE, getHelper().getDefinitionName())
        .with(ValuePropertyNames.CALCULATION_METHOD, isUseSABRExtrapolation() ? SABR_RIGHT_EXTRAPOLATION : SABR_NO_EXTRAPOLATION).get();
    final ValueSpecification alphaSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.toSpecification(), resultProperties);
    final ValueSpecification nuSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.toSpecification(), resultProperties);
    final ValueSpecification rhoSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.toSpecification(), resultProperties);
    final Map<DoublesPair, Double> alpha = presentValue.getAlpha().getMap();
    final Map<DoublesPair, Double> nu = presentValue.getNu().getMap();
    final Map<DoublesPair, Double> rho = presentValue.getRho().getMap();
    final MySecurityVisitor alphaVisitor = new MySecurityVisitor(alpha, now);
    final DoubleLabelledMatrix2D alphaValue = security.accept(alphaVisitor);
    final MySecurityVisitor nuVisitor = new MySecurityVisitor(nu, now);
    final DoubleLabelledMatrix2D nuValue = security.accept(nuVisitor);
    final MySecurityVisitor rhoVisitor = new MySecurityVisitor(rho, now);
    final DoubleLabelledMatrix2D rhoValue = security.accept(rhoVisitor);
    return Sets.newHashSet(new ComputedValue(alphaSpec, alphaValue), new ComputedValue(nuSpec, nuValue), new ComputedValue(rhoSpec, rhoValue));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties resultProperties = createValueProperties().with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName()).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
        .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).with(ValuePropertyNames.CALCULATION_METHOD, isUseSABRExtrapolation() ? SABR_RIGHT_EXTRAPOLATION : SABR_NO_EXTRAPOLATION).get();
    return getResults(target.toSpecification(), resultProperties);
  }

  private Set<ValueSpecification> getResults(final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification alphaSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, targetSpec, resultProperties);
    final ValueSpecification nuSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, targetSpec, resultProperties);
    final ValueSpecification rhoSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, targetSpec, resultProperties);
    return Sets.newHashSet(alphaSpec, nuSpec, rhoSpec);
  }

  private class MySecurityVisitor implements FinancialSecurityVisitor<DoubleLabelledMatrix2D> {
    private final DecimalFormat _formatter = new DecimalFormat("##.#");
    private final Map<DoublesPair, Double> _data;
    private final ZonedDateTime _now;

    public MySecurityVisitor(final Map<DoublesPair, Double> data, final ZonedDateTime now) {
      _data = data;
      _now = now;
    }

    @Override
    public DoubleLabelledMatrix2D visitBondSecurity(BondSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitCashSecurity(CashSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitEquitySecurity(EquitySecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitFRASecurity(FRASecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitFutureSecurity(FutureSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitSwapSecurity(SwapSecurity security) {
      final Map.Entry<DoublesPair, Double> entry = _data.entrySet().iterator().next();
      return new DoubleLabelledMatrix2D(new Double[] {entry.getKey().first}, new Double[] {entry.getKey().second}, new double[][] {new double[] {entry.getValue()}});
    }

    @Override
    public DoubleLabelledMatrix2D visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitEquityOptionSecurity(EquityOptionSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitFXOptionSecurity(FXOptionSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitSwaptionSecurity(SwaptionSecurity security) {
      final Map.Entry<DoublesPair, Double> entry = _data.entrySet().iterator().next();
      final ZonedDateTime swaptionExpiry = security.getExpiry().getExpiry();
      final SwapSecurity underlying = (SwapSecurity) getSecuritySource().getSecurity(ExternalIdBundle.of(security.getUnderlyingId()));
      final ZonedDateTime swapMaturity = underlying.getMaturityDate();
      final double swaptionExpiryYears = DateUtils.getDifferenceInYears(_now, swaptionExpiry);
      final double swapMaturityYears = DateUtils.getDifferenceInYears(_now, swapMaturity);
      return new DoubleLabelledMatrix2D(new Double[] {entry.getKey().first}, new Object[] {_formatter.format(swaptionExpiryYears)}, new Double[] {entry.getKey().second},
          new Object[] {_formatter.format(swapMaturityYears)}, new double[][] {new double[] {entry.getValue()}});
    }

    @Override
    public DoubleLabelledMatrix2D visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
      throw new NotImplementedException();
    }

    @Override
    public DoubleLabelledMatrix2D visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitFXSecurity(FXSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitFXForwardSecurity(FXForwardSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitCapFloorSecurity(CapFloorSecurity security) {
      final Map.Entry<DoublesPair, Double> entry = _data.entrySet().iterator().next();
      return new DoubleLabelledMatrix2D(new Double[] {entry.getKey().first}, new Double[] {entry.getKey().second}, new double[][] {new double[] {entry.getValue()}});
    }

    @Override
    public DoubleLabelledMatrix2D visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
      final Map.Entry<DoublesPair, Double> entry = _data.entrySet().iterator().next();
      return new DoubleLabelledMatrix2D(new Double[] {entry.getKey().first}, new Double[] {entry.getKey().second}, new double[][] {new double[] {entry.getValue()}});
    }

    @Override
    public DoubleLabelledMatrix2D visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
      return null;
    }

    @Override
    public DoubleLabelledMatrix2D visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
      return null;
    }

  }
}
