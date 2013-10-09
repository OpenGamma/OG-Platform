/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts FX instruments into a form suitable for use by the analytics library.
 * @deprecated Use the converters that convert one specific type (e.g. {@link FXForwardSecurityConverter}
 */
@Deprecated
public class ForexSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The currency pairs */
  private final CurrencyPairs _currencyPairs;

  /**
   * @param currencyPairs The currency pairs, not null
   */
  public ForexSecurityConverter(final CurrencyPairs currencyPairs) {
    ArgumentChecker.notNull(currencyPairs, "currency pairs");
    _currencyPairs = currencyPairs;
  }

  @Override
  public InstrumentDefinition<?> visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity fxDigitalOptionSecurity) {
    ArgumentChecker.notNull(fxDigitalOptionSecurity, "fx digital option (ndf) security");
    final Currency putCurrency = fxDigitalOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxDigitalOptionSecurity.getCallCurrency();
    final double putAmount = fxDigitalOptionSecurity.getPutAmount();
    final double callAmount = fxDigitalOptionSecurity.getCallAmount();
    final ZonedDateTime expiry = fxDigitalOptionSecurity.getExpiry().getExpiry();
    final ZonedDateTime settlementDate = fxDigitalOptionSecurity.getSettlementDate();
    final boolean isLong = fxDigitalOptionSecurity.isLong();
    final ForexDefinition underlying;
    final Currency payCurrency = fxDigitalOptionSecurity.getPaymentCurrency();
    final boolean payDomestic;
    final CurrencyPair baseQuotePair = _currencyPairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote order for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    // Implementation note: To get Base/quote in market standard order.
    if (baseQuotePair.getBase().equals(putCurrency)) {
      underlying = ForexDefinition.fromAmounts(putCurrency, callCurrency, settlementDate, putAmount, -callAmount);
      payDomestic = (payCurrency.equals(callCurrency));
      return new ForexOptionDigitalDefinition(underlying, expiry, false, isLong, payDomestic);
    }
    underlying = ForexDefinition.fromAmounts(callCurrency, putCurrency, settlementDate, callAmount, -putAmount);
    payDomestic = (payCurrency.equals(putCurrency));
    return new ForexOptionDigitalDefinition(underlying, expiry, true, isLong, payDomestic);
  }

  @Override
  public InstrumentDefinition<?> visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity fxNDFDigitalOptionSecurity) {
    ArgumentChecker.notNull(fxNDFDigitalOptionSecurity, "fx digital option (ndf) security");
    final Currency putCurrency = fxNDFDigitalOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxNDFDigitalOptionSecurity.getCallCurrency();
    final double putAmount = fxNDFDigitalOptionSecurity.getPutAmount();
    final double callAmount = fxNDFDigitalOptionSecurity.getCallAmount();
    final ZonedDateTime expiry = fxNDFDigitalOptionSecurity.getExpiry().getExpiry();
    final ZonedDateTime settlementDate = fxNDFDigitalOptionSecurity.getSettlementDate();
    final boolean isLong = fxNDFDigitalOptionSecurity.isLong();
    final ForexDefinition underlying;
    final CurrencyPair baseQuotePair = _currencyPairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote order for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    // TODO: Review this part (see digital options)
    if (baseQuotePair.getBase().equals(putCurrency)) { // To get Base/quote in market standard order.
      final double fxRate = callAmount / putAmount;
      underlying = new ForexDefinition(putCurrency, callCurrency, settlementDate, putAmount, fxRate);
      return new ForexOptionDigitalDefinition(underlying, expiry, false, isLong);
    }
    final double fxRate = putAmount / callAmount;
    underlying = new ForexDefinition(callCurrency, putCurrency, settlementDate, callAmount, fxRate);
    return new ForexOptionDigitalDefinition(underlying, expiry, true, isLong);
  }

  @Override
  public InstrumentDefinition<?> visitFXOptionSecurity(final FXOptionSecurity fxOptionSecurity) {
    ArgumentChecker.notNull(fxOptionSecurity, "fx option security");
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    final double putAmount = fxOptionSecurity.getPutAmount();
    final double callAmount = fxOptionSecurity.getCallAmount();
    final ZonedDateTime expiry = fxOptionSecurity.getExpiry().getExpiry();
    final ZonedDateTime settlementDate = fxOptionSecurity.getSettlementDate();
    final boolean isLong = fxOptionSecurity.isLong();
    ForexDefinition underlying;
    final CurrencyPair baseQuotePair = _currencyPairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote order for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    if (baseQuotePair.getBase().equals(putCurrency)) {
      underlying = ForexDefinition.fromAmounts(putCurrency, callCurrency, settlementDate, putAmount, -callAmount);
      return new ForexOptionVanillaDefinition(underlying, expiry, false, isLong);
    }
    underlying = ForexDefinition.fromAmounts(callCurrency, putCurrency, settlementDate, callAmount, -putAmount);
    return new ForexOptionVanillaDefinition(underlying, expiry, true, isLong);
  }

  @Override
  public InstrumentDefinition<?> visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity fxOptionSecurity) {
    ArgumentChecker.notNull(fxOptionSecurity, "fx option security");
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    final double putAmount = fxOptionSecurity.getPutAmount();
    final double callAmount = fxOptionSecurity.getCallAmount();
    final ZonedDateTime expiry = fxOptionSecurity.getExpiry().getExpiry();
    final ZonedDateTime settlementDate = fxOptionSecurity.getSettlementDate();
    final boolean isLong = fxOptionSecurity.isLong();
    ForexDefinition underlying;
    final CurrencyPair baseQuotePair = _currencyPairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote order for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    if (baseQuotePair.getBase().equals(putCurrency)) {
      underlying = ForexDefinition.fromAmounts(putCurrency, callCurrency, settlementDate, putAmount, -callAmount);
      return new ForexOptionVanillaDefinition(underlying, expiry, false, isLong);
    }
    underlying = ForexDefinition.fromAmounts(callCurrency, putCurrency, settlementDate, callAmount, -putAmount);
    return new ForexOptionVanillaDefinition(underlying, expiry, true, isLong);
  }

  @Override
  public InstrumentDefinition<?> visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity barrierOptionSecurity) {
    ArgumentChecker.notNull(barrierOptionSecurity, "fx barrier option security");
    ArgumentChecker.isTrue(barrierOptionSecurity.getBarrierType() != BarrierType.DOUBLE, "Can only handle single barrier options");
    ArgumentChecker.isTrue(barrierOptionSecurity.getMonitoringType() == MonitoringType.CONTINUOUS, "Can only handle continuously-monitored barrier options");
    final ZonedDateTime expiry = barrierOptionSecurity.getExpiry().getExpiry();
    final ZonedDateTime settlementDate = barrierOptionSecurity.getSettlementDate();
    final boolean isLong = barrierOptionSecurity.isLong();
    final Currency putCurrency = barrierOptionSecurity.getPutCurrency();
    final Currency callCurrency = barrierOptionSecurity.getCallCurrency();
    final double putAmount = barrierOptionSecurity.getPutAmount();
    final double callAmount = barrierOptionSecurity.getCallAmount();
    final Barrier barrier = new Barrier(getKnockType(barrierOptionSecurity.getBarrierDirection()), getBarrierType(barrierOptionSecurity.getBarrierType()),
        getObservationType(barrierOptionSecurity.getMonitoringType()), barrierOptionSecurity.getBarrierLevel());
    // Compose underlying FXOption s.t. strike is quoted using market convention, as defined in _currencyPairs
    final CurrencyPair baseQuotePair = _currencyPairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote order for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    ForexDefinition underlying;
    boolean isCall;
    if (baseQuotePair.getBase().equals(putCurrency)) {
      underlying = ForexDefinition.fromAmounts(putCurrency, callCurrency, settlementDate, putAmount, -callAmount);
      isCall = false;
    } else {
      underlying = ForexDefinition.fromAmounts(callCurrency, putCurrency, settlementDate, callAmount, -putAmount);
      isCall = true;
    }
    return new ForexOptionSingleBarrierDefinition(new ForexOptionVanillaDefinition(underlying, expiry, isCall, isLong), barrier);
  }

  @Override
  public InstrumentDefinition<?> visitFXForwardSecurity(final FXForwardSecurity fxForwardSecurity) {
    ArgumentChecker.notNull(fxForwardSecurity, "fx forward security");
    final Currency payCurrency = fxForwardSecurity.getPayCurrency();
    final Currency receiveCurrency = fxForwardSecurity.getReceiveCurrency();
    final double payAmount = fxForwardSecurity.getPayAmount();
    final double receiveAmount = fxForwardSecurity.getReceiveAmount();
    final ZonedDateTime forwardDate = fxForwardSecurity.getForwardDate();
    return ForexDefinition.fromAmounts(payCurrency, receiveCurrency, forwardDate, -payAmount, receiveAmount);
  }

  @Override
  public InstrumentDefinition<?> visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity fxForwardSecurity) {
    ArgumentChecker.notNull(fxForwardSecurity, "fx forward security");
    final Currency payCurrency = fxForwardSecurity.getPayCurrency();
    final Currency receiveCurrency = fxForwardSecurity.getReceiveCurrency();
    final double payAmount = fxForwardSecurity.getPayAmount();
    final double receiveAmount = fxForwardSecurity.getReceiveAmount();
    final double exchangeRate = receiveAmount / payAmount;
    final ZonedDateTime fixingDate = fxForwardSecurity.getForwardDate();
    final ZonedDateTime paymentDate = fixingDate; //TODO get this right
    return new ForexNonDeliverableForwardDefinition(payCurrency, receiveCurrency, receiveAmount, exchangeRate, fixingDate, paymentDate);
  }

  private static KnockType getKnockType(final BarrierDirection direction) {
    switch (direction) {
      case KNOCK_IN:
        return KnockType.IN;
      case KNOCK_OUT:
        return KnockType.OUT;
      default:
        throw new OpenGammaRuntimeException("Should never happen");
    }
  }

  private static com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType getBarrierType(final BarrierType type) {
    switch (type) {
      case UP:
        return com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType.UP;
      case DOWN:
        return com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType.DOWN;
      default:
        throw new OpenGammaRuntimeException("Should never happen");
    }
  }

  private static ObservationType getObservationType(final MonitoringType type) {
    switch (type) {
      case CONTINUOUS:
        return ObservationType.CONTINUOUS;
      default:
        throw new OpenGammaRuntimeException("Should never happen");
    }
  }

}
