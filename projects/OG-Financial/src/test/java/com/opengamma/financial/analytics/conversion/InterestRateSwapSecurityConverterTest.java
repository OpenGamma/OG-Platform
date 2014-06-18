package com.opengamma.financial.analytics.conversion;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.annuity.AbstractAnnuityDefinitionBuilder.CouponStub;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.core.AbstractSource;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.analytics.curve.TestConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.StubCalculationMethod;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

public class InterestRateSwapSecurityConverterTest {

  private static final MondayToFridayCalendar CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final String SCHEME = "Test";
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  private static final DayCount THIRTY_360 = DayCounts.THIRTY_U_360;
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");
  private static final String TICKER = "TICKER";
  private static final ExternalId FIXED_LEG_ID = ExternalId.of(SCHEME, "USD Swap Fixed Leg");
  private static final ExternalId FIXED_LEG_PAY_LAG_ID = ExternalId.of(SCHEME, "USD Swap Fixed Leg Pay Lag");
  private static final String USDLIBOR_ACT_360_CONVENTION_NAME = "USD Libor ACT/360";
  private static final ExternalId USDLIBOR_ACT_360_CONVENTION_ID = ExternalId.of(SCHEME, USDLIBOR_ACT_360_CONVENTION_NAME);
  private static final String USDLIBOR_30_360_CONVENTION_NAME = "USD Libor 30/360";
  private static final ExternalId USDLIBOR_30_360_ID = ExternalId.of(SCHEME, USDLIBOR_30_360_CONVENTION_NAME);
  private static final ExternalId LEG_USDLIBOR3M_ID = ExternalId.of(SCHEME, "USD 3m Floating Leg");
  private static final ExternalId LEG_USDLIBOR6M_ID = ExternalId.of(SCHEME, "USD 6m Floating Leg");  
  
