/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
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
  private final SecuritySource _securitySource;
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final LegalEntitySource _orgSource;
  private final ZonedDateTime _vaulationTime;

  public CreditDefaultIndexSwapSecurityToProxyConverter(final HolidaySource holidaySource, final RegionSource regionSource, final LegalEntitySource legalEntitySource,
      final SecuritySource securitySource, final ZonedDateTime valuationTime) {
    _securitySource = securitySource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _orgSource = legalEntitySource;
    _vaulationTime = valuationTime;

  }

  @Override
  public CreditDefaultSwapDefinition visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    final boolean isBuy = security.isBuy();
    final ExternalId protectionSeller = security.getProtectionSeller();
    final ExternalId protectionBuyer = security.getProtectionBuyer();
    final ExternalId referenceEntity = security.getReferenceEntity();
    final CreditDefaultSwapIndexDefinitionSecurity indexDef = (CreditDefaultSwapIndexDefinitionSecurity) _securitySource.getSingle(referenceEntity.toBundle());
    if (indexDef == null) {
      throw new OpenGammaRuntimeException("Underlying index definition not found: " + referenceEntity);
    }
    final double recoveryRate = indexDef.getRecoveryRate();
    final CreditDefaultSwapSecurityConverter converter = new CreditDefaultSwapSecurityConverter(_holidaySource, _regionSource, _orgSource, recoveryRate, _vaulationTime);
    final DebtSeniority debtSeniority = DebtSeniority.NONE;
    final RestructuringClause restructuringClause = RestructuringClause.NONE;
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime effectiveDate = security.getEffectiveDate();
    final ZonedDateTime maturityDate = security.getMaturityDate();
    final StubType stubType = security.getStubType();
    final Frequency couponFrequency = security.getCouponFrequency();
    if (couponFrequency.getName().equals(Frequency.NEVER_NAME)) {
      throw new OpenGammaRuntimeException("Coupon payment frequency was set to NEVER; cannot price CDX");
    }
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
    final CreditDefaultSwapDefinition definition = cds.accept(converter);
    return definition;
  }
}
