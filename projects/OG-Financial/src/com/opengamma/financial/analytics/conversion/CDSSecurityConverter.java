/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondSecurityDefinition;
import com.opengamma.analytics.financial.instrument.cds.CDSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleFactory;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts CDS from security objects to definition objects
 * @author Martin Traverse
 * @see CDSSecurity
 * @see CDSDefinition
 */
public class CDSSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  
  private final SecuritySource _securitySource;
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;
  
  private final BondSecurityConverter _bondConverter;
  
  public CDSSecurityConverter(final SecuritySource securitySource, final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _securitySource = securitySource;
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
    
    _bondConverter = new BondSecurityConverter(_holidaySource, _conventionSource, _regionSource);
  }
  
  // TODO: Lots of stuff to verify still in here!!!
  @Override
  public InstrumentDefinition<?> visitCDSSecurity(final CDSSecurity cds) {
    
    final BondSecurity bond = (BondSecurity) _securitySource.getSecurity(ExternalIdBundle.of(cds.getUnderlying()));
    if (bond == null) {
      throw new OpenGammaRuntimeException("No security found with identifiers " + cds.getUnderlying());
    }
    
    final String conventionName = "TODO: Find correct convention name"; // TODO: find correct convention name
    final ConventionBundle conventionBundle = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
    if (conventionBundle == null) {
      throw new OpenGammaRuntimeException("Convention called " + conventionName + " was null");
    }
    
    Calendar calendar = CalendarUtils.getCalendar(_holidaySource, cds.getCurrency(), bond.getCurrency());
    DayCount dayCount = conventionBundle.getDayCount();
    BusinessDayConvention convention = conventionBundle.getBusinessDayConvention();
    
    // TODO: Is settlement date the same as first premium date?
    final AnnuityCouponFixedDefinition premiumPayments = AnnuityCouponFixedDefinition.from(
      cds.getCurrency(), cds.getProtectionStartDate(), cds.getMaturity(), cds.getPremiumFrequency(),
      calendar, dayCount, convention, /* isEOM */ false,
      cds.getNotional(), cds.getSpread(), /* isPayer */ false
    );
    
    final AnnuityPaymentFixedDefinition defaultPayments = cds.getUnderlying() != null ? possibleDefaultPayments(cds, bond, calendar, convention) : null;
    
    return new CDSDefinition(premiumPayments, defaultPayments, cds.getProtectionStartDate(), cds.getMaturity(), cds.getNotional(), cds.getSpread(), cds.getRecoveryRate());
  }
  
  
  // Build a fixed payment annuity representing possible default payouts
  // Payout dates are coupon dates on the underlying bond that fall within the effective period of the CDS
  // If the CDS extends beyond the maturity of the bond, the CDS maturity date is included as an extra possible default date
  private AnnuityPaymentFixedDefinition possibleDefaultPayments(CDSSecurity cds, BondSecurity bond, Calendar calendar, BusinessDayConvention convention) {
    
    final double payoutAmmount = cds.getNotional() * (1.0 - cds.getRecoveryRate());
    
    // Extract the bond coupon schedule by converting the bond security object
    @SuppressWarnings("unchecked")
    final BondSecurityDefinition<PaymentDefinition, CouponDefinition> bondDefinition = (BondSecurityDefinition<PaymentDefinition, CouponDefinition>) bond.accept(_bondConverter);
    final AnnuityDefinition<CouponDefinition> bondCoupons = bondDefinition.getCoupon();
    
    int coveredCouponDates = 0;

    for (CouponDefinition coupon: bondCoupons.getPayments()) {
      if (!coupon.getPaymentDate().isBefore(cds.getProtectionStartDate()) && !coupon.getPaymentDate().isAfter(cds.getMaturity())) {
        ++coveredCouponDates;
      }
    }
    
    if (cds.getMaturity().isAfter(bond.getLastTradeDate().getExpiry())) {
      ++coveredCouponDates;
    }
    
    PaymentFixedDefinition[] payouts = new PaymentFixedDefinition[coveredCouponDates];
    int i = 0;
    
    for (CouponDefinition coupon: bondCoupons.getPayments()) {
      if (!coupon.getPaymentDate().isBefore(cds.getProtectionStartDate()) && !coupon.getPaymentDate().isAfter(cds.getMaturity())) {
        payouts[i++] = new PaymentFixedDefinition(cds.getCurrency(), coupon.getPaymentDate(), payoutAmmount);
      }
    }
    
    if (cds.getMaturity().isAfter(bond.getLastTradeDate().getExpiry())) {
      payouts[i++] = new PaymentFixedDefinition(cds.getCurrency(), convention.adjustDate(calendar, cds.getMaturity()), payoutAmmount);
    }
    
    return new AnnuityPaymentFixedDefinition(payouts);
  }

}
