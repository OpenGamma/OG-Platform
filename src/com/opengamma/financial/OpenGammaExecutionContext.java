/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.historicaldata.HistoricalDataSource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.world.RegionSource;

/**
 * Utility methods to pull standard objects out of a {@link FunctionExecutionContext}.
 *
 * @author pietari
 */
public class OpenGammaExecutionContext {
  
  /**
   * The name under which an instance of {@link TimeSeriesSource} should be bound.
   */
  public static final String HISTORICAL_DATA_PROVIDER_NAME = "historicalDataProvider";

  /**
   * The name under which an instance of {@link RegionRepository} should be bound.
   */
  public static final String REGION_SOURCE_NAME = "regionRepository";

  /**
   * The name under which an instance of {@link HolidayRepository} should be bound.
   */
  public static final String HOLIDAY_SOURCE_NAME = "holidaySource";
  
  /**
   * The name under which an instance of {@link ConventionBundleMaster} should be bound.
   */
  public static final String REFERENCE_RATE_REPOSITORY_NAME = "referenceRateRepository";
  
  /**
   * The name under which an instance of {@link ExchangeRepository} should be bound.
   */
  public static final String EXCHANGE_REPOSITORY_NAME = "exchangeRepository";

  private static final String CONVENTION_BUNDLE_SOURCE_NAME = "conventionBundleSource";;
  
  public static HistoricalDataSource getHistoricalDataProvider(FunctionExecutionContext context) {
    return (HistoricalDataSource) context.get(HISTORICAL_DATA_PROVIDER_NAME);
  }
  
  public static void setHistoricalDataProvider(FunctionExecutionContext context, 
      HistoricalDataSource historicalDataProvider) {
    context.put(HISTORICAL_DATA_PROVIDER_NAME, historicalDataProvider);
  }
  
  public static SecuritySource getSecuritySource(FunctionExecutionContext context) {
    return context.getSecuritySource();
  }
  
  public static void setSecuritySource(FunctionExecutionContext context, SecuritySource secSource) {
    context.setSecuritySource(secSource);
  }
  
  public static ConventionBundleSource getConventionBundleSource(FunctionExecutionContext context) {
    return (ConventionBundleSource) context.get(CONVENTION_BUNDLE_SOURCE_NAME);
  }
  
  public static void setConventionBundleSource(FunctionExecutionContext context, ConventionBundleSource conventionBundleSource) {
    context.put(CONVENTION_BUNDLE_SOURCE_NAME, conventionBundleSource);
  }
  
  public static RegionSource getRegionSource(FunctionExecutionContext context) {
    return (RegionSource) context.get(REGION_SOURCE_NAME);
  }
  
  public static void setRegionSource(FunctionExecutionContext context, RegionSource regionSource) {
    context.put(REGION_SOURCE_NAME, regionSource);
  }
  
  public static HolidaySource getHolidaySource(FunctionExecutionContext context) {
    return (HolidaySource) context.get(HOLIDAY_SOURCE_NAME);
  }
  
  public static void setHolidaySource(FunctionExecutionContext context, HolidaySource holidayRepository) {
    context.put(HOLIDAY_SOURCE_NAME, holidayRepository);
  }
  
  public static ConventionBundleMaster getReferenceRateRepository(FunctionExecutionContext context) {
    return (ConventionBundleMaster) context.get(REFERENCE_RATE_REPOSITORY_NAME);
  }
  
  public static void setReferenceRateRepository(FunctionExecutionContext context, ConventionBundleMaster referenceRateRepository) {
    context.put(REFERENCE_RATE_REPOSITORY_NAME, referenceRateRepository);
  }
  
  public static ExchangeRepository getExchangeRepository(FunctionExecutionContext context) {
    return (ExchangeRepository) context.get(EXCHANGE_REPOSITORY_NAME);
  }
  
  public static void setExchangeRepository(FunctionExecutionContext context, ExchangeRepository exchangeRepository) {
    context.put(EXCHANGE_REPOSITORY_NAME, exchangeRepository);
  }
}
