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
import com.opengamma.id.ExternalIdBundle;
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
  protected boolean isIncluded(final FinancialSecurity security, final ValueProperties filterProperties, final SecuritySource securities) {
    final Boolean isIncluded = security.accept(s_inclusionVisitor);
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
    final String[] currencyCodes = new String[securityCurrencies.size()];
    int i = 0;
    for (final Currency currency : securityCurrencies) {
      currencyCodes[i++] = currency.getCode();
    }
    final ValueProperties securityProperties = ValueProperties.with(ValuePropertyNames.CURVE_CURRENCY, currencyCodes).get();
    return filterProperties.isSatisfiedBy(securityProperties);
  }

  //-------------------------------------------------------------------------

  private static class SecurityInclusionVisitor implements FinancialSecurityVisitor<Boolean> {

    @Override
    public Boolean visitBondSecurity(final BondSecurity security) {
      return true;
    }

    @Override
    public Boolean visitCashSecurity(final CashSecurity security) {
      return true;
    }

    @Override
    public Boolean visitEquitySecurity(final EquitySecurity security) {
      return false;
    }

    @Override
    public Boolean visitFRASecurity(final FRASecurity security) {
      return true;
    }

    @Override
    public Boolean visitFutureSecurity(final FutureSecurity security) {
      return (security instanceof InterestRateFutureSecurity);
    }

    @Override
    public Boolean visitSwapSecurity(final SwapSecurity security) {
      return true;
    }

    @Override
    public Boolean visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return false;
    }

    @Override
    public Boolean visitEquityOptionSecurity(final EquityOptionSecurity security) {
      return false;
    }

    @Override
    public Boolean visitFXOptionSecurity(final FXOptionSecurity security) {
      return true;
    }

    @Override
    public Boolean visitSwaptionSecurity(final SwaptionSecurity security) {
      return true;
    }

    @Override
    public Boolean visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      return false;
    }

    @Override
    public Boolean visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      return false;
    }

    @Override
    public Boolean visitFXSecurity(final FXSecurity security) {
      return false;
    }

    @Override
    public Boolean visitFXForwardSecurity(final FXForwardSecurity security) {
      return true;
    }

    @Override
    public Boolean visitCapFloorSecurity(final CapFloorSecurity security) {
      return false;
    }

    @Override
    public Boolean visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      return false;
    }

    @Override
    public Boolean visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      return false;
    }

  }

  private static class SecurityCurrencyVisitor implements FinancialSecurityVisitor<Collection<Currency>> {

    private final SecuritySource _securitySource;

    public SecurityCurrencyVisitor(final SecuritySource securitySource) {
      _securitySource = securitySource;
    }

    public SecuritySource getSecuritySource() {
      return _securitySource;
    }

    @Override
    public Collection<Currency> visitBondSecurity(final BondSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitCashSecurity(final CashSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitEquitySecurity(final EquitySecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFRASecurity(final FRASecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFutureSecurity(final FutureSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitSwapSecurity(final SwapSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitEquityOptionSecurity(final EquityOptionSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFXOptionSecurity(final FXOptionSecurity security) {
      return Arrays.asList(security.getCallCurrency(), security.getPutCurrency());
    }

    @Override
    public Collection<Currency> visitSwaptionSecurity(final SwaptionSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFXSecurity(final FXSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitFXForwardSecurity(final FXForwardSecurity security) {
      final FXSecurity underlying = (FXSecurity) getSecuritySource().getSecurity(ExternalIdBundle.of(security.getUnderlyingId()));
      return Arrays.asList(underlying.getPayCurrency(), underlying.getReceiveCurrency());
    }

    @Override
    public Collection<Currency> visitCapFloorSecurity(final CapFloorSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      return null;
    }

    @Override
    public Collection<Currency> visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      return null;
    }

  }

}
