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
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;

/**
 * 
 */
public class CreditDefaultIndexSwapSecurityToProxyConverter extends FinancialSecurityVisitorAdapter<CreditDefaultSwapDefinition> {
  private final CreditDefaultSwapSecurityConverter _converter;

  public CreditDefaultIndexSwapSecurityToProxyConverter(final HolidaySource holidaySource, final RegionSource regionSource, final OrganizationSource organizationSource) {
    _converter = new CreditDefaultSwapSecurityConverter(holidaySource, regionSource, organizationSource);
  }

  public CreditDefaultSwapDefinition visitSomething() {
    final boolean isBuy = false;
    final ExternalId protectionSeller = null;
    final ExternalId protectionBuyer = null;
    final ExternalId referenceEntity = null;
    final DebtSeniority debtSeniority = null;
    final RestructuringClause restructuringClause = null;
    final ExternalId regionId = null;
    final ZonedDateTime startDate = null;
    final ZonedDateTime effectiveDate = null;
    final ZonedDateTime maturityDate = null;
    final StubType stubType = null;
    final Frequency couponFrequency = null;
    final DayCount dayCount = null;
    final BusinessDayConvention businessDayConvention = null;
    final boolean immAdjustMaturityDate = false;
    final boolean adjustEffectiveDate = false;
    final boolean adjustMaturityDate = false;
    final InterestRateNotional notional = null;
    final double recoveryRate = 0;
    final boolean includeAccruedPremium = false;
    final boolean protectionStart = false;
    final double parSpread = 0;
    final LegacyVanillaCDSSecurity cds = new LegacyVanillaCDSSecurity(
        isBuy,
        protectionSeller,
        protectionBuyer,
        referenceEntity,
        debtSeniority,
        restructuringClause,
        regionId,
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
        recoveryRate,
        includeAccruedPremium,
        protectionStart,
        parSpread);
    return cds.accept(_converter);
  }
}
