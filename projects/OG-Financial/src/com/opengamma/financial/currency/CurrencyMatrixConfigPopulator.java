/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.money.Currency;

/**
 * Loads a default currency matrix into the configuration database.
 */
public class CurrencyMatrixConfigPopulator {

  
  
  private static final Logger s_logger = LoggerFactory.getLogger(CurrencyMatrixConfigPopulator.class);
  
  private static final String[] CURRENCIES = new String[] {"EUR", "GBP", "CHF", "AUD", "SEK", "NZD", "CAD", "DKK", "JPY"};

  /**
   * Bloomberg currency matrix config name
   */
  public static final String BLOOMBERG_LIVE_DATA = "BloombergLiveData";
  
  /**
   * Synthetic currency matrix config name
   */
  public static final String SYNTHETIC_LIVE_DATA = "SyntheticLiveData";

  public CurrencyMatrixConfigPopulator(ConfigMaster cfgMaster) {
    populateCurrencyMatrixConfigMaster(cfgMaster);
  }

  public static ConfigMaster populateCurrencyMatrixConfigMaster(ConfigMaster cfgMaster) {
    storeCurrencyMatrix(cfgMaster, BLOOMBERG_LIVE_DATA, createBloombergConversionMatrix());
    storeCurrencyMatrix(cfgMaster, SYNTHETIC_LIVE_DATA, createSyntheticConversionMatrix());
    return cfgMaster;
  }

  private static void storeCurrencyMatrix(final ConfigMaster cfgMaster, final String name, final CurrencyMatrix currencyMatrix) {
    ConfigDocument<CurrencyMatrix> doc = new ConfigDocument<CurrencyMatrix>(CurrencyMatrix.class);
    doc.setName(name);
    doc.setValue(currencyMatrix);
    ConfigMasterUtils.storeByName(cfgMaster, doc);
  }

  public static CurrencyMatrix createBloombergConversionMatrix() {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    final Currency commonCross = Currency.USD;
    for (String currency : CURRENCIES) {
      matrix.setLiveData(commonCross, Currency.of(currency), UniqueId.of(SecurityUtils.BLOOMBERG_TICKER.toString(), currency + " Curncy"));
    }
    for (String currency : CURRENCIES) {
      final Currency target = Currency.of(currency);
      for (String currency2 : CURRENCIES) {
        if (!currency.equals(currency2)) {
          matrix.setCrossConversion(Currency.of(currency2), target, commonCross);
        }
      }
    }
    dumpMatrix(matrix);
    return matrix;
  }
  
  public static CurrencyMatrix createSyntheticConversionMatrix() {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    final Currency commonCross = Currency.USD;
    for (String currency : CURRENCIES) {
      matrix.setLiveData(commonCross, Currency.of(currency), UniqueId.of(SecurityUtils.OG_SYNTHETIC_TICKER.getName(), commonCross.toString() + currency));
    }

    for (String currency : CURRENCIES) {
      final Currency target = Currency.of(currency);
      for (String currency2 : CURRENCIES) {
        if (!currency.equals(currency2)) {
          matrix.setCrossConversion(Currency.of(currency2), target, commonCross);
        }
      }
    }
    dumpMatrix(matrix);
    return matrix;

  }
  
  public static void dumpMatrix(final CurrencyMatrix matrix) {
    StringBuilder sb = new StringBuilder();
    sb.append('\n');
    for (Currency x : matrix.getTargetCurrencies()) {
      sb.append('\t').append(x.getCode());
    }
    for (Currency y : matrix.getSourceCurrencies()) {
      sb.append('\n').append(y.getCode());
      for (Currency x : matrix.getTargetCurrencies()) {
        sb.append('\t').append(matrix.getConversion(y, x));
      }
    }
    s_logger.debug("Currency matrix = {}", sb);
  }
  
}