  // LIBOR Index
  private static final String USDLIBOR1M_NAME = "USDLIBOR1M";
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR1M = 
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR1M_NAME, "ICE LIBOR 1M - USD", Tenor.ONE_MONTH, USDLIBOR_ACT_360_CONVENTION_ID);
  private static final ExternalId USDLIBOR1M_ID = ExternalId.of(TICKER, "USDLIBOR1M");
  private static final String USDLIBOR3M_NAME = "USDLIBOR3M";
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR3M = 
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR3M_NAME, "ICE LIBOR 3M - USD", Tenor.THREE_MONTHS, USDLIBOR_ACT_360_CONVENTION_ID);
  private static final ExternalId USDLIBOR3M_ID = ExternalId.of(TICKER, "USDLIBOR3M");
  private static final String USDLIBOR6M_NAME = "USDLIBOR6M";
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR6M = 
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR6M_NAME, "ICE LIBOR 6M - USD", Tenor.SIX_MONTHS, USDLIBOR_ACT_360_CONVENTION_ID);
  private static final ExternalId USDLIBOR6M_ID = ExternalId.of(TICKER, "USDLIBOR6M");
  private static final SwapFixedLegConvention FIXED_LEG = new SwapFixedLegConvention("USD Swap Fixed Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Swap Fixed Leg")),
      Tenor.SIX_MONTHS, ACT_360, MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, false, StubType.NONE, false, 0);
  private static final SwapFixedLegConvention FIXED_LEG_PAY_LAG = new SwapFixedLegConvention("USD Swap Fixed Leg Pay Lag", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Swap Fixed Leg")),
      Tenor.SIX_MONTHS, ACT_360, MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, false, StubType.NONE, false, 2);
  private static final VanillaIborLegConvention LEG_USDLIBOR3M = new VanillaIborLegConvention("USD 3m Floating Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD 3m Floating Leg")),
      USDLIBOR3M_ID, false, SCHEME, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 2);
  private static final VanillaIborLegConvention LEG_USDLIBOR6M = new VanillaIborLegConvention("USD 6m Floating Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD 6m Floating Leg")),
      USDLIBOR6M_ID, false, SCHEME, Tenor.SIX_MONTHS, 2, false, StubType.LONG_END, false, 2);
  private static final IborIndexConvention USDLIBOR_ACT_360 = new IborIndexConvention(USDLIBOR_ACT_360_CONVENTION_NAME, ExternalIdBundle.of(USDLIBOR_ACT_360_CONVENTION_ID),
      ACT_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  private static final IborIndexConvention LIBOR_30_360 = new IborIndexConvention(USDLIBOR_30_360_CONVENTION_NAME, ExternalIdBundle.of(USDLIBOR_30_360_ID),
      THIRTY_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");

  

  private static final Map<ExternalId, Convention> CONVENTIONS = new HashMap<>();
  private static final Map<ExternalIdBundle, Security> SECURITY_MAP = new HashMap<>();
  private static final SecuritySource SECURITY_SOURCE;
  private static final ConventionSource CONVENTION_SOURCE;
  private static final HolidaySource HOLIDAY_SOURCE;
  
  static {
    CONVENTIONS.put(FIXED_LEG_ID, FIXED_LEG);
    CONVENTIONS.put(FIXED_LEG_PAY_LAG_ID, FIXED_LEG_PAY_LAG);
    CONVENTIONS.put(USDLIBOR_ACT_360_CONVENTION_ID, USDLIBOR_ACT_360);
    CONVENTIONS.put(USDLIBOR_30_360_ID, LIBOR_30_360);
    CONVENTIONS.put(LEG_USDLIBOR3M_ID, LEG_USDLIBOR3M);
    CONVENTIONS.put(LEG_USDLIBOR6M_ID, LEG_USDLIBOR6M);

    // Security map. Used for index.
    SECURITY_MAP.put(USDLIBOR1M_ID.toBundle(), USDLIBOR1M);
    SECURITY_MAP.put(USDLIBOR3M_ID.toBundle(), USDLIBOR3M);
    SECURITY_MAP.put(USDLIBOR6M_ID.toBundle(), USDLIBOR6M);

    SECURITY_SOURCE = new MySecuritySource(SECURITY_MAP);
    CONVENTION_SOURCE = new TestConventionSource(CONVENTIONS);
    HOLIDAY_SOURCE = new MyHolidaySource();
  }


  @Test
  public void testShortStartStubCouponResolution() {
    
    StubCalculationMethod.Builder shortStubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_START)
        .firstStubEndDate(LocalDate.of(2014, 6, 18))
        .firstStubStartReferenceRateId(ExternalId.of(TICKER, USDLIBOR1M_NAME))
        .firstStubEndReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 1000000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();
            
    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setStubCalculationMethod(shortStubBuilder.build());
    
    legs.add(receiveLeg);
      
    InterestRateSwapSecurityConverter conv = new InterestRateSwapSecurityConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE, SECURITY_SOURCE);    
    Pair<CouponStub, CouponStub> stubs = conv.parseStubs(receiveLeg.getStubCalculationMethod());
    CouponStub firstStub = stubs.getFirst();
    
    IborIndex firstIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR1M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    IborIndex secondIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR3M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    StubType stubType = StubType.SHORT_START;
            
    Assert.assertEquals(firstStub.getStubType(), stubType);
    Assert.assertEquals(firstStub.getFirstIborIndex(), firstIborIndex);
    Assert.assertEquals(firstStub.getSecondIborIndex(), secondIborIndex);
  }
  
  @Test
  public void testLongStartStubCouponResolution() {
    
    StubCalculationMethod.Builder shortStubBuilder = StubCalculationMethod.builder()
        .type(StubType.LONG_START)
        .firstStubEndDate(LocalDate.of(2014, 6, 18))
        .firstStubStartReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME))
        .firstStubEndReferenceRateId(ExternalId.of(TICKER, USDLIBOR6M_NAME));
    
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 1000000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();
            
    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setStubCalculationMethod(shortStubBuilder.build());
    
    legs.add(receiveLeg);
      
    InterestRateSwapSecurityConverter conv = new InterestRateSwapSecurityConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE, SECURITY_SOURCE);    
    Pair<CouponStub, CouponStub> stubs = conv.parseStubs(receiveLeg.getStubCalculationMethod());
    CouponStub firstStub = stubs.getFirst();
    
    IborIndex firstIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR3M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    IborIndex secondIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR6M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    StubType stubType = StubType.LONG_START;
            
    Assert.assertEquals(firstStub.getStubType(), stubType);
    Assert.assertEquals(firstStub.getFirstIborIndex(), firstIborIndex);
    Assert.assertEquals(firstStub.getSecondIborIndex(), secondIborIndex);
  }
  
  @Test
  public void testShortEndStubCouponResolution() {
    
    StubCalculationMethod.Builder shortStubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_END)
        .lastStubEndDate(LocalDate.of(2014, 6, 18))
        .lastStubStartReferenceRateId(ExternalId.of(TICKER, USDLIBOR1M_NAME))
        .lastStubEndReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 1000000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();
            
    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setStubCalculationMethod(shortStubBuilder.build());
    
    legs.add(receiveLeg);
      
    InterestRateSwapSecurityConverter conv = new InterestRateSwapSecurityConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE, SECURITY_SOURCE);    
    Pair<CouponStub, CouponStub> stubs = conv.parseStubs(receiveLeg.getStubCalculationMethod());
    CouponStub secondStub = stubs.getSecond();
    
    IborIndex firstIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR1M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    IborIndex secondIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR3M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    StubType stubType = StubType.SHORT_END;
            
    Assert.assertEquals(secondStub.getStubType(), stubType);
    Assert.assertEquals(secondStub.getFirstIborIndex(), firstIborIndex);
    Assert.assertEquals(secondStub.getSecondIborIndex(), secondIborIndex);
  }
  
  @Test
  public void testLongEndStubCouponResolution() {
    
    StubCalculationMethod.Builder shortStubBuilder = StubCalculationMethod.builder()
        .type(StubType.LONG_END)
        .lastStubEndDate(LocalDate.of(2014, 6, 18))
        .lastStubStartReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME))
        .lastStubEndReferenceRateId(ExternalId.of(TICKER, USDLIBOR6M_NAME));
    
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 1000000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();
            
    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setStubCalculationMethod(shortStubBuilder.build());
    
    legs.add(receiveLeg);
      
    InterestRateSwapSecurityConverter conv = new InterestRateSwapSecurityConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE, SECURITY_SOURCE);    
    Pair<CouponStub, CouponStub> stubs = conv.parseStubs(receiveLeg.getStubCalculationMethod());
    CouponStub secondStub = stubs.getSecond();
    
    IborIndex firstIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR3M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    IborIndex secondIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR6M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    StubType stubType = StubType.LONG_END;
            
    Assert.assertEquals(secondStub.getStubType(), stubType);
    Assert.assertEquals(secondStub.getFirstIborIndex(), firstIborIndex);
    Assert.assertEquals(secondStub.getSecondIborIndex(), secondIborIndex);
  }
  
  @Test
  public void testBothStubCouponResolution() {
    
    StubCalculationMethod.Builder shortStubBuilder = StubCalculationMethod.builder()
        .type(StubType.BOTH)
        .firstStubEndDate(LocalDate.of(2014,6,18))        
        .firstStubStartReferenceRateId(ExternalId.of(TICKER, USDLIBOR1M_NAME))
        .firstStubEndReferenceRateId(ExternalId.of(TICKER, USDLIBOR6M_NAME))
        .lastStubEndDate(LocalDate.of(2014, 6, 18))
        .lastStubStartReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME))
        .lastStubEndReferenceRateId(ExternalId.of(TICKER, USDLIBOR6M_NAME));
    
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 1000000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();
            
    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setStubCalculationMethod(shortStubBuilder.build());
    
    legs.add(receiveLeg);
      
    InterestRateSwapSecurityConverter conv = new InterestRateSwapSecurityConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE, SECURITY_SOURCE);    
    Pair<CouponStub, CouponStub> stubs = conv.parseStubs(receiveLeg.getStubCalculationMethod());
    CouponStub firstStub = stubs.getFirst();
    CouponStub secondStub = stubs.getSecond();
    
    IborIndex firstStartIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR1M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    IborIndex firstEndIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR6M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    
    IborIndex lastStartIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR3M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);
    IborIndex lastEndIborIndex = (IborIndex) getIborIndex(ExternalId.of(TICKER, USDLIBOR6M_NAME), SECURITY_SOURCE, CONVENTION_SOURCE);    
    
    StubType stubType = StubType.BOTH;
            
    Assert.assertEquals(firstStub.getStubType(), stubType);
    Assert.assertEquals(firstStub.getFirstIborIndex(), firstStartIborIndex);
    Assert.assertEquals(firstStub.getSecondIborIndex(), firstEndIborIndex);
    
    Assert.assertEquals(secondStub.getStubType(), stubType);
    Assert.assertEquals(secondStub.getFirstIborIndex(), lastStartIborIndex);
    Assert.assertEquals(secondStub.getSecondIborIndex(), lastEndIborIndex);
  }
  
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testStubBadlyDefinedShortStart() {
    
    StubCalculationMethod.Builder shortStubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_START)
        .firstStubStartReferenceRateId(ExternalId.of(TICKER, USDLIBOR1M_NAME));
    
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 1000000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();
            
    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setStubCalculationMethod(shortStubBuilder.build());
    
    legs.add(receiveLeg);
      
    InterestRateSwapSecurityConverter conv = new InterestRateSwapSecurityConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE, SECURITY_SOURCE);    
    Pair<CouponStub, CouponStub> stubs = conv.parseStubs(receiveLeg.getStubCalculationMethod());
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testStubBadlyDefinedShortLast() {
    
    StubCalculationMethod.Builder shortStubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_END)
        .lastStubStartReferenceRateId(ExternalId.of(TICKER, USDLIBOR1M_NAME));
    
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 1000000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();
            
    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setStubCalculationMethod(shortStubBuilder.build());
    
    legs.add(receiveLeg);
      
    InterestRateSwapSecurityConverter conv = new InterestRateSwapSecurityConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE, SECURITY_SOURCE);    
    Pair<CouponStub, CouponStub> stubs = conv.parseStubs(receiveLeg.getStubCalculationMethod());
  }    
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testStubBadlyDefinedLongStart() {
    
    StubCalculationMethod.Builder shortStubBuilder = StubCalculationMethod.builder()
        .type(StubType.LONG_START)
        .firstStubStartReferenceRateId(ExternalId.of(TICKER, USDLIBOR1M_NAME));
    
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 1000000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();
            
    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setStubCalculationMethod(shortStubBuilder.build());
    
    legs.add(receiveLeg);
      
    InterestRateSwapSecurityConverter conv = new InterestRateSwapSecurityConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE, SECURITY_SOURCE);    
    Pair<CouponStub, CouponStub> stubs = conv.parseStubs(receiveLeg.getStubCalculationMethod());
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testStubUndefinedIndexId() {
    
    StubCalculationMethod.Builder shortStubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_END)
        .lastStubStartReferenceRateId(ExternalId.of(TICKER, "ZARJIBAR3M"))
        .lastStubEndReferenceRateId(ExternalId.of(TICKER, "ZARJIBAR12M"));
    
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 1000000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();
            
    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of(TICKER, USDLIBOR3M_NAME));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setStubCalculationMethod(shortStubBuilder.build());
    
    legs.add(receiveLeg);
      
    InterestRateSwapSecurityConverter conv = new InterestRateSwapSecurityConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE, SECURITY_SOURCE);    
    Pair<CouponStub, CouponStub> stubs = conv.parseStubs(receiveLeg.getStubCalculationMethod());
  }
    
  private IndexDeposit getIborIndex(ExternalId indexId, SecuritySource securitySource, ConventionSource conventionSource) {
    // try security lookup
    final Security sec = securitySource.getSingle(indexId.toBundle());
    if (sec != null) {
      final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
      IborIndexConvention indexConvention = conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
      return ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
    }
    
    // Fallback to convention lookup for old behaviour
    Convention iborLegConvention = conventionSource.getSingle(indexId);
    if (!(iborLegConvention instanceof VanillaIborLegConvention)) {
      throw new OpenGammaRuntimeException("Could not resolve an index convention for rate reference id: " + indexId.getValue());
    }
    Convention iborConvention = conventionSource.getSingle(((VanillaIborLegConvention) iborLegConvention).getIborIndexConvention());
    if (iborConvention == null) {
      throw new OpenGammaRuntimeException("Convention not found for " + ((VanillaIborLegConvention) iborLegConvention).getIborIndexConvention());
    }
    IborIndexConvention iborIndexConvention = (IborIndexConvention) iborConvention;

    return new IborIndex(iborIndexConvention.getCurrency(),
        ((VanillaIborLegConvention) iborLegConvention).getResetTenor().getPeriod(),
        iborIndexConvention.getSettlementDays(),  // fixing lag
        iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(),
        ((IborIndexConvention) iborConvention).isIsEOM(),
        indexId.getValue());
  }

  /**
   * A simplified local version of a HolidaySource for tests.
   */
  private static class MySecuritySource implements SecuritySource {
    
    /** Security source as a map for tests **/
    private final Map<ExternalIdBundle, Security> _map;
    
    /**
     * @param map The map of id/Security
     */
    public MySecuritySource(Map<ExternalIdBundle, Security> map) {
      super();
      _map = map;
    }

    @Override
    public Collection<Security> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<ExternalIdBundle, Collection<Security>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Collection<Security> get(ExternalIdBundle bundle) {
      return null;
    }

    @Override
    public Security getSingle(ExternalIdBundle bundle) {
      return _map.get(bundle);
    }

    @Override
    public Security getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<ExternalIdBundle, Security> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Security get(UniqueId uniqueId) {
      return null;
    }

    @Override
    public Security get(ObjectId objectId, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<UniqueId, Security> get(Collection<UniqueId> uniqueIds) {
      return null;
    }

    @Override
    public Map<ObjectId, Security> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public ChangeManager changeManager() {
      return null;
    }
    
  }

  private static class MyHolidaySource extends AbstractSource<Holiday> implements HolidaySource {
    private static final Calendar WEEKEND_HOLIDAY = new MondayToFridayCalendar("D");
    
    @Override
    public Holiday get(final UniqueId uniqueId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
      return WEEKEND_HOLIDAY.isWorkingDay(dateToCheck);
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
        final ExternalIdBundle regionOrExchangeIds) {
      return WEEKEND_HOLIDAY.isWorkingDay(dateToCheck);
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
        final ExternalId regionOrExchangeId) {
      return WEEKEND_HOLIDAY.isWorkingDay(dateToCheck);
    }

    @Override
    public Map<UniqueId, Holiday> get(Collection<UniqueId> uniqueIds) {
      return null;
    }

    @Override
    public Map<ObjectId, Holiday> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
      return null;
    }

  }
}
