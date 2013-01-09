/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_EXCH_CODE;
import static com.opengamma.bbg.BloombergConstants.FIELD_GICS_SUB_INDUSTRY;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_MARKET_SECTOR_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_MIC_1;
import static com.opengamma.bbg.BloombergConstants.FIELD_MIC_LOCAL_EXCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_MIC_PRIM_EXCH;
import static com.opengamma.bbg.BloombergConstants.FIELD_NAME;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_SHORT_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_TICKER;
import static com.opengamma.bbg.BloombergConstants.MARKET_SECTOR_PREFERRED;
import static com.opengamma.bbg.BloombergConstants.VALID_EQUITY_TYPES;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.security.BloombergSecurityProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.timeseries.exchange.Exchange;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Loads the data for an Equity Future from Bloomberg.
 */
public final class EquityLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_EQUITY_FIELDS = ImmutableSet.of(
      FIELD_NAME,
      FIELD_SECURITY_DES,
      FIELD_SECURITY_SHORT_DES,
      FIELD_TICKER,
      FIELD_EXCH_CODE,
      FIELD_MARKET_SECTOR_DES,
      FIELD_CRNCY,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_GICS_SUB_INDUSTRY,
      FIELD_ID_BBG_UNIQUE,
      FIELD_MIC_LOCAL_EXCH,
      FIELD_MIC_PRIM_EXCH,
      FIELD_MIC_1);
  
  /**
   * The valid Bloomberg security types for Equity.
   */  
  public static final Set<String> VALID_SECURITY_TYPES = new ImmutableSet.Builder<String>().addAll(VALID_EQUITY_TYPES).build();

  /**
   * The exchange data provider.
   */
  private final ExchangeDataProvider _exchangeDataProvider;

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   * @param exchangeDataProvider  the exchange data provider, not null
   */
  public EquityLoader(ReferenceDataProvider referenceDataProvider, ExchangeDataProvider exchangeDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.EQUITY);
    ArgumentChecker.notNull(exchangeDataProvider, "exchangeDataProvider");
    _exchangeDataProvider = exchangeDataProvider;
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    final String bbgUniqueIdString = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    final String name = fieldData.getString(FIELD_NAME);
    final String ticker = fieldData.getString(FIELD_TICKER);
    final String bbgExchangeCode = fieldData.getString(FIELD_EXCH_CODE);
    final String gicsCodeString = fieldData.getString(FIELD_GICS_SUB_INDUSTRY);
    final String currencyCode = fieldData.getString(FIELD_CRNCY);
    
    String marketSector = fieldData.getString(FIELD_MARKET_SECTOR_DES);
    String micExchangeCode;
    boolean isPreferred;
    if (MARKET_SECTOR_PREFERRED.equals(marketSector)) {
      micExchangeCode = fieldData.getString(FIELD_MIC_1);
      isPreferred = true;
    } else {
      micExchangeCode = fieldData.getString(FIELD_MIC_LOCAL_EXCH);
      if (micExchangeCode == null || !BloombergDataUtils.isValidField(micExchangeCode)) {
        micExchangeCode = fieldData.getString(FIELD_MIC_PRIM_EXCH);
      }
      isPreferred = false;
    }

    if (!isValidField(bbgUniqueIdString)) {
      logMissingData("bbgUniqueID", name);
      return null;
    }
    if (!isValidField(name)) {
      logMissingData("equity name", bbgUniqueIdString);
      return null;
    }
    if (!BloombergDataUtils.isValidField(ticker)) {
      logMissingData("equity ticker", name);
      return null;
    }
    if (!BloombergDataUtils.isValidField(bbgExchangeCode)) {
      logMissingData("equity exchange", name);
      return null;
    }
    if (!BloombergDataUtils.isValidField(currencyCode)) {
      logMissingData("equity currency", name);
      return null;
    }
    
    final Exchange exchangeData = _exchangeDataProvider.getExchange(micExchangeCode);
    if (exchangeData == null) {
      logMissingData("equity exchange data (common stock)", name);
      return null;
    }
    
    UniqueId bbgUniqueId = BloombergSecurityProvider.createUniqueId(bbgUniqueIdString);
    Currency currency = Currency.of(currencyCode.toUpperCase());
    GICSCode gicsCode = gicsCodeString != null ? GICSCode.of(gicsCodeString) : null;
    
    EquitySecurity security = new EquitySecurity(exchangeData.getDescription(), exchangeData.getMic(), name, currency);
    security.setUniqueId(bbgUniqueId);
    security.setName(name);
    security.setShortName(ticker);
    if (gicsCode != null) {
      security.setGicsCode(gicsCode);
    }
    security.setPreferred(isPreferred);
    
    //set identifiers
    parseIdentifiers(fieldData, security);
    
    return security;
  }

  private void logMissingData(String fieldName, String securityName) {
    s_logger.warn("Cannot construct equity security " + securityName + " as " + fieldName + " is missing");
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_EQUITY_FIELDS;
  }

  @Override
  protected void parseIdentifiers(final FudgeMsg fieldData, final ManageableSecurity security) {
    super.parseIdentifiers(fieldData, security);
    
    String marketSector = StringUtils.trimToEmpty(fieldData.getString(FIELD_MARKET_SECTOR_DES));
    if (!BloombergDataUtils.isValidField(marketSector)) {
      return;
    }
    String bbgExchangeCode;
    if (MARKET_SECTOR_PREFERRED.equals(marketSector)) {
      bbgExchangeCode = null;
    } else {
      bbgExchangeCode = StringUtils.trimToEmpty(fieldData.getString(FIELD_EXCH_CODE));
      if (!BloombergDataUtils.isValidField(bbgExchangeCode)) {
        bbgExchangeCode = null;
      }
    }

    String securityShortDes = StringUtils.trimToEmpty(fieldData.getString(FIELD_SECURITY_SHORT_DES));
    if (BloombergDataUtils.isValidField(securityShortDes)) {
      security.addExternalId(getTicker(securityShortDes, bbgExchangeCode, marketSector));
    } else {
      String securityDes = StringUtils.trimToEmpty(fieldData.getString(FIELD_SECURITY_DES));
      if (BloombergDataUtils.isValidField(securityDes)) {
        security.addExternalId(getTicker(securityDes, bbgExchangeCode, marketSector));
      }
    }
  }

  private ExternalId getTicker(final String securityDes, final String exchangeCode, final String marketSector) {
    final StringBuilder sb = new StringBuilder(securityDes).append(" ");
    if (exchangeCode != null) {
      sb.append(exchangeCode).append(" ");
    }
    sb.append(marketSector);
    return ExternalSchemes.bloombergTickerSecurityId(sb.toString());
  }
}
