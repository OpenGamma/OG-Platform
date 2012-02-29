/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.rowparser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;

import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * This class parses vanilla swaps from the standard OG import fields
 */
public class SwapParser extends RowParser {

  private static final String ID_SCHEME = "SWAP_LOADER";

  //CSOFF
  protected String TRADE_DATE = "trade date";
  protected String EFFECTIVE_DATE = "effective date";
  protected String TERMINATION_DATE = "termination date";
  protected String PAY_FIXED = "pay fixed";

  protected String FIXED_LEG_CURRENCY = "fixed currency";
  protected String FIXED_LEG_NOTIONAL = "fixed notional (mm)";
  protected String FIXED_LEG_DAYCOUNT = "fixed daycount";
  protected String FIXED_LEG_BUS_DAY_CONVENTION = "fixed business day convention";
  protected String FIXED_LEG_FREQUENCY = "fixed frequency";
  protected String FIXED_LEG_REGION = "fixed region";
  protected String FIXED_LEG_RATE = "fixed rate";

  protected String FLOATING_LEG_CURRENCY = "floating currency";
  protected String FLOATING_LEG_NOTIONAL = "floating notional (mm)";
  protected String FLOATING_LEG_DAYCOUNT = "floating daycount";
  protected String FLOATING_LEG_BUS_DAY_CONVENTION = "floating business day convention";
  protected String FLOATING_LEG_FREQUENCY = "floating frequency";
  protected String FLOATING_LEG_REGION = "floating region";
  protected String FLOATING_LEG_RATE = "initial floating rate";
  protected String FLOATING_LEG_REFERENCE = "floating reference";
  //CSON
  
  public SwapParser(ToolContext toolContext) {
    super(toolContext);
  }

