/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.MissingInput;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Function that computes the profit or loss since previous close,
 * as defined by {@link ValueRequirementNames#HISTORICAL_TIME_SERIES_LATEST}. This will get most recent closing price before today.
 * By intention, this will not be today's close even if it's available. Note that this may be stale, if time series aren't updated nightly, as we take latest value.
 * Illiquid securities do not trade each day..
 * As the name MarkToMarket implies, this simple Function applies to Trades on Exchange-Traded Securities.
 */
public class MarkToMarketPnLFunction extends AbstractFunction.NonCompiledInvoker {

  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MarkToMarketPnLFunction.class);

  private final String _costOfCarryField;
  private final String _closingPriceField;

  public MarkToMarketPnLFunction(final String closingPriceField, final String costOfCarryField) {
    ArgumentChecker.notNull(costOfCarryField, "costOfCarryField");
    ArgumentChecker.notNull(closingPriceField, "closingPriceField");
    _closingPriceField = closingPriceField;
    _costOfCarryField = costOfCarryField;
  }

  protected String getValueRequirementName() {
    return ValueRequirementNames.MTM_PNL;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {

    final Security security = target.getPositionOrTrade().getSecurity();
    if (FXUtils.isFXSecurity(security)) {
      return false;
    }
    return FinancialSecurityUtils.isExchangeTraded(security) || (security instanceof BondSecurity); // See SecurityMarketValueFunction
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext,
                                    final FunctionInputs inputs,
                                    final ComputationTarget target,
                                    final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // 1. Unpack
    final Trade trade = target.getTrade();
    final Security security = trade.getSecurity();
    final LocalDate tradeDate = trade.getTradeDate();
    final LocalDate valuationDate = ZonedDateTime.now(executionContext.getValuationClock()).toLocalDate();
    final boolean isNewTrade = tradeDate.equals(valuationDate);

    // Get desired TradeType: Open (traded before today), New (traded today) or All
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String tradeType = desiredValue.getConstraint(PnLFunctionUtils.PNL_TRADE_TYPE_CONSTRAINT);
    if (tradeType == null) {
      throw new OpenGammaRuntimeException("TradeType not set for: " + security.getName() +
          ". Choose one of {" + PnLFunctionUtils.PNL_TRADE_TYPE_OPEN + "," + PnLFunctionUtils.PNL_TRADE_TYPE_NEW + "," + PnLFunctionUtils.PNL_TRADE_TYPE_ALL + "}");
    }

    // Create output specification. Check for trivial cases
    final ValueSpecification valueSpecification = new ValueSpecification(getValueRequirementName(), target.toSpecification(), desiredValue.getConstraints());
    if (isNewTrade && tradeType.equalsIgnoreCase(PnLFunctionUtils.PNL_TRADE_TYPE_OPEN) ||
        (!isNewTrade) && tradeType.equalsIgnoreCase(PnLFunctionUtils.PNL_TRADE_TYPE_NEW)) {
      return Sets.newHashSet(new ComputedValue(valueSpecification, 0.0));
    }

    // 2. Get inputs
    // For all TradeTypes, we'll require the live Price
    final Double livePrice = calculateLivePrice(inputs, target);

    // For PNL, we need a reference price. We have two cases:
    // Open: will need the closing price and any carry
    // New: will need the trade price
    Double referencePrice;
    Double costOfCarry = 0.0;

    if (isNewTrade) {
      referencePrice = trade.getPremium();
      if (referencePrice == null) {
        throw new NullPointerException("New Trades require a premium to compute PNL on trade date. Premium was null for " + trade.getUniqueId());
      }
      if ((security instanceof InterestRateFutureSecurity || security instanceof IRFutureOptionSecurity) && (trade.getPremium() > 1.0)) {
        referencePrice /= 100.0;
      }
    } else {
      referencePrice = calculateReferencePrice(inputs, target);
      if (referencePrice == null) {
        final ComputedValue result = new ComputedValue(valueSpecification, MissingInput.MISSING_MARKET_DATA);
        return Sets.newHashSet(result);
      }
      final Object carryValue = inputs.getValue(_costOfCarryField);
      if (carryValue != null) {
        costOfCarry = (Double) carryValue;
      }
    }
    // 3. Compute the PNL
    // Move in the marked prices: Live - Previous Close
    final Double dailyPriceMove = livePrice - referencePrice;
    // Total move := Value
    Double dailyValueMove = dailyPriceMove - costOfCarry;

    // 4. Scale by Trade Notionals and Quantity
    // Some SecurityType's have Notional values built-in. Scale by these if required.
    if (security instanceof FutureSecurity) {
      final FutureSecurity futureSecurity = (FutureSecurity) security;
      dailyValueMove *= futureSecurity.getUnitAmount();
    } else if (security instanceof EquityOptionSecurity) {
      final EquityOptionSecurity optionSecurity = (EquityOptionSecurity) security;
      dailyValueMove *= optionSecurity.getPointValue();
    } else if (security instanceof EquityIndexOptionSecurity) {
      final EquityIndexOptionSecurity optionSecurity = (EquityIndexOptionSecurity) security;
      dailyValueMove *= optionSecurity.getPointValue();
    } else if (security instanceof EquityIndexFutureOptionSecurity) {
      final EquityIndexFutureOptionSecurity optionSecurity = (EquityIndexFutureOptionSecurity) security;
      dailyValueMove *= optionSecurity.getPointValue();
    } else if (security instanceof IRFutureOptionSecurity) {
      final IRFutureOptionSecurity optionSecurity = (IRFutureOptionSecurity) security;
      dailyValueMove *= optionSecurity.getPointValue();
    }
    // Multiply by the Trade's Quantity
    final Double dailyPnL = target.getTrade().getQuantity().doubleValue() * dailyValueMove;

    // 5. Return
    final ComputedValue result = new ComputedValue(valueSpecification, dailyPnL);
    return Sets.newHashSet(result);
  }


  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties();
    properties.withAny(PnLFunctionUtils.PNL_TRADE_TYPE_CONSTRAINT);
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getPositionOrTrade().getSecurity());
    if (ccy != null) {
      properties.with(ValuePropertyNames.CURRENCY, ccy.getCode());
    }
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getValueRequirementName(),
                                                        target.toSpecification(),
                                                        createValueProperties(target).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    final Security security = target.getPositionOrTrade().getSecurity();
    requirements.addAll(createLivePriceRequirement(security));
    requirements.addAll(createReferencePriceRequirement(security));
    if (_costOfCarryField.length() > 0) {     // Cost of Carry, if provided
      requirements.add(HistoricalTimeSeriesFunctionUtils.createHTSLatestRequirement(security, _costOfCarryField, null));
    }
    return requirements;
  }

  /**
   * @param security the target's security
   * @return Engine Function requirements for the current / live price
   */
  protected Set<ValueRequirement> createLivePriceRequirement(final Security security) {
    final ComputationTargetReference securityTarget = new ComputationTargetSpecification(ComputationTargetType.SECURITY, security.getUniqueId());
    final ValueRequirement securityValueReq = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, securityTarget);
    return Collections.singleton(securityValueReq);
  }

  /**
   * @param security the target's security
   * @return Engine Function requirements for the closing / reference price
   */
  protected Set<ValueRequirement> createReferencePriceRequirement(final Security security) {
    final ValueRequirement htsReq =
        HistoricalTimeSeriesFunctionUtils.createHTSLatestRequirement(security, getClosingPriceField(), null);
    return Collections.singleton(htsReq);
  }

  // Provides the current / live price
  protected Double calculateLivePrice(final FunctionInputs inputs, final ComputationTarget target) {
    final ComputedValue valLivePrice = inputs.getComputedValue(MarketDataRequirementNames.MARKET_VALUE);
    if (valLivePrice == null) {
      throw new OpenGammaRuntimeException(MarketDataRequirementNames.MARKET_VALUE + " not available," + target.getTrade().getSecurity().getName());
    }
    return (Double) valLivePrice.getValue();
  }

  // Provides the closing / reference price
  protected Double calculateReferencePrice(final FunctionInputs inputs, final ComputationTarget target) {
    for (final ComputedValue input : inputs.getAllValues()) {
      if (input.getSpecification().getValueName().equals(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST)) {
        final String field = input.getSpecification().getProperty(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
        if (field.equals(getClosingPriceField())) {
          final Object value = input.getValue();
          if (value == null) {
            return null;
          }
          return (Double) value;
        }
      }
    }
    return null;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }

  protected String getClosingPriceField() {
    return _closingPriceField;
  }
}
