/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CreditDefaultSwapSecurityConverter extends FinancialSecurityVisitorAdapter<CreditDefaultSwapDefinition> {

  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;

  static final LegalEntity DUMMY_OBLIGOR_A = new ManageableLegalEntity("Dummy_A", ExternalIdBundle.of(ExternalId.of("DUMMY", "Dummy_A")));
  static final LegalEntity DUMMY_OBLIGOR_B = new ManageableLegalEntity("Dummy_B", ExternalIdBundle.of(ExternalId.of("DUMMY", "Dummy_B")));
  static final LegalEntity DUMMY_OBLIGOR_C = new ManageableLegalEntity("Dummy_C", ExternalIdBundle.of(ExternalId.of("DUMMY", "Dummy_C")));

  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final LegalEntitySource _legalEntitySource;
  private double _recoveryRate = 0.5;
  private final ZonedDateTime _valuationTime;

  public CreditDefaultSwapSecurityConverter(final HolidaySource holidaySource, final RegionSource regionSource, final LegalEntitySource legalEntitySource,
      final double recoveryRate, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(valuationTime, "valuation time" +
        "");
    //ArgumentChecker.notNull(legalEntitySource, "organization source");
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _legalEntitySource = legalEntitySource;
    _recoveryRate = recoveryRate;
    _valuationTime = valuationTime;
  }

  public CreditDefaultSwapSecurityConverter(final HolidaySource holidaySource, final RegionSource regionSource, final LegalEntitySource legalEntitySource, ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    //ArgumentChecker.notNull(legalEntitySource, "organization source");
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _legalEntitySource = legalEntitySource;
    _valuationTime = valuationTime;
  }

  public static com.opengamma.analytics.financial.legalentity.LegalEntity convert(LegalEntity legalEntity) {
    String ticker = legalEntity.getExternalIdBundle().getValue(ExternalScheme.of("TICKER"));
    String name = legalEntity.getName();
    final Set<com.opengamma.analytics.financial.legalentity.CreditRating> creditRatings = functional(legalEntity.getRatings()).map(
        new Function1<Rating, com.opengamma.analytics.financial.legalentity.CreditRating>() {
          @Override
          public com.opengamma.analytics.financial.legalentity.CreditRating execute(Rating rating) {
            return com.opengamma.analytics.financial.legalentity.CreditRating.of(rating.getScore().name(), rating.getRater(), true); //TODO check the long term flag
          }
        }).asSet();
    com.opengamma.analytics.financial.legalentity.Region region = legalEntity.getAttributes().get("region") != null ? com.opengamma.analytics.financial.legalentity.Region.of(legalEntity
        .getAttributes().get("region")) : null;
    com.opengamma.analytics.financial.legalentity.Sector sector = legalEntity.getAttributes().get("sector") != null ? com.opengamma.analytics.financial.legalentity.Sector.of(legalEntity
        .getAttributes().get("sector")) : null;
    return new com.opengamma.analytics.financial.legalentity.LegalEntity(ticker, name, creditRatings, sector, region);
  }

  @Override
  public LegacyVanillaCreditDefaultSwapDefinition visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final BuySellProtection buySellProtection = security.isBuy() ? BuySellProtection.BUY : BuySellProtection.SELL;
    //final ExternalId regionId = security.getRegionId();
    //final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, _regionSource.getHighestLevelRegion(regionId));
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getNotional().getCurrency());
    final ZonedDateTime startDate = security.getStartDate();
    //final ZonedDateTime effectiveDate = IMMDateLogic.getPrevIMMDate(_valuationTime.toLocalDate()).atStartOfDay(ZoneId.systemDefault());
    final ZonedDateTime effectiveDate = _valuationTime;
    final ZonedDateTime maturityDate = security.getMaturityDate();
    final PeriodFrequency couponFrequency = getPeriodFrequency(security.getCouponFrequency());
    final DayCount dayCount = security.getDayCount();
    final BusinessDayConvention businessDayConvention = security.getBusinessDayConvention();
    final boolean immAdjustMaturityDate = security.isImmAdjustMaturityDate();
    final boolean adjustEffectiveDate = security.isAdjustEffectiveDate();
    final boolean adjustMaturityDate = security.isAdjustMaturityDate();
    final InterestRateNotional notional = security.getNotional();
    final Currency currency = notional.getCurrency();
    final DebtSeniority debtSeniority = security.getDebtSeniority();
    final RestructuringClause restructuringClause = security.getRestructuringClause();
    final double amount = notional.getAmount();
    final boolean includeAccruedPremium = security.isIncludeAccruedPremium();
    final boolean protectionStart = security.isProtectionStart();
    final double quotedSpread = security.getQuotedSpread();
    final double premiumLegCoupon = security.getCoupon();
    final double upFrontAmount = security.getUpfrontAmount().getAmount();
    final StubType stubType = security.getStubType().toAnalyticsType();
    final ZonedDateTime cashSettlementDate = security.getCashSettlementDate();
    final boolean adjustCashSettlementDate = security.isAdjustCashSettlementDate();
    final double coupon = security.getCoupon();
    final LegalEntity protectionBuyer = getObligorForProtectionBuyer(security.getProtectionBuyer());
    final LegalEntity protectionSeller = getObligorForProtectionSeller(security.getProtectionSeller());
    final LegalEntity referenceEntity = getObligorForReferenceEntity(security.getReferenceEntity());

    return new LegacyVanillaCreditDefaultSwapDefinition(buySellProtection, convert(protectionBuyer), convert(protectionSeller), convert(referenceEntity), currency,
        debtSeniority, restructuringClause, calendar, startDate, effectiveDate, maturityDate, stubType,
        couponFrequency, dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate,
        amount, _recoveryRate, includeAccruedPremium, protectionStart, coupon);
  }

  @Override
  public LegacyVanillaCreditDefaultSwapDefinition visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final BuySellProtection buySellProtection = security.isBuy() ? BuySellProtection.BUY : BuySellProtection.SELL;
    //    final ExternalId regionId = security.getRegionId();
    //    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, _regionSource.getHighestLevelRegion(regionId));
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getNotional().getCurrency());
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime effectiveDate = security.getEffectiveDate(); //FOLLOWING.adjustDate(calendar, valuationDate.withHour(0).withMinute(0).withSecond(0).withNano(0).plusDays(1));
    final ZonedDateTime maturityDate = security.getMaturityDate();
    final PeriodFrequency couponFrequency = getPeriodFrequency(security.getCouponFrequency());
    final DayCount dayCount = security.getDayCount();
    final BusinessDayConvention businessDayConvention = security.getBusinessDayConvention();
    final boolean immAdjustMaturityDate = security.isImmAdjustMaturityDate();
    final boolean adjustEffectiveDate = security.isAdjustEffectiveDate();
    final boolean adjustMaturityDate = security.isAdjustMaturityDate();
    final InterestRateNotional notional = security.getNotional();
    final Currency currency = notional.getCurrency();
    final DebtSeniority debtSeniority = security.getDebtSeniority();
    final RestructuringClause restructuringClause = security.getRestructuringClause();
    final double amount = notional.getAmount();
    final boolean includeAccruedPremium = security.isIncludeAccruedPremium();
    final boolean protectionStart = security.isProtectionStart();
    final StubType stubType = security.getStubType().toAnalyticsType();
    final double parSpread = security.getParSpread();
    final LegalEntity protectionBuyer = getObligorForProtectionBuyer(security.getProtectionBuyer());
    final LegalEntity protectionSeller = getObligorForProtectionSeller(security.getProtectionSeller());
    final LegalEntity referenceEntity = getObligorForReferenceEntity(security.getReferenceEntity());
    return new LegacyVanillaCreditDefaultSwapDefinition(buySellProtection, convert(protectionBuyer), convert(protectionSeller), convert(referenceEntity), currency,
        debtSeniority, restructuringClause, calendar, startDate, effectiveDate, maturityDate, stubType,
        couponFrequency, dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate,
        amount, _recoveryRate, includeAccruedPremium, protectionStart, parSpread);
  }

  private PeriodFrequency getPeriodFrequency(final Frequency frequency) {
    if (frequency instanceof PeriodFrequency) {
      return (PeriodFrequency) frequency;
    }
    if (frequency instanceof SimpleFrequency) {
      return ((SimpleFrequency) frequency).toPeriodFrequency();
    }
    throw new OpenGammaRuntimeException("Can only handle PeriodFrequency and SimpleFrequency");
  }

  private LegalEntity getObligorForReferenceEntity(final ExternalId legalEntityId) {
    //TODO temporary fix until securities are reloaded with references to the org master
    if (legalEntityId.getScheme().getName().equals("DbLen") && _legalEntitySource != null) {
      return _legalEntitySource.get(UniqueId.of(legalEntityId.getScheme().getName(), legalEntityId.getValue()));
    } else {
      return DUMMY_OBLIGOR_C;
    }
  }

  private LegalEntity getObligorForProtectionBuyer(final ExternalId obligorId) {
    return DUMMY_OBLIGOR_A; //TODO fix this
  }

  private LegalEntity getObligorForProtectionSeller(final ExternalId obligorId) {
    return DUMMY_OBLIGOR_B; //TODO fix this
  }

  private com.opengamma.analytics.financial.credit.obligor.CreditRating getCreditRating(final com.opengamma.core.obligor.CreditRating ratingDb) {
    return com.opengamma.analytics.financial.credit.obligor.CreditRating.valueOf(ratingDb.name());
  }

  private com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys getCreditRating(final com.opengamma.core.obligor.CreditRatingMoodys ratingDb) {
    return com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys.valueOf(ratingDb.name());
  }

  private com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors getCreditRating(final com.opengamma.core.obligor.CreditRatingStandardAndPoors ratingDb) {
    return com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors.valueOf(ratingDb.name());
  }

  private com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch getCreditRating(final com.opengamma.core.obligor.CreditRatingFitch ratingDb) {
    return com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch.valueOf(ratingDb.name());
  }

  private com.opengamma.analytics.financial.credit.obligor.Region getRegion(final com.opengamma.core.obligor.Region regionDb) {
    return com.opengamma.analytics.financial.credit.obligor.Region.valueOf(regionDb.name());
  }

  private com.opengamma.analytics.financial.credit.obligor.Sector getSector(final com.opengamma.core.obligor.Sector sectorDb) {
    return com.opengamma.analytics.financial.credit.obligor.Sector.valueOf(sectorDb.name());
  }
}