  public String[] getColumns() {
    return new String[] {TRADE_DATE, EFFECTIVE_DATE, TERMINATION_DATE, PAY_FIXED, 
      FIXED_LEG_CURRENCY, FIXED_LEG_NOTIONAL, FIXED_LEG_DAYCOUNT, FIXED_LEG_BUS_DAY_CONVENTION, FIXED_LEG_FREQUENCY, FIXED_LEG_REGION, FIXED_LEG_RATE,
      FLOATING_LEG_CURRENCY, FLOATING_LEG_NOTIONAL, FLOATING_LEG_DAYCOUNT, FLOATING_LEG_BUS_DAY_CONVENTION, FLOATING_LEG_FREQUENCY,
      FLOATING_LEG_REGION, FLOATING_LEG_RATE, FLOATING_LEG_REFERENCE};
  }

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> swapDetails) {
    // REVIEW jonathan 2010-08-03 --
    // Admittedly this looks a bit messy, but it's going to be less error-prone than relying on column indices.
    DayCount fixedDayCount = DayCountFactory.INSTANCE.getDayCount(getWithException(swapDetails, FIXED_LEG_DAYCOUNT));
    Frequency fixedFrequency = SimpleFrequencyFactory.INSTANCE.getFrequency(getWithException(swapDetails, FIXED_LEG_FREQUENCY));
    ExternalId fixedRegionIdentifier = RegionUtils.countryRegionId(Country.of(getWithException(swapDetails, FIXED_LEG_REGION)));
    BusinessDayConvention fixedBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(getWithException(swapDetails, FIXED_LEG_BUS_DAY_CONVENTION));
    Currency fixedCurrency = Currency.of(getWithException(swapDetails, FIXED_LEG_CURRENCY));
    double fixedNotionalAmount = Double.parseDouble(getWithException(swapDetails, FIXED_LEG_NOTIONAL));
    Notional fixedNotional = new InterestRateNotional(fixedCurrency, fixedNotionalAmount);
    double fixedRate = Double.parseDouble(getWithException(swapDetails, FIXED_LEG_RATE));
    FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(fixedDayCount, fixedFrequency, fixedRegionIdentifier, fixedBusinessDayConvention, fixedNotional, false, fixedRate);
    
    DayCount floatingDayCount = DayCountFactory.INSTANCE.getDayCount(getWithException(swapDetails, FLOATING_LEG_DAYCOUNT));
    Frequency floatingFrequency = SimpleFrequencyFactory.INSTANCE.getFrequency(getWithException(swapDetails, FLOATING_LEG_FREQUENCY));
    ExternalId floatingRegionIdentifier = RegionUtils.countryRegionId(Country.of(getWithException(swapDetails, FLOATING_LEG_REGION)));
    BusinessDayConvention floatingBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(getWithException(swapDetails, FLOATING_LEG_BUS_DAY_CONVENTION));
    Currency floatingCurrency = Currency.of(getWithException(swapDetails, FLOATING_LEG_CURRENCY));
    double floatingNotionalAmount = Double.parseDouble(getWithException(swapDetails, FLOATING_LEG_NOTIONAL));
    Notional floatingNotional = new InterestRateNotional(floatingCurrency, floatingNotionalAmount);
    // TODO: not sure that this actually does anything, or what identifier we're looking for - just invented something for now
    String floatingReferenceRate = getWithException(swapDetails, FLOATING_LEG_REFERENCE);
    ExternalId floatingReferenceRateIdentifier = ExternalId.of("Ref", floatingReferenceRate);
    
    FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(floatingDayCount, floatingFrequency,
        floatingRegionIdentifier, floatingBusinessDayConvention, floatingNotional, false, floatingReferenceRateIdentifier, FloatingRateType.IBOR);
    
    double floatingInitialRate = Double.parseDouble(getWithException(swapDetails, FLOATING_LEG_RATE));
    floatingLeg.setInitialFloatingRate(floatingInitialRate);
    
    LocalDateTime tradeDate = LocalDateTime.of(LocalDate.parse(getWithException(swapDetails, TRADE_DATE), CSV_DATE_FORMATTER), LocalTime.MIDNIGHT);
    LocalDateTime effectiveDate = LocalDateTime.of(LocalDate.parse(getWithException(swapDetails, EFFECTIVE_DATE), CSV_DATE_FORMATTER), LocalTime.MIDNIGHT);
    LocalDateTime terminationDate = LocalDateTime.of(LocalDate.parse(getWithException(swapDetails, TERMINATION_DATE), CSV_DATE_FORMATTER), LocalTime.MIDNIGHT);
    
    String fixedLegDescription = RATE_FORMATTER.format(fixedRate);
    String floatingLegDescription = floatingReferenceRate;
    
    boolean isPayFixed = Boolean.parseBoolean(getWithException(swapDetails, PAY_FIXED));
    SwapLeg payLeg;
    String payLegDescription;
    SwapLeg receiveLeg;
    String receiveLegDescription;
    if (isPayFixed) {
      payLeg = fixedLeg;
      payLegDescription = fixedLegDescription;
      receiveLeg = floatingLeg;
      receiveLegDescription = floatingLegDescription;
    } else {
      payLeg = floatingLeg;
      payLegDescription = floatingLegDescription;
      receiveLeg = fixedLeg;
      receiveLegDescription = fixedLegDescription;
    }
    
    SwapSecurity swap = new SwapSecurity(tradeDate.atZone(TimeZone.UTC), effectiveDate.atZone(TimeZone.UTC),
        terminationDate.atZone(TimeZone.UTC), "Cpty Name", payLeg, receiveLeg);
    
    // Assume notional / currencies are the same for both legs - the name is really just to give us something to display anyway
    swap.setName("IR Swap " + NOTIONAL_FORMATTER.format(fixedNotionalAmount) + " " + fixedCurrency + " " +
        terminationDate.toString(OUTPUT_DATE_FORMATTER) + " - " + payLegDescription + " / " + receiveLegDescription);
    
    swap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));

    ManageableSecurity[] result = {swap};
    return result;
  }
  
  @Override
  public Map<String, String> constructRow(ManageableSecurity security) {
    Map<String, String> result = new HashMap<String, String>();
    SwapSecurity swap = (SwapSecurity) security;

    // Populate common fields
    result.put(TRADE_DATE, swap.getTradeDate().toString(CSV_DATE_FORMATTER));
    result.put(EFFECTIVE_DATE, swap.getEffectiveDate().toString(CSV_DATE_FORMATTER));
    result.put(TERMINATION_DATE, swap.getMaturityDate().toString(CSV_DATE_FORMATTER));
    
    // Populate floating fields
    FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) 
        (swap.getReceiveLeg() instanceof FloatingInterestRateLeg ? swap.getReceiveLeg() : swap.getPayLeg());
    result.put(FLOATING_LEG_BUS_DAY_CONVENTION, floatingLeg.getBusinessDayConvention().getConventionName());
    result.put(FLOATING_LEG_DAYCOUNT, floatingLeg.getDayCount().getConventionName());
    result.put(FLOATING_LEG_FREQUENCY, floatingLeg.getFrequency().getConventionName());
    result.put(FLOATING_LEG_REFERENCE, floatingLeg.getFloatingReferenceRateId().getValue());
    if (floatingLeg.getInitialFloatingRate() != null) {
      result.put(FLOATING_LEG_RATE, Double.toString(floatingLeg.getInitialFloatingRate()));
    }
    if (floatingLeg.getNotional() instanceof InterestRateNotional) {
      InterestRateNotional notional = (InterestRateNotional) floatingLeg.getNotional();
      result.put(FLOATING_LEG_CURRENCY, notional.getCurrency().getCode());
      result.put(FLOATING_LEG_NOTIONAL, Double.toString(notional.getAmount()));
    }
    Set<Region> regions = RegionUtils.getRegions(getToolContext().getRegionSource(), floatingLeg.getRegionId());
    if (!regions.isEmpty()) {
      Region region = regions.iterator().next();
      if (region != null) {
        result.put(FLOATING_LEG_REGION, region.getFullName());
      }
    }
    
    // Populate fixed fields
    FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) 
        (swap.getReceiveLeg() instanceof FixedInterestRateLeg ? swap.getReceiveLeg() : swap.getPayLeg());
    result.put(FIXED_LEG_BUS_DAY_CONVENTION, fixedLeg.getBusinessDayConvention().getConventionName());
    result.put(FIXED_LEG_DAYCOUNT, fixedLeg.getDayCount().getConventionName());
    result.put(FIXED_LEG_FREQUENCY, fixedLeg.getFrequency().getConventionName());
    result.put(FIXED_LEG_RATE, Double.toString(fixedLeg.getRate()));
    if (floatingLeg.getNotional() instanceof InterestRateNotional) {
      InterestRateNotional notional = (InterestRateNotional) fixedLeg.getNotional();
      result.put(FIXED_LEG_CURRENCY, notional.getCurrency().getCode());
      result.put(FIXED_LEG_NOTIONAL, Double.toString(notional.getAmount()));
    }
    regions = RegionUtils.getRegions(getToolContext().getRegionSource(), fixedLeg.getRegionId());
    if (!regions.isEmpty()) {
      Region region = regions.iterator().next();
      if (region != null) {
        result.put(FIXED_LEG_REGION, region.getFullName());
      }
    }
    
    return result;
  }
}
