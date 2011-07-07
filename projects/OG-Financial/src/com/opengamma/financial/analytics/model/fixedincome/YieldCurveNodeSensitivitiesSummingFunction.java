/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.FilteringSummingFunction;
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
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * Summing function for {@link ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES} which ensures it only sums across
 * the same curve currency.
 */
public class YieldCurveNodeSensitivitiesSummingFunction extends FilteringSummingFunction implements FinancialSecurityVisitor<Boolean> {

  public YieldCurveNodeSensitivitiesSummingFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, Collections.singleton(ValuePropertyNames.CURVE_CURRENCY));
  }

  @Override
  protected boolean isIncluded(FinancialSecurity security, ValueProperties filterProperties) {
    Boolean isIncluded = security.accept(this);
    if (isIncluded == null) {
      throw new OpenGammaRuntimeException("Security " + security + " not explicitly included or excluded by " + getClass().getSimpleName());
    }
    if (!isIncluded) {
      return false;
    }
    if (filterProperties.isEmpty()) {
      return true;
    }
    Currency currency = FinancialSecurityUtils.getCurrency(security);
    ValueProperties securityProperties = ValueProperties.with(ValuePropertyNames.CURVE_CURRENCY, currency.getCode()).get();
    return filterProperties.isSatisfiedBy(securityProperties);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public Boolean visitBondSecurity(BondSecurity security) {
    return true;
  }

  @Override
  public Boolean visitCashSecurity(CashSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquitySecurity(EquitySecurity security) {
    return false;
  }

  @Override
  public Boolean visitFRASecurity(FRASecurity security) {
    return true;
  }

  @Override
  public Boolean visitFutureSecurity(FutureSecurity security) {
    return (security instanceof InterestRateFutureSecurity);
  }

  @Override
  public Boolean visitSwapSecurity(SwapSecurity security) {
    return true;
  }

  @Override
  public Boolean visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitEquityOptionSecurity(EquityOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitFXOptionSecurity(FXOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitSwaptionSecurity(SwaptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
    return false;
  }

  @Override
  public Boolean visitFXSecurity(FXSecurity security) {
    return false;
  }

  @Override
  public Boolean visitFXForwardSecurity(FXForwardSecurity security) {
    return false;
  }

  @Override
  public Boolean visitCapFloorSecurity(CapFloorSecurity security) {
    return false;
  }

  @Override
  public Boolean visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
    return false;
  }

  @Override
  public Boolean visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
    return false;
  }

}
