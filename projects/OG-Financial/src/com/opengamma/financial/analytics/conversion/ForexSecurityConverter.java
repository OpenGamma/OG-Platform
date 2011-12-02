/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.model.forex.ForexUtils;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.model.option.definition.Barrier;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurityVisitor;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurityVisitor;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurityVisitor;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexSecurityConverter implements FXOptionSecurityVisitor<InstrumentDefinition<?>>, FXBarrierOptionSecurityVisitor<InstrumentDefinition<?>>,
    FXForwardSecurityVisitor<InstrumentDefinition<?>> {
  private final SecuritySource _securitySource;

  public ForexSecurityConverter(final SecuritySource securitySource) {
    Validate.notNull(securitySource, "security source");
    _securitySource = securitySource;
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
    final ForexDefinition underlying;
    if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) { // To get Base/quote in market standard order.
      final double fxRate = callAmount / putAmount;
      underlying = new ForexDefinition(putCurrency, callCurrency, settlementDate, putAmount, fxRate);
      return new ForexOptionVanillaDefinition(underlying, expiry, false, isLong);
    }
    final double fxRate = putAmount / callAmount;
    underlying = new ForexDefinition(callCurrency, putCurrency, settlementDate, callAmount, fxRate);
    return new ForexOptionVanillaDefinition(underlying, expiry, true, isLong);
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
    final ExternalId underlyingIdentifier = fxForwardSecurity.getUnderlyingId();
    final FXSecurity fxSecurity = (FXSecurity) _securitySource.getSecurity(ExternalIdBundle.of(underlyingIdentifier));
    final Currency payCurrency = fxSecurity.getPayCurrency();
    final Currency receiveCurrency = fxSecurity.getReceiveCurrency();
    final double payAmount = fxSecurity.getPayAmount();
    final double receiveAmount = fxSecurity.getReceiveAmount();
    final double fxRate = receiveAmount / payAmount;
    final ZonedDateTime forwardDate = fxForwardSecurity.getForwardDate();
    return new ForexDefinition(payCurrency, receiveCurrency, forwardDate, payAmount, fxRate);
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

  private com.opengamma.financial.model.option.definition.Barrier.BarrierType getBarrierType(final BarrierType type) {
    switch (type) {
      case UP:
        return com.opengamma.financial.model.option.definition.Barrier.BarrierType.UP;
      case DOWN:
        return com.opengamma.financial.model.option.definition.Barrier.BarrierType.DOWN;
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

}
