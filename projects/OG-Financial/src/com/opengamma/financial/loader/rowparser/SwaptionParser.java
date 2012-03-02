/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.rowparser;

import java.util.HashMap;
import java.util.Map;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * This class uses standard OG import fields and convention bundles to generate a Swaption security
 */
public class SwaptionParser extends RowParser {

  private static final String ID_SCHEME = "SWAPTION_LOADER";
  
  //CSOFF
  protected String EXPIRY = "expiry";
  protected String IS_LONG = "long";
  protected String IS_PAYER = "payer";
  protected String CURRENCY = "currency";
  protected String TRADE_DATE = "trade date";
  //public String PREMIUM_DATE = "premium date";
  //public String PREMIUM_AMOUNT = "premium amount";
  protected String STRIKE = "strike";
  protected String NOTIONAL = "notional";
  protected String COUNTERPARTY = "counterparty";
  protected String SWAP_LENGTH = "swap length"; 
  //CSON
  
  private static final ConventionBundleSource CONVENTIONS = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());

  public SwaptionParser(ToolContext toolContext) {
    super(toolContext);
  }

  public String[] getColumns() {
    return new String[] {EXPIRY, IS_LONG, IS_PAYER, CURRENCY, TRADE_DATE, STRIKE, NOTIONAL, COUNTERPARTY, SWAP_LENGTH };
  }

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> swaptionDetails) {
    
    String counterparty = getWithException(swaptionDetails, COUNTERPARTY);
    Currency currency = Currency.of(getWithException(swaptionDetails, CURRENCY));
    ConventionBundle swaptionConvention = CONVENTIONS.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_SWAPTION")); 
    ConventionBundle swapConvention = CONVENTIONS.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_SWAP"));
    final ConventionBundle floatingRateConvention = CONVENTIONS.getConventionBundle(swapConvention.getSwapFloatingLegInitialRate());
    Expiry swaptionExpiry = new Expiry(
        ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(getWithException(swaptionDetails, EXPIRY), CSV_DATE_FORMATTER), LocalTime.MIDNIGHT), TimeZone.UTC), 
        ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    
    if (swapConvention == null || swaptionConvention == null || floatingRateConvention == null) {
      throw new OpenGammaRuntimeException("Swaption parser failed to fetch one or more convention bundles required to populate a security");
    }
    
    boolean isLong = Boolean.parseBoolean(getWithException(swaptionDetails, IS_LONG));
    boolean isCashSettled = swaptionConvention.isCashSettled();    
    boolean isPayer = Boolean.parseBoolean(getWithException(swaptionDetails, IS_PAYER));
    double strike = Double.parseDouble(getWithException(swaptionDetails, STRIKE));
    double notional = 1000000 * Math.abs(Double.parseDouble(getWithException(swaptionDetails, NOTIONAL)));
    InterestRateNotional fixedNotional = new InterestRateNotional(currency, notional);
    InterestRateNotional floatingNotional = new InterestRateNotional(currency, notional);
    final ExternalId floatingRateBloombergTicker = floatingRateConvention.getIdentifiers().getExternalId(SecurityUtils.BLOOMBERG_TICKER);

    FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(swapConvention.getSwapFixedLegDayCount(), 
        swapConvention.getSwapFixedLegFrequency(), 
        swapConvention.getSwapFixedLegRegion(), 
        swapConvention.getSwapFixedLegBusinessDayConvention(), 
        fixedNotional, 
        false, strike);
    
    FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(swapConvention.getSwapFloatingLegDayCount(), 
        swapConvention.getSwapFloatingLegFrequency(), 
        swapConvention.getSwapFloatingLegRegion(), 
        swapConvention.getSwapFloatingLegBusinessDayConvention(), 
        floatingNotional,
        false, floatingRateBloombergTicker,
        FloatingRateType.IBOR);
    
    ZonedDateTime swapTradeDate = swaptionExpiry.getExpiry();
    ZonedDateTime swapEffectiveDate = swaptionExpiry.getExpiry();
    String swapLength = getWithException(swaptionDetails, SWAP_LENGTH);
    Period swapMaturity = Period.ofYears(Integer.parseInt(swapLength));
    ZonedDateTime swapMaturityDate = swaptionExpiry.getExpiry().plus(swapMaturity);
    
    SwapSecurity swap = new SwapSecurity(swapTradeDate, swapEffectiveDate, swapMaturityDate, counterparty, floatingLeg, fixedLeg);
    ExternalId swapIdentifier = ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString());
    swap.addExternalId(swapIdentifier);
    
    LocalDate tradeDate = LocalDate.parse(getWithException(swaptionDetails, TRADE_DATE), CSV_DATE_FORMATTER);

    SwaptionSecurity swaption = new SwaptionSecurity(isPayer, swapIdentifier, isLong, swaptionExpiry, isCashSettled, currency);
    swaption.setName("Vanilla swaption, " + getSwaptionString(swapLength, tradeDate, swaptionExpiry.getExpiry()) + ", " + currency.getCode()
        + " " + NOTIONAL_FORMATTER.format(notional) + " @ " + 
        RATE_FORMATTER.format(strike));

    swaption.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    
    ManageableSecurity[] result = {swaption, swap};
    return result;
  }

  private static String getSwaptionString(String swapLength, LocalDate tradeDate, ZonedDateTime expiry) {
    long daysBetween = DateUtils.getDaysBetween(tradeDate, expiry);
    if (daysBetween < 365) {
      int months = (int) (daysBetween / 12.);
      return months + "M x " + swapLength + "Y";
    }
    int years = (int) (daysBetween / 365);
    return years + "Y x " + swapLength + "Y";
  }

  // TODO implement constructRow properly, with underlying

  @Override
  public Map<String, String> constructRow(ManageableSecurity security) {
    Map<String, String> result = new HashMap<String, String>();
    SwaptionSecurity swaption = (SwaptionSecurity) security;

    // Swaption fields
    result.put(EXPIRY, swaption.getExpiry().getExpiry().toString(CSV_DATE_FORMATTER));
    result.put(IS_LONG, Boolean.toString(swaption.isLong()));
    result.put(IS_PAYER, Boolean.toString(swaption.isPayer()));
    result.put(CURRENCY, swaption.getCurrency().getCode());
    
    // Swap fields
//    result.put(STRIKE, );
//    result.put(NOTIONAL, );
//    result.put(COUNTERPARTY, );
//    result.put(SWAP_LENGTH, );
    
    return result;
  }

}
