/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forex;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.forex.calculator.ForexConverter;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.model.option.definition.Barrier;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurityVisitor;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexSingleBarrierOptionSecurityConverter implements FXBarrierOptionSecurityVisitor<ForexConverter<?>> {
  
  public ForexSingleBarrierOptionSecurityConverter(SecuritySource securitySource, FinancialSecurityVisitorAdapter<ForexConverter<?>> visitor) {
    Validate.notNull(visitor, "visitor");
  }
  
  public ForexConverter<?> visitFXBarrierOptionSecurity(FXBarrierOptionSecurity barrierOptionSecurity) {
    Validate.notNull(barrierOptionSecurity, "fx barrier option security");
    Validate.isTrue(barrierOptionSecurity.getBarrierType() != BarrierType.DOUBLE, "Can only handle single barrier options");
    Validate.isTrue(barrierOptionSecurity.getMonitoringType() == MonitoringType.CONTINUOUS, "Can only handle continuously-monitored barrier options");
    double level = 0; //barrierOptionSecurity.getLevel(); //TODO
    final Currency putCurrency = barrierOptionSecurity.getPutCurrency();
    final Currency callCurrency = barrierOptionSecurity.getCallCurrency();
    final double putAmount = barrierOptionSecurity.getPutAmount();
    final double callAmount = barrierOptionSecurity.getCallAmount();
    final double fxRate = -putAmount / callAmount;
    final ZonedDateTime expiry = barrierOptionSecurity.getExpiry().getExpiry();
    Barrier barrier = new Barrier(getKnockType(barrierOptionSecurity.getBarrierDirection()),
                                  getBarrierType(barrierOptionSecurity.getBarrierType()),
                                  getObservationType(barrierOptionSecurity.getMonitoringType()), level);
    final ZonedDateTime settlementDate = barrierOptionSecurity.getSettlementDate();
    final ForexDefinition underlying = new ForexDefinition(putCurrency, callCurrency, settlementDate, putAmount, fxRate); //TODO this needs its own converter
    boolean isLong = barrierOptionSecurity.getIsLong();
    return new ForexOptionSingleBarrierDefinition(new ForexOptionVanillaDefinition(underlying, expiry, true, isLong), barrier);
  }

  private KnockType getKnockType(BarrierDirection direction) {
    switch (direction) {
      case KNOCK_IN:
        return KnockType.IN;
      case KNOCK_OUT:
        return KnockType.OUT;
      default:
        throw new OpenGammaRuntimeException("Should never happen");
    }
  }
  
  private com.opengamma.financial.model.option.definition.Barrier.BarrierType getBarrierType(BarrierType type) {
    switch (type) {
      case UP:
        return com.opengamma.financial.model.option.definition.Barrier.BarrierType.UP;
      case DOWN:
        return com.opengamma.financial.model.option.definition.Barrier.BarrierType.DOWN;
      default:
        throw new OpenGammaRuntimeException("Should never happen");
    }
  }
  
  private ObservationType getObservationType(MonitoringType type) {
    switch (type) {
      case CONTINUOUS:
        return ObservationType.CONTINUOUS;
      default:
        throw new OpenGammaRuntimeException("Should never happen");
    }
  }
}
