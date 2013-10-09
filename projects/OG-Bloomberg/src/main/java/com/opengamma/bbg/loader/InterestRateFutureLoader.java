/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_INTEREST_RATE_TYPE;
import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUTURES_CATEGORY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LAST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LONG_NAME;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_TRADING_HRS;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_VAL_PT;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_MIC_PRIM_EXCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_DES;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Loads the data for an Interest Rate Future from Bloomberg.
 */
public class InterestRateFutureLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureLoader.class);

  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_INTEREST_RATE_FUTURE_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_FUT_LONG_NAME,
      FIELD_FUT_LAST_TRADE_DT,
      FIELD_FUT_TRADING_HRS,
      FIELD_ID_MIC_PRIM_EXCH, // trading exchange
      FIELD_CRNCY,
      FIELD_FUTURES_CATEGORY,
      FIELD_SECURITY_DES,
      FIELD_PARSEKYABLE_DES,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_FUT_VAL_PT));
  
  private static final Map<String, String> BBGCODE_UNDERLYING = Maps.newHashMap();
  static {
    // Mid-curves
    for (int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0 }) {
      BBGCODE_UNDERLYING.put(i + "E", "US0003M Index");
      BBGCODE_UNDERLYING.put(i + "R", "EUR003M Index");
      BBGCODE_UNDERLYING.put(i + "L", "BP0003M Index");
    }
    BBGCODE_UNDERLYING.put("ED", "US0003M Index");
    BBGCODE_UNDERLYING.put("EM", "US0001M Index");
    BBGCODE_UNDERLYING.put("ER", "EUR003M Index");
    BBGCODE_UNDERLYING.put("FP", "EUR003M Index");
    BBGCODE_UNDERLYING.put("L ", "BP0003M Index");
    BBGCODE_UNDERLYING.put("ES", "SF0003M Index");
    BBGCODE_UNDERLYING.put("EF", "JY0003M Index");
    BBGCODE_UNDERLYING.put("IR", "BBSW3M Index");
    BBGCODE_UNDERLYING.put("BA", "CDOR03 Index");
    BBGCODE_UNDERLYING.put("EY", "TI0003M Index");
    BBGCODE_UNDERLYING.put("FF", "FEDL01 Index");
  }
  
  /**
   * The valid Bloomberg future categories for IR Futures
   */
  public static final Set<String> VALID_FUTURE_CATEGORIES = ImmutableSet.of(BLOOMBERG_INTEREST_RATE_TYPE);

  /**
   * Creates an instance. See {@link FutureSecurity}
   * @param referenceDataProvider  the provider, not null
   */
  public InterestRateFutureLoader(ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.INTEREST_RATE_FUTURE);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    String expiryDate = fieldData.getString(FIELD_FUT_LAST_TRADE_DT);
    String futureTradingHours = fieldData.getString(FIELD_FUT_TRADING_HRS);
    String micExchangeCode = fieldData.getString(FIELD_ID_MIC_PRIM_EXCH);
    String currencyStr = fieldData.getString(FIELD_CRNCY);
    String category = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUTURES_CATEGORY), " ");
    String name = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUT_LONG_NAME), " ");
    String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    double unitAmount = 2500;
    try {
      unitAmount = Double.valueOf(fieldData.getString(FIELD_FUT_VAL_PT));
    } catch (NumberFormatException e) {
      if (!currencyStr.equals("AUD")) { // Review: In AUD, you don't really have IR Futures, you have futures on Bank Bills..
        throw e;
      }
    }
    unitAmount *= 100.0; // Scale unitAmount as we quote prices without units, while Bloomberg's is in percent, ie. Bbg's 99.5 is our 0.995

    if (!isValidField(bbgUnique)) {
      s_logger.warn("bbgUnique is null. Cannot construct interest rate future security.");
      return null;
    }
    if (!isValidField(expiryDate)) {
      s_logger.warn("expiry date is null. Cannot construct interest rate future security.");
      return null;
    }
    if (!isValidField(futureTradingHours)) {
      s_logger.warn("futures trading hours is null. Cannot construct interest rate future security.");
      return null;
    }
    if (!isValidField(micExchangeCode)) {
      s_logger.warn("settlement exchange is null. Cannot construct interest rate future security.");
      return null;
    }
    if (!isValidField(currencyStr)) {
      s_logger.warn("currency is null. Cannot construct interest rate future security.");
      return null;
    }
    if (!isValidField(category)) {
      s_logger.warn("No category provided from field {}. Cannot construct interest rate future security.", FIELD_FUTURES_CATEGORY);
      return null;
    }

    Expiry expiry = decodeExpiry(expiryDate, futureTradingHours);
    if (expiry == null) {
      s_logger.warn("expiry is null. Cannot construct interest rate future security.");
      return null;
    }
    
    if (!isValidField(name)) {
      s_logger.warn("name is null. Cannot construct interest rate future security.");
      return null;
    }
    
    Currency currency = Currency.parse(currencyStr);
    String bbgCode = fieldData.getString(FIELD_PARSEKYABLE_DES);
    String bbgCode2 = bbgCode.substring(0, 2); // 2 first char
    String id = BBGCODE_UNDERLYING.get(bbgCode2);
    if (id == null) {
      s_logger.warn("Cannot get underlying for future " + bbgCode2);
      return null;
    }
    ExternalId underlyingIdentifier = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, id);
    
    ManageableSecurity security;
    if ("FEDL01 Index".equals(id)) {
      security = new FederalFundsFutureSecurity(expiry, micExchangeCode, micExchangeCode, currency, unitAmount, underlyingIdentifier, category);
    } else {
      security = new InterestRateFutureSecurity(expiry, micExchangeCode, micExchangeCode, currency, unitAmount, underlyingIdentifier, category);
    }
    security.setName(name);
    // set identifiers
    parseIdentifiers(fieldData, security);
    return security;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_INTEREST_RATE_FUTURE_FIELDS;
  }

}
