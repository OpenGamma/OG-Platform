/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
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
import com.opengamma.financial.analytics.conversion.EquityFutureConverter;
import com.opengamma.financial.equity.future.EquityFutureDataBundle;
import com.opengamma.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.financial.equity.future.derivative.EquityFuture;
import com.opengamma.financial.equity.future.pricing.EquityFuturePricerFactory;
import com.opengamma.financial.equity.future.pricing.EquityFuturesPricer;
import com.opengamma.financial.equity.future.pricing.EquityFuturesPricingMethod;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;

/**
 * This function will produce all valueRequirements that the EquityFutureSecurity offers.
 * A trade may produce additional generic ones, e.g. date and number of contracts..  
 */
public class EquityFuturesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final String DIVIDEND_YIELD_FIELD = "EQY_DVD_YLD_EST";

  private final String _valueRequirementName;
  private final EquityFuturesPricingMethod _pricingMethod;
  private final String _fundingCurveName;
  private EquityFutureConverter _financialToAnalyticConverter;
  private final EquityFuturesPricer _pricer;
  private final String _pricingMethodName;

  /**
  * @param valueRequirementName String describes the value requested 
  * @param pricingMethodName String corresponding to enum EquityFuturesPricingMethod {MARK_TO_MARKET or COST_OF_CARRY, DIVIDEND_YIELD}
  * @param fundingCurveName The name of the curve that will be used for discounting 
  */
  public EquityFuturesFunction(final String valueRequirementName, final String pricingMethodName, final String fundingCurveName) {
    Validate.notNull(valueRequirementName, "value requirement name");
    Validate.notNull(pricingMethodName, "pricing method name");
    Validate.notNull(fundingCurveName, "funding curve name");
    Validate.isTrue(valueRequirementName.equals(ValueRequirementNames.PRESENT_VALUE)
            || valueRequirementName.equals(ValueRequirementNames.VALUE_RHO)
            || valueRequirementName.equals(ValueRequirementNames.PV01)
            || valueRequirementName.equals(ValueRequirementNames.VALUE_DELTA),
            "EquityFuturesFunction provides the following values PRESENT_VALUE, VALUE_DELTA, VALUE_RHO and PV01. Please choose one.");

    _valueRequirementName = valueRequirementName;

    Validate.isTrue(pricingMethodName.equals(EquityFuturePricerFactory.MARK_TO_MARKET)
                 || pricingMethodName.equals(EquityFuturePricerFactory.COST_OF_CARRY)
                 || pricingMethodName.equals(EquityFuturePricerFactory.DIVIDEND_YIELD),
        "OG-Analytics provides the following pricing methods for EquityFutureSecurity: MARK_TO_MARKET, DIVIDEND_YIELD and COST_OF_CARRY. Please choose one.");

    _pricingMethod = EquityFuturesPricingMethod.valueOf(pricingMethodName);
    _pricingMethodName = pricingMethodName;
    _fundingCurveName = fundingCurveName;
    _pricer = EquityFuturePricerFactory.getMethod(pricingMethodName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _financialToAnalyticConverter = new EquityFutureConverter();
  }

  @Override
  /**
   * @param target The ComputationTarget is a TradeImpl
   */
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final SimpleTrade trade = (SimpleTrade) target.getTrade();
    final EquityFutureSecurity security = (EquityFutureSecurity) trade.getSecurity();

    final ZonedDateTime valuationTime = executionContext.getValuationClock().zonedDateTime();

    final Double lastMarginPrice = getLatestValueFromTimeSeries(HistoricalTimeSeriesFields.LAST_PRICE, executionContext, security.getExternalIdBundle(), now);
    trade.setPremium(lastMarginPrice); // TODO !!! Issue of futures and margining

    // Build the analytic's version of the security - the derivative    
    final EquityFutureDefinition definition = _financialToAnalyticConverter.visitEquityFutureTrade(trade);
    final EquityFuture derivative = definition.toDerivative(valuationTime);

    // Build the DataBundle it requires
    final EquityFutureDataBundle dataBundle;
    switch (_pricingMethod) {
      case MARK_TO_MARKET:
        Double marketPrice = getMarketPrice(security, inputs);
        dataBundle = new EquityFutureDataBundle(null, marketPrice, null, null, null);
        break;
      case COST_OF_CARRY:
        Double costOfCarry = getCostOfCarry(security, inputs);
        Double spotUnderlyer = getSpot(security, inputs);
        dataBundle = new EquityFutureDataBundle(null, null, spotUnderlyer, null, costOfCarry);
        break;
      case DIVIDEND_YIELD:
        Double spot = getSpot(security, inputs);
        Double dividendYield = getLatestValueFromTimeSeries(DIVIDEND_YIELD_FIELD, executionContext, ExternalIdBundle.of(security.getUnderlyingId()), now);
        dividendYield /= 100.0;
        YieldAndDiscountCurve fundingCurve = getYieldCurve(security, inputs);
        dataBundle = new EquityFutureDataBundle(fundingCurve, null, spot, dividendYield, null);
        break;
      default:
        throw new OpenGammaRuntimeException("Unhandled pricingMethod");
    }

    // Call OG-Analytics
    return getComputedValue(derivative, dataBundle, trade);
  }

  /**
   * Given _valueRequirement and _pricingMethod supplied, this calls to OG-Analytics. 
   * @return Call to the Analytics to get the value required
   */
  private Set<ComputedValue> getComputedValue(EquityFuture derivative, EquityFutureDataBundle bundle, SimpleTrade trade) {

    final double nContracts = trade.getQuantity().doubleValue();
    final double valueItself;

    final EquityFutureSecurity security = (EquityFutureSecurity) trade.getSecurity();
    final ValueSpecification specification = getValueSpecification(_valueRequirementName, security);

    if (_valueRequirementName.equals(ValueRequirementNames.PRESENT_VALUE)) {
      valueItself = _pricer.presentValue(derivative, bundle);
    } else if (_valueRequirementName.equals(ValueRequirementNames.VALUE_DELTA)) {
      valueItself = _pricer.spotDelta(derivative, bundle);
    } else if (_valueRequirementName.equals(ValueRequirementNames.VALUE_RHO)) {
      valueItself = _pricer.ratesDelta(derivative, bundle);
    } else if (_valueRequirementName.equals(ValueRequirementNames.PV01)) {
      valueItself = _pricer.pv01(derivative, bundle);
    } else {
      throw new OpenGammaRuntimeException("_valueRequirementName," + _valueRequirementName + ", unexpected. Should have been recognized in the constructor.");
    }
    return Collections.singleton(new ComputedValue(specification, nContracts * valueItself));

  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    return target.getTrade().getSecurity() instanceof com.opengamma.financial.security.future.EquityFutureSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {

    final SimpleTrade trade = (SimpleTrade) target.getTrade();
    final EquityFutureSecurity security = (EquityFutureSecurity) trade.getSecurity();

    switch (_pricingMethod) {
      case MARK_TO_MARKET:
        return Collections.singleton(getMarketPriceRequirement(security));

      case COST_OF_CARRY:
        return Sets.newHashSet(getSpotAssetRequirement(security), getCostOfCarryRequirement(security));

      case DIVIDEND_YIELD:
        return Sets.newHashSet(getSpotAssetRequirement(security), getDiscountCurveRequirement(security));
    }

    throw new OpenGammaRuntimeException("Unhandled _pricingMethod!");
  }

  private ValueRequirement getDiscountCurveRequirement(EquityFutureSecurity security) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, _fundingCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, security.getCurrency().getUniqueId(), properties);
  }

  private YieldAndDiscountCurve getYieldCurve(EquityFutureSecurity security, FunctionInputs inputs) {

    final ValueRequirement curveRequirement = getDiscountCurveRequirement(security);
    final Object curveObject = inputs.getValue(curveRequirement);
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveRequirement);
    }
    return (YieldAndDiscountCurve) curveObject;
  }

  private ValueRequirement getDividendYieldRequirement(EquityFutureSecurity security) {
    ExternalId id = security.getUnderlyingId();
    return new ValueRequirement(MarketDataRequirementNames.DIVIDEND_YIELD, id);
  }

  @SuppressWarnings("unused")
  private Double getDividendYield(EquityFutureSecurity security, FunctionInputs inputs) {
    ValueRequirement dividendRequirement = getDividendYieldRequirement(security);
    final Object dividendObject = inputs.getValue(dividendRequirement);
    if (dividendObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + dividendRequirement);
    }
    return (Double) dividendObject;
  }

  private ValueRequirement getSpotAssetRequirement(EquityFutureSecurity security) {
    ValueRequirement req = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, security.getUnderlyingId());
    return req;
  }

  private Double getSpot(EquityFutureSecurity security, FunctionInputs inputs) {
    ValueRequirement spotRequirement = getSpotAssetRequirement(security);
    final Object spotObject = inputs.getValue(spotRequirement);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + spotRequirement);
    }
    return (Double) spotObject;
  }

  private ValueRequirement getCostOfCarryRequirement(EquityFutureSecurity security) {
    return new ValueRequirement(MarketDataRequirementNames.COST_OF_CARRY, security.getUnderlyingId());
  }

  private Double getCostOfCarry(EquityFutureSecurity security, FunctionInputs inputs) {
    ValueRequirement costOfCarryRequirement = getCostOfCarryRequirement(security);
    final Object costOfCarryObject = inputs.getValue(costOfCarryRequirement);
    if (costOfCarryObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + costOfCarryRequirement);
    }
    return (Double) costOfCarryObject;
  }

  private ValueRequirement getMarketPriceRequirement(EquityFutureSecurity security) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueId());
  }

  private Double getMarketPrice(EquityFutureSecurity security, FunctionInputs inputs) {
    ValueRequirement marketPriceRequirement = getMarketPriceRequirement(security);
    final Object marketPriceObject = inputs.getValue(marketPriceRequirement);
    if (marketPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + marketPriceRequirement);
    }
    return (Double) marketPriceObject;
  }

  /**
   *  Returns the latest value of the historical time series keyed by idBundle and field. 
   */
  private Double getLatestValueFromTimeSeries(final String field, final FunctionExecutionContext executionContext, final ExternalIdBundle idBundle, final ZonedDateTime now) {
    final ZonedDateTime startDate = now.minusDays(7);
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final HistoricalTimeSeries ts = dataSource.getHistoricalTimeSeries(field, idBundle, null, null, startDate.toLocalDate(), true, now.toLocalDate(), true);

    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get " + field + " time series for " + idBundle.toString());
    }
    return ts.getTimeSeries().getLatestValue();
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(getValueSpecification(_valueRequirementName, target.getTrade().getSecurity()));
  }

  /** Create a ValueSpecification, the meta data for the value itself.
   * @param equityFuture 
   * @param equityFutureSecurity The OG_Financial Security
   */
  private ValueSpecification getValueSpecification(final String valueRequirementName, final Security equityFutureSecurity) {

    final ValueRequirement valueReq = new ValueRequirement(valueRequirementName, equityFutureSecurity);
    final Currency ccy = FinancialSecurityUtils.getCurrency(equityFutureSecurity);

    final ValueProperties.Builder properties = createValueProperties();
    final ValueProperties valueProps = properties
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE, _fundingCurveName)
        .with(ValuePropertyNames.CALCULATION_METHOD, _pricingMethodName)
        .get();

    return new ValueSpecification(valueReq, valueProps);
  }
}
