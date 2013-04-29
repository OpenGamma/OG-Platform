/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CurveConfigurationForSecurityVisitor extends FinancialSecurityVisitorSameValueAdapter<List<List<ExternalId>>> {
  public static final String SECURITY_IDENTIFIER = "SecurityType";

  public CurveConfigurationForSecurityVisitor() {
    super(Collections.<List<ExternalId>>emptyList());
  }

  @Override
  public List<List<ExternalId>> visitCashSecurity(final CashSecurity security) {
    final ExternalId regionId = security.getRegionId();
    final String region = regionId.getValue();
    final String currency = security.getCurrency().getCode();
    final String securityType = security.getSecurityType();
    final UniqueId uniqueId = security.getUniqueId();
    final List<List<ExternalId>> result = new ArrayList<>();
    result.add(Arrays.asList(ExternalId.of(uniqueId.getScheme(), uniqueId.getValue())));
    result.add(Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + region)));
    result.add(Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + currency)));
    result.add(Arrays.asList(ExternalId.of(regionId.getScheme().getName(), region)));
    result.add(Arrays.asList(ExternalId.of(Currency.OBJECT_SCHEME, currency)));
    result.add(Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType)));
    return result;
  }

  @Override
  public List<List<ExternalId>> visitFRASecurity(final FRASecurity security) {
    final ExternalId regionId = security.getRegionId();
    final String region = regionId.getValue();
    final String currency = security.getCurrency().getCode();
    final String securityType = security.getSecurityType();
    final UniqueId uniqueId = security.getUniqueId();
    final List<List<ExternalId>> result = new ArrayList<>();
    result.add(Arrays.asList(ExternalId.of(uniqueId.getScheme(), uniqueId.getValue())));
    result.add(Arrays.asList(security.getUnderlyingId()));
    result.add(Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + region)));
    result.add(Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + currency)));
    result.add(Arrays.asList(ExternalId.of(regionId.getScheme().getName(), region)));
    result.add(Arrays.asList(ExternalId.of(Currency.OBJECT_SCHEME, currency)));
    result.add(Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType)));
    return result;
  }

  @Override
  public List<List<ExternalId>> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    final UniqueId uniqueId = security.getUniqueId();
    final String securityType = security.getSecurityType();
    final String currency = security.getCurrency().getCode();
    final List<List<ExternalId>> result = new ArrayList<>();
    result.add(Arrays.asList(ExternalId.of(uniqueId.getScheme(), uniqueId.getValue())));
    result.add(Arrays.asList(security.getUnderlyingId()));
    result.add(Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + currency)));
    result.add(Arrays.asList(ExternalId.of(Currency.OBJECT_SCHEME, currency)));
    result.add(Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType)));
    return result;
  }

  @Override
  public List<List<ExternalId>> visitSwapSecurity(final SwapSecurity security) {
    final List<List<ExternalId>> result = new ArrayList<>();
    final List<ExternalId> tickersList = new ArrayList<>();
    final List<ExternalId> securityRegionList = new ArrayList<>();
    final List<ExternalId> securityCurrencyList = new ArrayList<>();
    final List<ExternalId> regionList = new ArrayList<>();
    final List<ExternalId> currencyList = new ArrayList<>();
    final UniqueId uniqueId = security.getUniqueId();
    final String securityType = security.getSecurityType();
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    securityRegionList.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + payLeg.getRegionId()));
    securityRegionList.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + receiveLeg.getRegionId()));
    regionList.add(payLeg.getRegionId());
    regionList.add(receiveLeg.getRegionId());
    if (payLeg.getNotional() instanceof InterestRateNotional) {
      final String payCurrency = ((InterestRateNotional) payLeg.getNotional()).getCurrency().getCode();
      final ExternalId payCurrencyId = ExternalId.of(Currency.OBJECT_SCHEME, payCurrency);
      securityCurrencyList.add(payCurrencyId);
      currencyList.add(payCurrencyId);
    }
    if (receiveLeg.getNotional() instanceof InterestRateNotional) {
      final String receiveCurrency = ((InterestRateNotional) receiveLeg.getNotional()).getCurrency().getCode();
      final ExternalId receiveCurrencyId = ExternalId.of(Currency.OBJECT_SCHEME, receiveCurrency);
      securityCurrencyList.add(receiveCurrencyId);
      currencyList.add(receiveCurrencyId);
    }
    if (payLeg instanceof FloatingInterestRateLeg) {
      tickersList.add(((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId());
    }
    if (receiveLeg instanceof FloatingInterestRateLeg) {
      tickersList.add(((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId());
    }
    result.add(Arrays.asList(ExternalId.of(uniqueId.getScheme(), uniqueId.getValue())));
    result.add(tickersList);
    result.add(securityRegionList);
    result.add(securityCurrencyList);
    result.add(regionList);
    result.add(currencyList);
    result.add(Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType)));
    return result;
  }

  @Override
  public List<List<ExternalId>> visitFXForwardSecurity(final FXForwardSecurity security) {
    final List<List<ExternalId>> result = new ArrayList<>();
    final List<ExternalId> securityCurrencyList = new ArrayList<>();
    final List<ExternalId> currencyList = new ArrayList<>();
    final UniqueId uniqueId = security.getUniqueId();
    final String securityType = security.getSecurityType();
    final String payCurrency = security.getPayCurrency().getCode();
    final String receiveCurrency = security.getReceiveCurrency().getCode();
    securityCurrencyList.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + payCurrency));
    securityCurrencyList.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + receiveCurrency));
    currencyList.add(ExternalId.of(Currency.OBJECT_SCHEME, payCurrency));
    currencyList.add(ExternalId.of(Currency.OBJECT_SCHEME, receiveCurrency));
    result.add(Arrays.asList(ExternalId.of(uniqueId.getScheme(), uniqueId.getValue())));
    result.add(securityCurrencyList);
    result.add(currencyList);
    result.add(Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, uniqueId.getValue())));
    return result;
  }

}
