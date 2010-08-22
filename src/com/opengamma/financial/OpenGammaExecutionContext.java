/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.historicaldata.HistoricalDataProvider;
import com.opengamma.engine.historicaldata.HistoricalDataSource;
import com.opengamma.engine.security.SecuritySource;

/**
 * Utility methods to pull standard objects out of a {@link FunctionExecutionContext}.
 *
 * @author pietari
 */
public class OpenGammaExecutionContext {
  
  /**
   * The name under which an instance of {@link HistoricalDataProvider} should be bound.
   */
  public static final String HISTORICAL_DATA_SOURCE_NAME = "historicalDataSource";

  /**
   * The name under which an instance of {@link RegionRepository} should be bound.
   */
  public static final String REGION_REPOSITORY_NAME = "regionRepository";

  /**
   * The name under which an instance of {@link HolidayRepository} should be bound.
   */
  public static final String HOLIDAY_SOURCE_NAME = "holidaySource";
  
  /**
   * The name under which an instance of {@link ReferenceRateRepository} should be bound.
   */
  public static final String REFERENCE_RATE_REPOSITORY_NAME = "referenceRateRepository";
  
  /**
   * The name under which an instance of {@link ExchangeRepository} should be bound.
   */
  public static final String EXCHANGE_REPOSITORY_NAME = "exchangeRespotiroy";
  
  public static HistoricalDataSource getHistoricalDataSource(FunctionExecutionContext context) {
    return (HistoricalDataSource) context.get(HISTORICAL_DATA_SOURCE_NAME);
  }
  
  public static void setHistoricalDataSource(FunctionExecutionContext context, 
      HistoricalDataSource historicalDataSource) {
    context.put(HISTORICAL_DATA_SOURCE_NAME, historicalDataSource);
  }
  
  public static SecuritySource getSecuritySource(FunctionExecutionContext context) {
    return context.getSecuritySource();
  }
  
  public static void setSecuritySource(FunctionExecutionContext context, SecuritySource secSource) {
    context.setSecuritySource(secSource);
  }
  
  public static RegionRepository getRegionRepository(FunctionExecutionContext context) {
    return (RegionRepository) context.get(REGION_REPOSITORY_NAME);
  }
  
  public static void setRegionRepository(FunctionExecutionContext context, RegionRepository regionRepository) {
    context.put(REGION_REPOSITORY_NAME, regionRepository);
  }
  
  public static HolidaySource getHolidaySource(FunctionExecutionContext context) {
    return (HolidaySource) context.get(HOLIDAY_SOURCE_NAME);
  }
  
  public static void setHolidaySource(FunctionExecutionContext context, HolidaySource holidayRepository) {
    context.put(HOLIDAY_SOURCE_NAME, holidayRepository);
  }
  
  public static ReferenceRateRepository getReferenceRateRepository(FunctionExecutionContext context) {
    return (ReferenceRateRepository) context.get(REFERENCE_RATE_REPOSITORY_NAME);
  }
  
  public static void setReferenceRateRepository(FunctionExecutionContext context, ReferenceRateRepository referenceRateRepository) {
    context.put(REFERENCE_RATE_REPOSITORY_NAME, referenceRateRepository);
  }
  
  public static ExchangeRepository getExchangeRepository(FunctionExecutionContext context) {
    return (ExchangeRepository) context.get(EXCHANGE_REPOSITORY_NAME);
  }
  
  public static void setExchangeRepository(FunctionExecutionContext context, ExchangeRepository exchangeRepository) {
    context.put(EXCHANGE_REPOSITORY_NAME, exchangeRepository);
  }
}
