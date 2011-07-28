/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.money.Currency;

/**
 * Summing function for {@link ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES} which ensures it only sums across
 * the same curve currency.
 */
public class YieldCurveNodeSensitivitiesSummingFunction extends FilteringSummingFunction {

  private static final FinancialSecurityVisitor<Boolean> s_inclusionVisitor = new SecurityInclusionVisitor();
  
  public YieldCurveNodeSensitivitiesSummingFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, Collections.singleton(ValuePropertyNames.CURVE_CURRENCY));
  }

  @Override
  protected boolean isIncluded(FinancialSecurity security, ValueProperties filterProperties, SecuritySource securities) {
    Boolean isIncluded = security.accept(s_inclusionVisitor);
    if (isIncluded == null) {
      throw new OpenGammaRuntimeException("Security " + security + " not explicitly included or excluded by " + getClass().getSimpleName());
    }
    if (!isIncluded) {
      return false;
    }
    if (filterProperties.isEmpty()) {
      return true;
    }
    Collection<Currency> securityCurrencies = security.accept(new SecurityCurrencyVisitor(securities));
    if (securityCurrencies == null) {
      securityCurrencies = Collections.singleton(FinancialSecurityUtils.getCurrency(security));
    }
    String[] currencyCodes = new String[securityCurrencies.size()];
    int i = 0;
    for (Currency currency : securityCurrencies) {
      currencyCodes[i++] = currency.getCode();
    }
    ValueProperties securityProperties = ValueProperties.with(ValuePropertyNames.CURVE_CURRENCY, currencyCodes).get();
    return filterProperties.isSatisfiedBy(securityProperties);
  }
  
  //-------------------------------------------------------------------------
  
  private static class SecurityInclusionVisitor implements FinancialSecurityVisitor<Boolean> {
    
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
      return true;
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
      return true;
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
  
  private static class SecurityCurrencyVisitor implements FinancialSecurityVisitor<Collection<Currency>> {

    private final SecuritySource _securitySource;
    
    public SecurityCurrencyVisitor(SecuritySource securitySource) {
      _securitySource = securitySource;
    }
    
    public SecuritySource getSecuritySource() {
      return _securitySource;
    }
    
    @Override
    public Collection<Currency> visitBondSecurity(BondSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitCashSecurity(CashSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitEquitySecurity(EquitySecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFRASecurity(FRASecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFutureSecurity(FutureSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitSwapSecurity(SwapSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitEquityOptionSecurity(EquityOptionSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFXOptionSecurity(FXOptionSecurity security) {
      return Arrays.asList(security.getCallCurrency(), security.getPutCurrency());
    }

    @Override
    public Collection<Currency> visitSwaptionSecurity(SwaptionSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFXSecurity(FXSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFXForwardSecurity(FXForwardSecurity security) {
      FXSecurity underlying = (FXSecurity) getSecuritySource().getSecurity(IdentifierBundle.of(security.getUnderlyingIdentifier()));
      return Arrays.asList(underlying.getPayCurrency(), underlying.getReceiveCurrency());
    }

    @Override
    public Collection<Currency> visitCapFloorSecurity(CapFloorSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
      return null;
    }
    
    
    
  }
    
}
