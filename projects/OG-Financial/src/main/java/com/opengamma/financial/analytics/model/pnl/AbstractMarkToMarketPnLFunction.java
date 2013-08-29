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
 * Function that computes the profit or loss since previous close.<p>
 * As the name MarkToMarket implies, this simple Function applies to Trades on Exchange-Traded Securities.<p>
 * This class has abstract methods to deal with the previous closing price as it can be derived in different ways
 * depending on what market data is available from the provider.
 */
public abstract class AbstractMarkToMarketPnLFunction extends AbstractFunction.NonCompiledInvoker {

  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractMarkToMarketPnLFunction.class);

  private final String _costOfCarryField;

  public AbstractMarkToMarketPnLFunction(String costOfCarryField) {
    ArgumentChecker.notNull(costOfCarryField, "costOfCarryField");
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
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {

    final Security security = target.getPositionOrTrade().getSecurity();
    if (FXUtils.isFXSecurity(security)) {
      return false;
    }
    return FinancialSecurityUtils.isExchangeTraded(security) || (security instanceof BondSecurity); // See SecurityMarketValueFunction
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext,
                                    FunctionInputs inputs,
                                    ComputationTarget target,
                                    Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // 1. Unpack
    final Trade trade = target.getTrade();
    final Security security = trade.getSecurity();
    final LocalDate tradeDate = trade.getTradeDate();
    LocalDate valuationDate = ZonedDateTime.now(executionContext.getValuationClock()).toLocalDate();
    final boolean isNewTrade = tradeDate.equals(valuationDate);

    // Get desired TradeType: Open (traded before today), New (traded today) or All
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String tradeType = desiredValue.getConstraint(PnLFunctionUtils.PNL_TRADE_TYPE_CONSTRAINT);
    if (tradeType == null) {
      s_logger.error("TradeType not set for: " + security.getName() +
          ". Choose one of {" + PnLFunctionUtils.PNL_TRADE_TYPE_OPEN + "," + PnLFunctionUtils.PNL_TRADE_TYPE_OPEN + "," + PnLFunctionUtils.PNL_TRADE_TYPE_ALL + "}");
    }

    // Create output specification. Check for trivial cases
    final ValueSpecification valueSpecification = new ValueSpecification(getValueRequirementName(), target.toSpecification(), desiredValue.getConstraints());
    if (isNewTrade && tradeType.equalsIgnoreCase(PnLFunctionUtils.PNL_TRADE_TYPE_OPEN) ||
        (!isNewTrade) && tradeType.equalsIgnoreCase(PnLFunctionUtils.PNL_TRADE_TYPE_NEW)) {
      return Sets.newHashSet(new ComputedValue(valueSpecification, 0.0));
    }

    // 2. Get inputs
    // For all TradeTypes, we'll require the live Price
    final ComputedValue valLivePrice = inputs.getComputedValue(MarketDataRequirementNames.MARKET_VALUE);
    if (valLivePrice == null) {
      throw new OpenGammaRuntimeException(MarketDataRequirementNames.MARKET_VALUE + " not available," + security.getName());
    }
    Double livePrice = (Double) valLivePrice.getValue();

    // For PNL, we need a reference price. We have two cases:
    // Open: will need the closing price
    // New: will need the trade price
    Double referencePrice;
    Double costOfCarry = 0.0;

    if (isNewTrade) {
      referencePrice = trade.getPremium();
      if (referencePrice == null) {
        throw new NullPointerException("New Trades require a premium to compute PNL on trade date. Premium was null for " + trade.getUniqueId());
      }
    } else {
      for (final ComputedValue input : inputs.getAllValues()) {
        if (input.getSpecification().getValueName().equals(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST)) {
          final String field = input.getSpecification().getProperty(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
          if (field.equals(_costOfCarryField)) {
            // Get cost of carry, if available
            Object value = input.getValue();
            if (value != null) {
              costOfCarry = (Double) value;
            }
          }
        }
      }
      referencePrice = calculateReferencePrice(inputs, target);
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
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getValueRequirementName(),
                                                        target.toSpecification(),
                                                        createValueProperties(target).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    // Security's Market Value. We scale up by Notionals and Quantities during execute()
    final Security security = target.getPositionOrTrade().getSecurity();
    final ComputationTargetReference securityTarget = new ComputationTargetSpecification(ComputationTargetType.SECURITY, security.getUniqueId());
    final ValueRequirement securityValueReq = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, securityTarget);
    requirements.add(securityValueReq);
    requirements.addAll(createReferencePriceRequirement(security));
    // and Cost of Carry, if provided
    if (_costOfCarryField.length() > 0) {
      requirements.add(HistoricalTimeSeriesFunctionUtils.createHTSLatestRequirement(security, _costOfCarryField, null));
    }
    return requirements;
  }

  protected abstract Set<ValueRequirement> createReferencePriceRequirement(Security security);

  // TODO throw NPE if price isn't in inputs for consistency with old behaviour
  protected abstract Double calculateReferencePrice(FunctionInputs inputs, ComputationTarget target);

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }
}
