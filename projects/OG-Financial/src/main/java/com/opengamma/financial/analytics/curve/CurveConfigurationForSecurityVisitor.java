/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CurveConfigurationForSecurityVisitor extends FinancialSecurityVisitorSameValueAdapter<List<ExternalId>> {
  public static final String SECURITY_IDENTIFIER = "SecurityType";

  public CurveConfigurationForSecurityVisitor() {
    super(Collections.<ExternalId>emptyList());
  }

  @Override
  public List<ExternalId> visitCashSecurity(final CashSecurity security) {
    final ExternalId regionId = security.getRegionId();
    final String region = regionId.getValue();
    final String currency = security.getCurrency().getCode();
    final String securityType = security.getSecurityType();
    final UniqueId uniqueId = security.getUniqueId();
    final List<ExternalId> result = new ArrayList<>();
    result.add(ExternalId.of(uniqueId.getScheme(), uniqueId.getValue()));
    result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + region));
    result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + currency));
    result.add(ExternalId.of(regionId.getScheme().getName(), region));
    result.add(ExternalId.of(Currency.OBJECT_SCHEME, currency));
    result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType));
    return result;
  }

  @Override
  public List<ExternalId> visitFRASecurity(final FRASecurity security) {
    final ExternalId regionId = security.getRegionId();
    final String region = regionId.getValue();
    final String currency = security.getCurrency().getCode();
    final String securityType = security.getSecurityType();
    final UniqueId uniqueId = security.getUniqueId();
    final List<ExternalId> result = new ArrayList<>();
    result.add(ExternalId.of(uniqueId.getScheme(), uniqueId.getValue()));
    result.add(security.getUnderlyingId());
    result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + region));
    result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + currency));
    result.add(ExternalId.of(regionId.getScheme().getName(), region));
    result.add(ExternalId.of(Currency.OBJECT_SCHEME, currency));
    result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType));
    return result;
  }

  @Override
  public List<ExternalId> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    final List<ExternalId> result = new ArrayList<>();
    final UniqueId uniqueId = security.getUniqueId();
    final String securityType = security.getSecurityType();
    final String currency = security.getCurrency().getCode();
    result.add(ExternalId.of(uniqueId.getScheme(), uniqueId.getValue()));
    result.add(security.getUnderlyingId());
    result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + "_" + currency));
    result.add(ExternalId.of(Currency.OBJECT_SCHEME, currency));
    result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType));
    return result;
  }
  //
  //  @Override
  //  public List<ExternalId> visitSwapSecurity(final SwapSecurity security) {
  //
  //  }
  //
  //  @Override
  //  public List<ExternalId> visitFXForwardSecurity(final FXForwardSecurity security) {
  //
  //  }
  //
  //  @Override
  //  public List<ExternalId> visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
  //
  //  }
  //
  //  @Override
  //  public List<ExternalId> visitFXOptionSecurity(final FXOptionSecurity security) {
  //
  //  }
  //
  //  @Override
  //  public List<ExternalId> visitSwaptionSecurity(final SwaptionSecurity security) {
  //
  //  }
}
