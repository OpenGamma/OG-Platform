/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
//TODO use the visitor adapter
public class ForexSecurityConverter implements FinancialSecurityVisitor<InstrumentDefinition<?>> {

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

  private KnockType getKnockType(final BarrierDirection direction) {
    switch (direction) {
      case KNOCK_IN:
        return KnockType.IN;
      case KNOCK_OUT:
        return KnockType.OUT;
      default:
        throw new OpenGammaRuntimeException("Should never happen");
    }
  }

  private com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType getBarrierType(final BarrierType type) {
    switch (type) {
      case UP:
        return com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType.UP;
      case DOWN:
        return com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType.DOWN;
      default:
        throw new OpenGammaRuntimeException("Should never happen");
    }
  }

  private ObservationType getObservationType(final MonitoringType type) {
    switch (type) {
      case CONTINUOUS:
        return ObservationType.CONTINUOUS;
      default:
        throw new OpenGammaRuntimeException("Should never happen");
    }
  }

  @Override
  public InstrumentDefinition<?> visitBondSecurity(final BondSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitCashSecurity(final CashSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitEquitySecurity(final EquitySecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitFRASecurity(final FRASecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitFutureSecurity(final FutureSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitSwapSecurity(final SwapSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitSwaptionSecurity(final SwaptionSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitCapFloorSecurity(final CapFloorSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    return null;
  }

  @Override
  public InstrumentDefinition<?> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    return null;
  }
}
