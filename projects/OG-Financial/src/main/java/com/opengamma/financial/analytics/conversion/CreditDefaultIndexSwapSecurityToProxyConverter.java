/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;

/**
 * 
 */
public class CreditDefaultIndexSwapSecurityToProxyConverter extends FinancialSecurityVisitorAdapter<CreditDefaultSwapDefinition> {
  //TODO remove this
  private static final ExternalId REGION = ExternalSchemes.financialRegionId("US");
  private final CreditDefaultSwapSecurityConverter _converter;

  public CreditDefaultIndexSwapSecurityToProxyConverter(final HolidaySource holidaySource, final RegionSource regionSource, final OrganizationSource organizationSource) {
    _converter = new CreditDefaultSwapSecurityConverter(holidaySource, regionSource, organizationSource);
  }

  @Override
  public CreditDefaultSwapDefinition visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    final boolean isBuy = security.isBuy();
    final ExternalId protectionSeller = security.getProtectionSeller();
    final ExternalId protectionBuyer = security.getProtectionBuyer();
    final ExternalId referenceEntity = security.getReferenceEntity();
    final DebtSeniority debtSeniority = DebtSeniority.NONE;
    final RestructuringClause restructuringClause = RestructuringClause.NONE;
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime effectiveDate = security.getEffectiveDate();
    final ZonedDateTime maturityDate = security.getMaturityDate();
    final StubType stubType = security.getStubType();
    final Frequency couponFrequency = security.getCouponFrequency();
    final DayCount dayCount = security.getDayCount();
    final BusinessDayConvention businessDayConvention = security.getBusinessDayConvention();
    final boolean immAdjustMaturityDate = false;
    final boolean adjustEffectiveDate = false;
    final boolean adjustMaturityDate = false;
    final InterestRateNotional notional = security.getNotional();
    final boolean includeAccruedPremium = false;
    final boolean protectionStart = false;
    final double parSpread = security.getIndexCoupon();
    final LegacyVanillaCDSSecurity cds = new LegacyVanillaCDSSecurity(
        isBuy,
        protectionSeller,
        protectionBuyer,
        referenceEntity,
        debtSeniority,
        restructuringClause,
        REGION,
        startDate,
        effectiveDate,
        maturityDate,
        stubType,
        couponFrequency,
        dayCount,
        businessDayConvention,
        immAdjustMaturityDate,
        adjustEffectiveDate,
        adjustMaturityDate,
        notional,
        includeAccruedPremium,
        protectionStart,
        parSpread);
    return cds.accept(_converter);
  }
}
