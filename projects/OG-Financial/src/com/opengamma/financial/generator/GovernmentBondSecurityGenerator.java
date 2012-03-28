/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Source of random, but reasonable, bond security instances.
 */
public class GovernmentBondSecurityGenerator extends BondSecurityGenerator<GovernmentBondSecurity> {

  private static final class BondConvention {

    private final Currency _currency;
    private final String _issuerName;
    private final String _issuerType;
    private final String _issuerDomicile;
    private final String _market;
    private final YieldConvention _yieldConvention;
    private final String _couponType;
    private final Frequency _frequency;
    private final DayCount _dayCount;
    private final int _firstCoupon;
    private final String _prefix;
    private final int _rateLimit;
    private final double _redemption;

    private BondConvention(final Currency currency, final String issuerName, final String issuerType, final String issuerDomicile, final String market, final YieldConvention yieldConvention,
        final String couponType, final Frequency frequency, final DayCount dayCount, final int firstCoupon, final String prefix, final int rateLimit, final double redemption) {
      _currency = currency;
      _issuerName = issuerName;
      _issuerType = issuerType;
      _issuerDomicile = issuerDomicile;
      _market = market;
      _yieldConvention = yieldConvention;
      _couponType = couponType;
      _frequency = frequency;
      _dayCount = dayCount;
      _firstCoupon = firstCoupon;
      _prefix = prefix;
      _rateLimit = rateLimit;
      _redemption = redemption;
    }

  }

  private static final BondConvention[] CONVENTIONS = new BondConvention[] {
      new BondConvention(Currency.USD, "US TREASURY", "Sovereign", "US", "US GOVERNMENT", SimpleYieldConvention.US_STREET, "FIXED", SimpleFrequency.SEMI_ANNUAL,
          DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), 6, "T", 80, 0),
      new BondConvention(Currency.USD, "TSY INFL IX N/B", "Sovereign", "US", "US GOVERNMENT", SimpleYieldConvention.US_IL_REAL, "FIXED", SimpleFrequency.SEMI_ANNUAL,
          DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), 6, "TII", 32, 1),
      new BondConvention(Currency.GBP, "UK TREASURY", "Sovereign", "GB", "UK GILT STOCK", SimpleYieldConvention.UK_BUMP_DMO_METHOD, "FIXED", SimpleFrequency.SEMI_ANNUAL,
          DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"), 6, "UKT", 120, 0),
  };

  @Override
  public GovernmentBondSecurity createSecurity() {
    final BondConvention info = getRandom(CONVENTIONS);
    final int couponRate = getRandom(info._rateLimit);
    final int length = getRandomLength();
    final double elapsed = getRandom(0.2, 0.8); // Don't get too close to the start or end
    final ZonedDateTime interestAccrualDate = nextWorkingDay(ZonedDateTime.now().minusDays((int) (YEAR_LENGTH * (double) length * elapsed)), info._currency);
    final ZonedDateTime announcementDate = previousWorkingDay(interestAccrualDate.minusDays(7), info._currency);
    final Expiry lastTradeDate = new Expiry(previousWorkingDay(interestAccrualDate.plusDays((int) ((double) length * 365.25)), info._currency));
    final ZonedDateTime settlementDate = nextWorkingDay(interestAccrualDate.plusDays((int) (getRandom(0.3, 0.8) * elapsed * 365.25)), info._currency);
    final ZonedDateTime firstCouponDate = previousWorkingDay(interestAccrualDate.plusMonths(info._firstCoupon), info._currency);
    final double issuancePrice = getRandom(95.0, 101.0); // TODO: produce a more realistic value from a curve or something
    final double totalAmountIssued = (long) getRandom(1e5, 8e9);
    final double minimumAmount = (info._currency == Currency.GBP) ? 0.01 : 100;
    final double minimumIncrement = (info._currency == Currency.GBP) ? 0.01 : 100;
    final double parAmount = 100;
    final double redemptionValue = 100 + getRandom(info._redemption * (double) length * 0.75, info._redemption * (double) length * 1.25); // TODO: produce a more realistic value from a curve or something
    final GovernmentBondSecurity bond = new GovernmentBondSecurity(info._issuerName, info._issuerType, info._issuerDomicile, info._market, info._currency, info._yieldConvention, lastTradeDate,
        info._couponType, (double) couponRate / 8,
        info._frequency, info._dayCount, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond.setAnnouncementDate(announcementDate);
    bond.setName(createName(info._prefix, couponRate, lastTradeDate));
    return bond;
  }

}
