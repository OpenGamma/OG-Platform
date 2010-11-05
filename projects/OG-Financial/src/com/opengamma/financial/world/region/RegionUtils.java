/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.region;

import javax.time.calendar.TimeZone;

import com.opengamma.financial.Currency;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Utilities and constants for regions.
 */
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
    return Identifier.of(ISO_CURRENCY_ALPHA3, currency.getISOCode());
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

}
