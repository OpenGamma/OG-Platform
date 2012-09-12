/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_COUNTRY_ISO;
import static com.opengamma.bbg.BloombergConstants.FIELD_CRNCY;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_MARKET_SECTOR_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_NAME;
import static com.opengamma.bbg.BloombergConstants.FIELD_TICKER;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Loads the data for an Cash Security from Bloomberg.
 */
public final class CashLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(CashLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_CASH_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_TICKER,
      FIELD_MARKET_SECTOR_DES,
      FIELD_ID_BBG_UNIQUE,
      FIELD_CRNCY,
      FIELD_COUNTRY_ISO,
      FIELD_NAME
  ));

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public CashLoader(ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.CASH);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    String ticker = fieldData.getString(FIELD_TICKER);
    String currency = fieldData.getString(FIELD_CRNCY);
    if (currency != null) {
      currency = currency.toUpperCase();
    }
    String marketSector = fieldData.getString(FIELD_MARKET_SECTOR_DES);
    String countryIso = fieldData.getString(FIELD_COUNTRY_ISO);
    String bbgUniqueID = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    String name = fieldData.getString(FIELD_NAME);
    if (!isValidField(bbgUniqueID)) {
      s_logger.warn("bbgUniqueID is missing, cannot construct cash security");
      return null;
    }
    if (!isValidField(name)) {
      s_logger.warn("name is missing, cannot construct cash security");
    }
    if (!BloombergDataUtils.isValidField(ticker)) {
      s_logger.warn("equity ticker is missing, cannot construct cash security");
      return null;
    }
    if (!BloombergDataUtils.isValidField(countryIso)) {
      s_logger.warn("equity exchange is missing, cannot construct cash security");
      return null;
    }
    if (!BloombergDataUtils.isValidField(currency)) {
      s_logger.warn("equity currency is missing, cannot construct cash security");
      return null;
    }
//    CashSecurity security = new CashSecurity(Currency.getInstance(currency), Identifier.of(InMemoryRegionRepository.ISO_COUNTRY_2, countryIso));
//    security.setUniqueId(BloombergSecuritySource.createUniqueId(bbgUniqueID));
//    security.setName(name);
//    //add other domain specific identifiers if available
//    if (BloombergDataUtil.isValidField(marketSector)) {
//      StringBuffer bbgTicker = new StringBuffer(ticker).append(" ").append(marketSector);
//      security.addIdentifier(Identifier.of(ExternalScheme.BLOOMBERG_TICKER, bbgTicker.toString()));
//    }
//    security.addIdentifier(Identifier.of(ExternalScheme.BLOOMBERG_BUID, bbgUniqueID));
//  
//    return security;
    return null;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_CASH_FIELDS;
  }

}
