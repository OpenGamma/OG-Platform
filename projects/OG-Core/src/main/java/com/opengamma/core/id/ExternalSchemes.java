/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Utilities and constants for {@code Security}.
 * <p>
 * This is a thread-safe static utility class.
 */
@PublicAPI
public class ExternalSchemes {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExternalSchemes.class);

  // --------------------------- SCHEMES FOR USER IDENTITY ------------------------------------
  /**
   * Identification scheme for Windows user id.
   */
  public static final ExternalScheme WINDOWS_USER_ID = ExternalScheme.of("WINDOWS_USER_ID");
  /**
   * Identification scheme for UUID identifier
   */
  public static final ExternalScheme BLOOMBERG_UUID = ExternalScheme.of("BLOOMBERG_UUID");
  /**
   * Identification scheme for EMRSID identifier
   */
  public static final ExternalScheme BLOOMBERG_EMRSID = ExternalScheme.of("BLOOMBERG_EMRSID");

  // --------------------------- SCHEMES FOR SECURITIES AND RATES -----------------------------
  /**
   * Identification scheme for the ISIN code.
   */
  public static final ExternalScheme ISIN = ExternalScheme.of("ISIN");
  /**
   * Identification scheme for the CUSIP code.
   */
  public static final ExternalScheme CUSIP = ExternalScheme.of("CUSIP");
  /**
   * Identification scheme for the CUSIP entity stub code.
   */
  public static final ExternalScheme CUSIP_ENTITY_STUB = ExternalScheme.of("CUSIP_ENTITY_STUB");
  /**
   * Identification scheme for SEDOL1.
   */
  public static final ExternalScheme SEDOL1 = ExternalScheme.of("SEDOL1");
  /**
   * Identification scheme for Bloomberg BUIDs.
   */
  public static final ExternalScheme BLOOMBERG_BUID = ExternalScheme.of("BLOOMBERG_BUID");
  /**
   * Identification scheme for weak Bloomberg BUIDs.
   * A weak ID permits the underlying market data to return old data.
   */
  public static final ExternalScheme BLOOMBERG_BUID_WEAK = ExternalScheme.of("BLOOMBERG_BUID_WEAK");
  /**
   * Identification scheme for Bloomberg tickers.
   */
  public static final ExternalScheme BLOOMBERG_TICKER = ExternalScheme.of("BLOOMBERG_TICKER");
  /**
   * Identification scheme for weak Bloomberg tickers.
   * A weak ID permits the underlying market data to return old data.
   */
  public static final ExternalScheme BLOOMBERG_TICKER_WEAK = ExternalScheme.of("BLOOMBERG_TICKER_WEAK");
  /**
   * Identification scheme for Bloomberg tickers.
   * @deprecated use BLOOMBERG_TICKER instead
   */
  @Deprecated
  public static final ExternalScheme BLOOMBERG_TCM = ExternalScheme.of("BLOOMBERG_TCM");
  /**
   * Identification scheme for conventions, using the stub of SECURITY_DES (minus date information)
   */
  public static final ExternalScheme BLOOMBERG_CONVENTION_NAME = ExternalScheme.of("BLOOMBERG_CONVENTION_NAME");
  /**
   * Identification scheme for index families, using the stub of SECURITY_DES (minus date information)
   */
  public static final ExternalScheme BLOOMBERG_INDEX_FAMILY = ExternalScheme.of("BLOOMBERG_INDEX_FAMILY");
  /**
   * Identification scheme for Reuters RICs.
   */
  public static final ExternalScheme RIC = ExternalScheme.of("RIC");
  /**
   * Identification scheme for ActivFeed tickers.
   */
  public static final ExternalScheme ACTIVFEED_TICKER = ExternalScheme.of("ACTIVFEED_TICKER");
  /**
   * Identification scheme for OpenGamma synthetic instruments.
   */
  public static final ExternalScheme OG_SYNTHETIC_TICKER = ExternalScheme.of("OG_SYNTHETIC_TICKER");
  /**
   * Identification scheme for Tullett Prebon SURF tickers.
   */
  public static final ExternalScheme SURF = ExternalScheme.of("SURF");
  /**
   * Identification scheme for ICAP market data feed tickers.
   */
  public static final ExternalScheme ICAP = ExternalScheme.of("ICAP");
  /**
   * Identification scheme for GMI contracts.
   */
  public static final ExternalScheme GMI = ExternalScheme.of("GMI");
  /**
   * Identification scheme conventions specified for ISDA.
   */
  public static final ExternalScheme ISDA = ExternalScheme.of("ISDA");
  // --------------------- SCHEMES FOR EXCHANGES ---------------------------

  /**
   * Identification scheme for CDS Index and Obligors.
   */
  public static final ExternalScheme MARKIT_RED_CODE = ExternalScheme.of("MARKIT_RED_CODE");

  //-------------------- SCHEMES FOR REGIONS ---------------------

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
   * Identification scheme for the MIC exchange code ISO standard.
   */
  public static final ExternalScheme ISO_MIC = ExternalScheme.of("ISO_MIC");

  /**
   * Identification scheme for holiday conventions specified for ISDA.
   * Common examples include USNY, CATO, and EUTA.
   */
  public static final ExternalScheme ISDA_HOLIDAY = ExternalScheme.of("ISDA_HOLIDAY");

  /**
   * Restricted constructor.
   */
  protected ExternalSchemes() {
  }

  //------------------ METHODS FOR USER IDENTITY -----------------------------
  /**
   * Creates a Windows user id.
   * 
   * @param windowsUserId  the Windows user id, not null
   * @return the identifier, not null
   */
  public static ExternalId windowsUserId(String windowsUserId) {
    ArgumentChecker.notNull(windowsUserId, "windowsUserId");
    return ExternalId.of(ExternalSchemes.WINDOWS_USER_ID, windowsUserId);
  }

  /**
   * Creates a UUID identifier.
   * <p>
   * This is an identifier for BPS bloomberg user.
   * 
   * @param uuid the bps bloomberg user identifier, not null
   * @return the user uuid identifier, not null
   */
  public static ExternalId bloombergUuidUserId(final String uuid) {
    return ExternalId.of(BLOOMBERG_UUID, ArgumentChecker.notNull(StringUtils.trimToNull(uuid), "uuid"));
  }

  /**
   * Creates an EMRSID identifier.
   * <p>
   * This is an identifier for NON-BPS bloomberg user 
   * 
   *  @param emrsid the non-bps bloomberg user identifier, not null
   * @return the user emrsid identifier, not null
   */
  public static ExternalId bloombergEmrsUserId(final String emrsid) {
    return ExternalId.of(BLOOMBERG_EMRSID, ArgumentChecker.notNull(StringUtils.trimToNull(emrsid), "emrsid"));
  }

  //------------------ METHODS FOR SECURITIES AND RATES ----------------------
  /**
   * Creates an ISIN code.
   * <p>
   * This is the international standard securities identifying number.
   * The first two characters are the ISO country code, followed by a 9 character
   * alphanumeric national code and a single numeric check-digit.
   * Example might be {@code US0231351067}.
   * 
   * @param code  the ISIN code, not null
   * @return the security identifier, not null
   */
  public static ExternalId isinSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("ISIN code is invalid: " + code);
    }
    //    if (code.matches("[A-Z]{2}[A-Z0-9]{9}[0-9]") == false) {
    //      throw new IllegalArgumentException("ISIN code is invalid: " + code);
    //    }
    return ExternalId.of(ISIN, code);
  }

  /**
   * Creates a CUSIP code.
   * <p>
   * This is the national securities identifying number for USA and Canada.
   * The code should be 8 alphanumeric characters followed by a single numeric check-digit.
   * Example might be {@code 023135106}.
   * 
   * @param code  the CUSIP code, not null
   * @return the security identifier, not null
   */
  public static ExternalId cusipSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("CUSIP code is invalid: " + code);
    }
    //    if (code.matches("[A-Z0-9]{8}[0-9]?") == false) {
    //      throw new IllegalArgumentException("CUSIP code is invalid: " + code);
    //    }
    return ExternalId.of(CUSIP, code);
  }

  /**
   * Creates a SEDOL code.
   * <p>
   * This is the national securities identifying number for UK and Ireland.
   * The code should be 6 alphanumeric characters followed by a single numeric check-digit.
   * Example might be {@code 0263494}.
   * 
   * @param code  the SEDOL code, not null
   * @return the security identifier, not null
   */
  public static ExternalId sedol1SecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("SEDOL1 code is invalid: " + code);
    }
    //    if (code.matches("[A-Z0-9]{6}[0-9]?") == false) {
    //      throw new IllegalArgumentException("SEDOL1 code is invalid: " + code);
    //    }
    return ExternalId.of(SEDOL1, code);
  }

  /**
   * Creates a Bloomberg BIUD code.
   * <p>
   * This is the BUID code supplied by Bloomberg.
   * Examples might be {@code EQ0010599500001000} or {@code IX6572023-0}.
   * 
   * @param code  the Bloomberg BIUD code, not null
   * @return the security identifier, not null
   */
  public static ExternalId bloombergBuidSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("BUID code is invalid: " + code);
    }
    return ExternalId.of(BLOOMBERG_BUID, code);
  }

  /**
   * Creates a Bloomberg ticker.
   * <p>
   * This is the ticker supplied by Bloomberg.
   * Examples might be {@code MCO US Equity} or {@code CSCO US 01/21/12 C30 Equity}.
   * 
   * @param ticker  the Bloomberg ticker, not null
   * @return the security identifier, not null
   */
  public static ExternalId bloombergTickerSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "code");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(BLOOMBERG_TICKER, ticker);
  }

  /**
   * Creates a Synthetic ticker.
   * <p>
   * This is the ticker used mainly by Examples-Simulated.
   * 
   * @param ticker  the OG-Synthetic ticker, not null
   * @return the security identifier, not null
   */
  public static ExternalId syntheticSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "ticker");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(OG_SYNTHETIC_TICKER, ticker);
  }

  /**
   * Creates a Bloomberg ticker coupon maturity identifier.
   * <p>
   * This is the ticker combined with a coupon and a maturity supplied by Bloomberg.
   * Example might be {@code T 4.75 15/08/43 Govt}.
   * 
   * @param tickerWithoutSector  the Bloomberg ticker without the sector, not null
   * @param coupon  the coupon, not null
   * @param maturity  the maturity date, not null
   * @param marketSector  the sector, not null
   * @return the security identifier, not null
   */
  public static ExternalId bloombergTCMSecurityId(final String tickerWithoutSector, final String coupon, final String maturity, final String marketSector) {
    ArgumentChecker.notNull(tickerWithoutSector, "tickerWithoutSector");
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(maturity, "maturity");
    ArgumentChecker.notNull(marketSector, "marketSector");
    if (StringUtils.isEmpty(tickerWithoutSector)) {
      throw new IllegalArgumentException("Ticker (without sector) must not be empty");
    }
    if (StringUtils.isEmpty(coupon)) {
      throw new IllegalArgumentException("Coupon must not be empty, ticker = " + tickerWithoutSector);
    }
    if (StringUtils.isEmpty(maturity)) {
      throw new IllegalArgumentException("Maturity must not be empty, ticker = " + tickerWithoutSector + ", coupon = " + coupon);
    }
    if (StringUtils.isEmpty(marketSector)) {
      throw new IllegalArgumentException("Market sector must not be empty, ticker = " + tickerWithoutSector + ", coupon = " + coupon + ", maturity = " + maturity);
    }
    Double couponDbl;
    try {
      couponDbl = Double.parseDouble(coupon);
    } catch (final NumberFormatException ex) {
      throw new IllegalArgumentException("Coupon must be a valid double, ticker=" + tickerWithoutSector + ", coupon=" + coupon, ex);
    }
    if (s_logger.isDebugEnabled()) {
      try {
        LocalDate.parse(maturity, DateTimeFormatter.ofPattern("MM/dd/yy"));
      } catch (final UnsupportedOperationException uoe) {
        s_logger.warn("Problem parsing maturity " + maturity + " ticker=" + tickerWithoutSector + ", coupon=" + coupon);
      } catch (final DateTimeException ex) {
        s_logger.warn("Problem parsing maturity " + maturity + " ticker=" + tickerWithoutSector + ", coupon=" + coupon);
      }
    }
    return ExternalId.of(BLOOMBERG_TCM, tickerWithoutSector + " " + couponDbl + " " + maturity + " " + marketSector);
  }

  /**
   * Creates a Reuters RIC code.
   * <p>
   * This is the RIC code supplied by Reuters.
   * Example might be {@code MSFT.OQ}.
   * 
   * @param code  the BIUD code, not null
   * @return the security identifier, not null
   */
  public static ExternalId ricSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("RIC code is invalid: " + code);
    }
    return ExternalId.of(RIC, code);
  }

  /**
   * Creates an ActivFeed ticker.
   * <p>
   * This is the ticker used by ActivFeed.
   * Examples might be {@code IBM.N} or {@code C/04H.CB}.
   * 
   * @param ticker  the ActivFeed ticker, not null
   * @return the security identifier, not null
   */
  public static ExternalId activFeedTickerSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "code");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(ACTIVFEED_TICKER, ticker);
  }

  /**
   * Creates a Tullett-Prebon ticker.
   * <p>
   * This is the ticker used by Tullett-Prebon.
   * An example is {@code ASIRSUSD20Y30S03L}.
   * 
   * @param ticker The Tullett-Prebon ticker, not null
   * @return The security identifier, not null
   */
  public static ExternalId tullettPrebonSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "ticker");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(SURF, ticker);
  }

  /**
   * Creates an ICAP ticker.
   * <p>
   * This is the ticker used by ICAP.
   * 
   * @param ticker The ICAP ticker, not null
   * @return The security identifier, not null
   */
  public static ExternalId icapSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "ticker");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(ICAP, ticker);
  }

  /**
   * Creates a GMI ticker.
   * <p>
   * This is the ticker used by GMI.
   * 
   * @param ticker The GMI ticker, not null
   * @return The security identifier, not null
   */
  public static ExternalId gmiSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "ticker");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(GMI, ticker);
  }

  /**
   * Creates a MarkIt RED_CODE identifier
   * <p>
   * @param redcode the redcode identifier, not null or empty
   * @return the security redcode identifier, not null
   */
  public static ExternalId markItRedCode(final String redcode) {
    ArgumentChecker.notNull(redcode, "redcode");
    ArgumentChecker.isFalse(redcode.isEmpty(), "Empty redcode is invalid");
    return ExternalId.of(MARKIT_RED_CODE, redcode);
  }

  /**
   * Creates an ISDA identifier
   * <p>
   * @param isdaName the isda name, not null or empty
   * @return the isda identifier, not null
   */
  public static ExternalId isda(final String isdaName) {
    ArgumentChecker.notEmpty(isdaName, "isdaname");
    return ExternalId.of(ISDA, isdaName);
  }

  // -------------------------- METHODS FOR REGIONS ---------------------------

  /**
   * Creates an identifier for a financial location.
   * 
   * @param code  the code, not null
   * @return the region identifier, not null
   */
  public static ExternalId financialRegionId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.matches("[A-Z+]+") == false) {
      throw new IllegalArgumentException("Code is invalid: " + code);
    }
    return ExternalId.of(ExternalSchemes.FINANCIAL, code);
  }

  /**
   * Creates a tz database time-zone code.
   * <p>
   * Examples might be {@code Europe/London} or {@code Asia/Hong_Kong}.
   * 
   * @param zone  the time-zone, not null
   * @return the region identifier, not null
   */
  public static ExternalId timeZoneRegionId(final ZoneId zone) {
    ArgumentChecker.notNull(zone, "zone");
    return ExternalId.of(ExternalSchemes.TZDB_TIME_ZONE, zone.getId());
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
  public static ExternalId coppClarkRegionId(final String locode) {
    ArgumentChecker.notNull(locode, "locode");
    if (locode.matches("[A-Z]{2}[A-Z0-9]{3}") == false) {
      throw new IllegalArgumentException("Copp Clark LOCODE is invalid: " + locode);
    }
    return ExternalId.of(ExternalSchemes.COPP_CLARK_LOCODE, locode);
  }

  /**
   * Creates a UN/LOCODE 2010-2 code, formatted without spaces.
   * <p>
   * Examples might be {@code GBHOH} or {@code AEDXB}.
   * 
   * @param locode  the UN/LOCODE, not null
   * @return the region identifier, not null
   */
  public static ExternalId unLocode20102RegionId(final String locode) {
    ArgumentChecker.notNull(locode, "locode");
    if (locode.matches("[A-Z]{2}[A-Z0-9]{3}") == false) {
      throw new IllegalArgumentException("UN/LOCODE is invalid: " + locode);
    }
    return ExternalId.of(ExternalSchemes.UN_LOCODE_2010_2, locode);
  }

  /**
   * Creates an ISO alpha 3 currency code.
   * <p>
   * Examples might be {@code GBP} or {@code USD}.
   * 
   * @param currency  the currency, not null
   * @return the region identifier, not null
   */
  public static ExternalId currencyRegionId(final Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    return ExternalId.of(ExternalSchemes.ISO_CURRENCY_ALPHA3, currency.getCode());
  }

  /**
   * Creates an ISO alpha 2 country code.
   * <p>
   * Examples might be {@code GB} or {@code US}.
   * 
   * @param country  the country, not null
   * @return the region identifier, not null
   */
  public static ExternalId countryRegionId(final Country country) {
    ArgumentChecker.notNull(country, "country");
    return ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, country.getCode());
  }

  //---------------------- METHODS FOR EXCHANGES ---------------------
  /**
   * Creates an ISO MIC code.
   * <p>
   * Examples might be {@code XLON} or {@code XNYS}.
   * 
   * @param code  the code, not null
   * @return the region identifier, not null
   */
  public static ExternalId isoMicExchangeId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.matches("[A-Z0-9]{4}([-][A-Z0-9]{3})?") == false) {
      throw new IllegalArgumentException("ISO MIC code is invalid: " + code);
    }
    return ExternalId.of(ExternalSchemes.ISO_MIC, code);
  }

  //---------------------- HOLIDAYS ---------------------
  /**
   * Creates an ISDA holiday code.
   * <p>
   * Examples might be {@code USNY} or {@code EUTA}.
   * 
   * @param code  the code, not null
   * @return the holiday identifier, not null
   */
  public static ExternalId isdaHoliday(final String code) {
    ArgumentChecker.notNull(code, "code");
    return ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, code);
  }

}
