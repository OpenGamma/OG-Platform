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

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.money.Currency;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Utilities and constants for regions.
 */
@PublicAPI
public class RegionUtils {

  /**
   * Identification scheme for the ISO alpha 2 country code ISO standard.
   */
  public static final IdentificationScheme ISO_COUNTRY_ALPHA2 = IdentificationScheme.of("ISO_COUNTRY_ALPHA2");
  /**
   * Identification scheme for the ISO alpha 3 currency code ISO standard.
   */
  public static final IdentificationScheme ISO_CURRENCY_ALPHA3 = IdentificationScheme.of("ISO_CURRENCY_ALPHA3");
  /**
   * Identification scheme for the Copp Clark version of UN/LOCODE , formatted without spaces.
   */
  public static final IdentificationScheme COPP_CLARK_LOCODE = IdentificationScheme.of("COPP_CLARK_LOCODE");
  /**
   * Identification scheme for the UN/LOCODE 2010-2 code standard, formatted without spaces.
   */
  public static final IdentificationScheme UN_LOCODE_2010_2 = IdentificationScheme.of("UN_LOCODE_2010_2");
  /**
   * Identification scheme for the tz database time-zone standard.
   */
  public static final IdentificationScheme TZDB_TIME_ZONE = IdentificationScheme.of("TZDB_TIME_ZONE");
  /**
   * Identification scheme for financial activity.
   * This currently tends to be the country code, but can be more complex.
   */
  public static final IdentificationScheme FINANCIAL = IdentificationScheme.of("FINANCIAL_REGION");

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
   * @param codeAlpha2  the country code, not null
   * @return the region identifier, not null
   */
  public static Identifier countryRegionId(String codeAlpha2) {
    ArgumentChecker.notNull(codeAlpha2, "codeAlpha2");
    if (codeAlpha2.matches("[A-Z][A-Z]") == false) {
      throw new IllegalArgumentException("ISO alpha 2 country code is invalid: " + codeAlpha2);
    }
    return Identifier.of(ISO_COUNTRY_ALPHA2, codeAlpha2);
  }

  /**
   * Creates an ISO alpha 3 currency code.
   * <p>
   * Examples might be {@code GBP} or {@code USD}.
   * 
   * @param currency  the currency, not null
   * @return the region identifier, not null
   */
  public static Identifier currencyRegionId(Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    return Identifier.of(ISO_CURRENCY_ALPHA3, currency.getCode());
  }

  /**
   * Creates a UN/LOCODE 2010-2 code, formatted without spaces.
   * <p>
   * Examples might be {@code GBHOH} or {@code AEDXB}.
   * 
   * @param locode  the UN/LOCODE, not null
   * @return the region identifier, not null
   */
  public static Identifier unLocode20102RegionId(String locode) {
    ArgumentChecker.notNull(locode, "locode");
    if (locode.matches("[A-Z]{2}[A-Z0-9]{3}") == false) {
      throw new IllegalArgumentException("UN/LOCODE is invalid: " + locode);
    }
    return Identifier.of(UN_LOCODE_2010_2, locode);
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
  public static Identifier coppClarkRegionId(String locode) {
    ArgumentChecker.notNull(locode, "locode");
    if (locode.matches("[A-Z]{2}[A-Z0-9]{3}") == false) {
      throw new IllegalArgumentException("Copp Clark LOCODE is invalid: " + locode);
    }
    return Identifier.of(COPP_CLARK_LOCODE, locode);
  }

  /**
   * Creates a tz database time-zone code.
   * <p>
   * Examples might be {@code Europe/London} or {@code Asia/Hong_Kong}.
   * 
   * @param zone  the time-zone, not null
   * @return the region identifier, not null
   */
  public static Identifier timeZoneRegionId(TimeZone zone) {
    ArgumentChecker.notNull(zone, "zone");
    return Identifier.of(TZDB_TIME_ZONE, zone.getID());
  }

  /**
   * Creates an identifier for a financial location.
   * 
   * @param code  the code, not null
   * @return the region identifier, not null
   */
  public static Identifier financialRegionId(String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.matches("[A-Z+]+") == false) {
      throw new IllegalArgumentException("Code is invalid: " + code);
    }
    return Identifier.of(FINANCIAL, code);
  }

  /**
   * Creates a set of regions from a region id. This is useful in the case where the region is compound (e.g. NY+LON)
   * @param regionSource The region source, not null
   * @param regionId The region id, not null
   * @return a set of the region(s)
   */
  @SuppressWarnings("unchecked")
  public static Set<Region> getRegions(RegionSource regionSource, final Identifier regionId) {
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
