/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUTURES_CATEGORY;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_FIRST_TRADE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_LONG_NAME;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_TRADING_HRS;
import static com.opengamma.bbg.BloombergConstants.FIELD_FUT_TRADING_UNITS;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_MIC_PRIM_EXCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_LAST_TRADEABLE_DT;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_QUOTED_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_QUOTE_UNITS;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Loads the data for a FX Future from Bloomberg.
 */
public class FXFutureLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(FXFutureLoader.class);

  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_CURRENCY_FUTURE_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_FUT_LONG_NAME,
      FIELD_LAST_TRADEABLE_DT,
      FIELD_FUT_TRADING_HRS,
      FIELD_FUTURES_CATEGORY,
      FIELD_CRNCY,
      FIELD_FUT_TRADING_UNITS,
      FIELD_QUOTE_UNITS,
      FIELD_QUOTED_CRNCY,
      FIELD_ID_MIC_PRIM_EXCH,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1, 
      FIELD_PARSEKYABLE_DES,
      FIELD_FUT_FIRST_TRADE_DT));
  
  /**
   * Maps from the QUOTE_UNITS field to the unit amount. As more units are encountered, this approach will need to be
   * improved.
   */
  private static final Map<String, Double> UNIT_AMOUNT_MAP = ImmutableMap.of(
      "cents/CHF", 100d,
      "cents/100 YEN", 1d,
      "cents/CAD", 100d);

  /**
   * The valid Bloomberg future categories for FX Futures
   */
  public static final Set<String> VALID_FUTURE_CATEGORIES = Collections.unmodifiableSet(Sets.newHashSet(
      BloombergConstants.BBG_CURRENCY_TYPE));
  
  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public FXFutureLoader(ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.FX_FUTURE);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    String category = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUTURES_CATEGORY), " ");
    String name = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUT_LONG_NAME), " ");
    String expiryDate = fieldData.getString(FIELD_LAST_TRADEABLE_DT);
    String futureTradingHours = fieldData.getString(FIELD_FUT_TRADING_HRS);
    String micExchangeCode = fieldData.getString(FIELD_ID_MIC_PRIM_EXCH);
    String currencyCode = fieldData.getString(FIELD_CRNCY);
    String quoteUnits = fieldData.getString(FIELD_QUOTE_UNITS);
    String tradingCurrencyCode = fieldData.getString(FIELD_FUT_TRADING_UNITS);
    String quotedCurrencyCode = fieldData.getString(FIELD_QUOTED_CRNCY);
    
    if (!isValidField(bbgUnique)) {
      logMissingData(FIELD_ID_BBG_UNIQUE, name);
      return null;
    }
    if (!isValidField(expiryDate)) {
      logMissingData(FIELD_LAST_TRADEABLE_DT, name);
      return null;
    }
    if (!isValidField(futureTradingHours)) {
      logMissingData(FIELD_FUT_TRADING_HRS, name);
      return null;
    }
    if (!isValidField(micExchangeCode)) {
      logMissingData(FIELD_ID_MIC_PRIM_EXCH, name);
      return null;
    }
    if (!isValidField(currencyCode)) {
      logMissingData(FIELD_CRNCY, name);
      return null;
    }
    if (!isValidField(quoteUnits)) {
      logMissingData(FIELD_QUOTE_UNITS, name);
      return null;
    }
    if (!isValidField(tradingCurrencyCode)) {
      logMissingData(FIELD_FUT_TRADING_UNITS, name);
      return null;
    }
    if (!isValidField(quotedCurrencyCode)) {
      logMissingData(FIELD_QUOTED_CRNCY, name);
    }
    if (!isValidField(category)) {
      logMissingData(FIELD_FUTURES_CATEGORY, name);
    }
    
    Double unitAmount = UNIT_AMOUNT_MAP.get(quoteUnits);
    if (unitAmount == null) {
      s_logger.warn("Unknown quote units: " + quoteUnits);
      return null;
    }
    
    Expiry expiry = decodeExpiry(expiryDate, futureTradingHours);
    if (expiry == null) {
      s_logger.warn("Unable to decode expiry '" + expiryDate + "' against trading hours '" + futureTradingHours + "'");
      return null;
    }
    Currency currency = Currency.parse(currencyCode);
    Currency tradingCurrency = Currency.parse(tradingCurrencyCode);
    Currency quotedCurrency = Currency.parse(quotedCurrencyCode);
    
    FXFutureSecurity security = new FXFutureSecurity(expiry, micExchangeCode, micExchangeCode, currency, unitAmount, tradingCurrency, quotedCurrency, category);    
    security.setName(name);
    parseIdentifiers(fieldData, security, FIELD_FUT_FIRST_TRADE_DT, FIELD_LAST_TRADEABLE_DT);
    return security;
  }
  
  private void logMissingData(String fieldName, String securityName) {
    s_logger.warn("Cannot construct FX Future security '" + securityName + "' as " + fieldName + " is missing");
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_CURRENCY_FUTURE_FIELDS;
  }

}
