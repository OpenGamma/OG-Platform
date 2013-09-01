/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.Validate;
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
import com.opengamma.financial.analytics.model.forex.FXUtils;
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
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use the version that uses CurrencyPairs {@link ForexSecurityConverter}
 */
@Deprecated
public class ForexSecurityConverterDeprecated extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {

  @Override
  public InstrumentDefinition<?> visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity fxDigitalOptionSecurity) {
    Validate.notNull(fxDigitalOptionSecurity, "fx digital option (ndf) security");
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
    final boolean order = FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency);
    // Implementation note: To get Base/quote in market standard order.
    if (order) {
      underlying = ForexDefinition.fromAmounts(putCurrency, callCurrency, settlementDate, putAmount, -callAmount);
      payDomestic = (payCurrency.equals(callCurrency));
    } else {
      underlying = ForexDefinition.fromAmounts(callCurrency, putCurrency, settlementDate, callAmount, -putAmount);
      payDomestic = (payCurrency.equals(putCurrency));
    }
    return new ForexOptionDigitalDefinition(underlying, expiry, !order, isLong, payDomestic);
  }

  @Override
  public InstrumentDefinition<?> visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity fxNDFDigitalOptionSecurity) {
    Validate.notNull(fxNDFDigitalOptionSecurity, "fx digital option (ndf) security");
    final Currency putCurrency = fxNDFDigitalOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxNDFDigitalOptionSecurity.getCallCurrency();
    final double putAmount = fxNDFDigitalOptionSecurity.getPutAmount();
    final double callAmount = fxNDFDigitalOptionSecurity.getCallAmount();
    final ZonedDateTime expiry = fxNDFDigitalOptionSecurity.getExpiry().getExpiry();
    final ZonedDateTime settlementDate = fxNDFDigitalOptionSecurity.getSettlementDate();
    final boolean isLong = fxNDFDigitalOptionSecurity.isLong();
    final ForexDefinition underlying;
    // TODO: Review this part (see digital options)
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) { // To get Base/quote in market standard order.
      final double fxRate = callAmount / putAmount;
      underlying = new ForexDefinition(putCurrency, callCurrency, settlementDate, putAmount, fxRate);
      // REVIEW: jim 6-Feb-2012 -- take account of NDF!
      return new ForexOptionDigitalDefinition(underlying, expiry, false, isLong);
    }
    final double fxRate = putAmount / callAmount;
    underlying = new ForexDefinition(callCurrency, putCurrency, settlementDate, callAmount, fxRate);
    // REVIEW: jim 6-Feb-2012 -- take account of NDF!
    return new ForexOptionDigitalDefinition(underlying, expiry, true, isLong);
  }

  @Override
  public InstrumentDefinition<?> visitFXOptionSecurity(final FXOptionSecurity fxOptionSecurity) {
    Validate.notNull(fxOptionSecurity, "fx option security");
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    final double putAmount = fxOptionSecurity.getPutAmount();
    final double callAmount = fxOptionSecurity.getCallAmount();
    final ZonedDateTime expiry = fxOptionSecurity.getExpiry().getExpiry();
    final ZonedDateTime settlementDate = fxOptionSecurity.getSettlementDate();
    final boolean isLong = fxOptionSecurity.isLong();
    ForexDefinition underlying;
    final boolean order = FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency); // To get Base/quote in market standard order.
    if (order) {
      underlying = ForexDefinition.fromAmounts(putCurrency, callCurrency, settlementDate, putAmount, -callAmount);
    } else {
      underlying = ForexDefinition.fromAmounts(callCurrency, putCurrency, settlementDate, callAmount, -putAmount);
    }
    return new ForexOptionVanillaDefinition(underlying, expiry, !order, isLong);
  }

  @Override
  public InstrumentDefinition<?> visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity barrierOptionSecurity) {
    Validate.notNull(barrierOptionSecurity, "fx barrier option security");
    Validate.isTrue(barrierOptionSecurity.getBarrierType() != BarrierType.DOUBLE, "Can only handle single barrier options");
    Validate.isTrue(barrierOptionSecurity.getMonitoringType() == MonitoringType.CONTINUOUS, "Can only handle continuously-monitored barrier options");
    final double level = barrierOptionSecurity.getBarrierLevel();
    final Currency putCurrency = barrierOptionSecurity.getPutCurrency();
    final Currency callCurrency = barrierOptionSecurity.getCallCurrency();
    final double putAmount = barrierOptionSecurity.getPutAmount();
    final double callAmount = barrierOptionSecurity.getCallAmount();
    final double fxRate = callAmount / putAmount;
    final ZonedDateTime expiry = barrierOptionSecurity.getExpiry().getExpiry();
    final ZonedDateTime settlementDate = barrierOptionSecurity.getSettlementDate();
    final ForexDefinition underlying = new ForexDefinition(putCurrency, callCurrency, settlementDate, putAmount, fxRate); //TODO this needs its own converter
    final boolean isLong = barrierOptionSecurity.isLong();
    final Barrier barrier = new Barrier(getKnockType(barrierOptionSecurity.getBarrierDirection()), getBarrierType(barrierOptionSecurity.getBarrierType()),
        getObservationType(barrierOptionSecurity.getMonitoringType()), level);
    return new ForexOptionSingleBarrierDefinition(new ForexOptionVanillaDefinition(underlying, expiry, true, isLong), barrier);
  }

  @Override
  public InstrumentDefinition<?> visitFXForwardSecurity(final FXForwardSecurity fxForwardSecurity) {
    Validate.notNull(fxForwardSecurity, "fx forward security");
    final Currency payCurrency = fxForwardSecurity.getPayCurrency();
    final Currency receiveCurrency = fxForwardSecurity.getReceiveCurrency();
    final double payAmount = fxForwardSecurity.getPayAmount();
    final double receiveAmount = fxForwardSecurity.getReceiveAmount();
    final ZonedDateTime forwardDate = fxForwardSecurity.getForwardDate();
    return ForexDefinition.fromAmounts(payCurrency, receiveCurrency, forwardDate, -payAmount, receiveAmount);
  }

  @Override
  public InstrumentDefinition<?> visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity fxForwardSecurity) {
    Validate.notNull(fxForwardSecurity, "fx forward security");
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
