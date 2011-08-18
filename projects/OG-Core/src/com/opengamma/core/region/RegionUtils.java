/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.TimeZone;

import org.apache.commons.lang.Validate;

import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Utilities and constants for {@code Region}.
 * <p>
 * This is a thread-safe static utility class.
 */
@PublicAPI
public class RegionUtils {

  /**
   * Identification scheme for the ISO alpha 2 country code ISO standard.
   */
  public static final ExternalScheme ISO_COUNTRY_ALPHA2 = ExternalScheme.of("ISO_COUNTRY_ALPHA2");
  /**
   * Identification scheme for the ISO alpha 3 currency code ISO standard.
   */
  public static final ExternalScheme ISO_CURRENCY_ALPHA3 = ExternalScheme.of("ISO_CURRENCY_ALPHA3");
  /**
   * Identification scheme for the Copp Clark version of UN/LOCODE , formatted without spaces.
   */
  public static final ExternalScheme COPP_CLARK_LOCODE = ExternalScheme.of("COPP_CLARK_LOCODE");
  /**
   * Identification scheme for the UN/LOCODE 2010-2 code standard, formatted without spaces.
   */
  public static final ExternalScheme UN_LOCODE_2010_2 = ExternalScheme.of("UN_LOCODE_2010_2");
  /**
   * Identification scheme for the tz database time-zone standard.
   */
  public static final ExternalScheme TZDB_TIME_ZONE = ExternalScheme.of("TZDB_TIME_ZONE");
  /**
   * Identification scheme for financial activity.
   * This currently tends to be the country code, but can be more complex.
   */
  public static final ExternalScheme FINANCIAL = ExternalScheme.of("FINANCIAL_REGION");

  /**
   * Restricted constructor.
   */
  protected RegionUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an ISO alpha 2 country code.
   * <p>
   * Examples might be {@code GB} or {@code US}.
   * 
   * @param country  the country, not null
   * @return the region identifier, not null
   */
  public static ExternalId countryRegionId(Country country) {
    ArgumentChecker.notNull(country, "country");
    return ExternalId.of(ISO_COUNTRY_ALPHA2, country.getCode());
  }

  /**
   * Creates an ISO alpha 3 currency code.
   * <p>
   * Examples might be {@code GBP} or {@code USD}.
   * 
   * @param currency  the currency, not null
   * @return the region identifier, not null
   */
  public static ExternalId currencyRegionId(Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    return ExternalId.of(ISO_CURRENCY_ALPHA3, currency.getCode());
  }

  /**
   * Creates a UN/LOCODE 2010-2 code, formatted without spaces.
   * <p>
   * Examples might be {@code GBHOH} or {@code AEDXB}.
   * 
   * @param locode  the UN/LOCODE, not null
   * @return the region identifier, not null
   */
  public static ExternalId unLocode20102RegionId(String locode) {
    ArgumentChecker.notNull(locode, "locode");
    if (locode.matches("[A-Z]{2}[A-Z0-9]{3}") == false) {
      throw new IllegalArgumentException("UN/LOCODE is invalid: " + locode);
    }
    return ExternalId.of(UN_LOCODE_2010_2, locode);
  }

  /**
   * Creates a Copp Clark location code, formatted without spaces.
   * This is based on UN/LOCODE.
   * <p>
   * Examples might be {@code GBHOH} or {@code AEDXB}.
   * 
   * @param locode  the Copp Clark LOCODE, not null
   * @return the region identifier, not null
   */
  public static ExternalId coppClarkRegionId(String locode) {
    ArgumentChecker.notNull(locode, "locode");
    if (locode.matches("[A-Z]{2}[A-Z0-9]{3}") == false) {
      throw new IllegalArgumentException("Copp Clark LOCODE is invalid: " + locode);
    }
    return ExternalId.of(COPP_CLARK_LOCODE, locode);
  }

  /**
   * Creates a tz database time-zone code.
   * <p>
   * Examples might be {@code Europe/London} or {@code Asia/Hong_Kong}.
   * 
   * @param zone  the time-zone, not null
   * @return the region identifier, not null
   */
  public static ExternalId timeZoneRegionId(TimeZone zone) {
    ArgumentChecker.notNull(zone, "zone");
    return ExternalId.of(TZDB_TIME_ZONE, zone.getID());
  }

  /**
   * Creates an identifier for a financial location.
   * 
   * @param code  the code, not null
   * @return the region identifier, not null
   */
  public static ExternalId financialRegionId(String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.matches("[A-Z+]+") == false) {
      throw new IllegalArgumentException("Code is invalid: " + code);
    }
    return ExternalId.of(FINANCIAL, code);
  }

  /**
   * Creates a set of regions from a region id.
   * This is useful in the case where the region is compound (e.g. NY+LON).
   * 
   * @param regionSource The region source, not null
   * @param regionId The region id, not null
   * @return a set of the region(s)
   */
  @SuppressWarnings("unchecked")
  public static Set<Region> getRegions(RegionSource regionSource, final ExternalId regionId) {
    Validate.notNull(regionSource, "region source");
    Validate.notNull(regionId, "region id");
    if (regionId.isScheme(RegionUtils.FINANCIAL) && regionId.getValue().contains("+")) {
      final String[] regions = regionId.getValue().split("\\+");
      final Set<Region> resultRegions = new HashSet<Region>();
      for (final String region : regions) {
        resultRegions.add(regionSource.getHighestLevelRegion(RegionUtils.financialRegionId(region)));
      }
      return resultRegions;
    } 
    return Collections.singleton(regionSource.getHighestLevelRegion(regionId)); 
  }

}
