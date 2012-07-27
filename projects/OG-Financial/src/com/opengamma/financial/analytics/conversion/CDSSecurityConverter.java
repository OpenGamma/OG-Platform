/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

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
    
    final String conventionName = "TODO: Find correct convention name";
    final ConventionBundle conventions = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
    if (conventions == null) {
      throw new OpenGammaRuntimeException("Convention called " + conventionName + " was null");
    }
    
    Calendar calendar = CalendarUtils.getCalendar(_holidaySource, cds.getCurrency(), bond.getCurrency());
    DayCount dayCount = conventions.getDayCount();
    BusinessDayConvention bdConvention = conventions.getBusinessDayConvention();
    boolean isEOM = conventions.isEOMConvention();
    boolean isPayer = false;
    
    // TODO: Is settlement date the same as first premium date?
    AnnuityCouponFixedDefinition premiumPayments = AnnuityCouponFixedDefinition.from(
      cds.getCurrency(), cds.getFirstPremiumDate(), cds.getMaturity(), cds.getPremiumFrequency(),
      calendar, dayCount, bdConvention, isEOM,
      cds.getNotional(), cds.getPremiumRate(), isPayer
    );
    
    final double defaultPayoutAmmount = cds.getNotional() * (1.0 - cds.getRecoveryRate());
    BondSecurityDefinition<PaymentDefinition, CouponDefinition> bondDefinition = (BondSecurityDefinition<PaymentDefinition, CouponDefinition>) bond.accept(_bondConverter);
    AnnuityDefinition<CouponDefinition> bondCoupons = bondDefinition.getCoupon();
    
    // TODO: verify
    boolean includeCdsMaturityDate = cds.getMaturity().isAfter(bondCoupons.getNthPayment(bondCoupons.getNumberOfPayments()-1).getPaymentDate());

    PaymentFixedDefinition[] possibleDefaultPayouts = new PaymentFixedDefinition[ includeCdsMaturityDate
      ? bondCoupons.getNumberOfPayments()
      : bondCoupons.getNumberOfPayments() + 1
    ];
    
    for (int i = 0; i < bondCoupons.getNumberOfPayments(); ++i) {
      possibleDefaultPayouts[i] = new PaymentFixedDefinition(cds.getCurrency(), bondCoupons.getNthPayment(i).getPaymentDate(), defaultPayoutAmmount);
    }
    
    if (includeCdsMaturityDate)  {
      possibleDefaultPayouts[possibleDefaultPayouts.length-1] =  new PaymentFixedDefinition(cds.getCurrency(), cds.getMaturity(), defaultPayoutAmmount);
    }
    
    AnnuityPaymentFixedDefinition possibleDefaultPayments = new AnnuityPaymentFixedDefinition(possibleDefaultPayouts);
    
    return new CDSDefinition(premiumPayments, possibleDefaultPayments, cds.getRecoveryRate());
  }

}
