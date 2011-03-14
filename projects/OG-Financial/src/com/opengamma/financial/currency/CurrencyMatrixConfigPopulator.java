/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;

/**
 * Loads a default currency matrix into the configuration database.
 */
public class CurrencyMatrixConfigPopulator {

  private static final Logger s_logger = LoggerFactory.getLogger(CurrencyMatrixConfigPopulator.class);

  public CurrencyMatrixConfigPopulator(ConfigMaster cfgMaster) {
    populateCurrencyMatrixConfigMaster(cfgMaster);
  }

  public static ConfigMaster populateCurrencyMatrixConfigMaster(ConfigMaster cfgMaster) {
    ConfigDocument<CurrencyMatrix> doc = new ConfigDocument<CurrencyMatrix>();
    doc.setName("BloombergLiveData");
    doc.setValue(createBloombergConversionMatrix());
    ConfigMasterUtils.storeByName(cfgMaster, doc);
    return cfgMaster;
  }

  public static CurrencyMatrix createBloombergConversionMatrix() {
    final String[] currencies = new String[] {"EUR", "GBP", "CHF", "AUD", "SEK", "NZD", "CAD", "DKK", "JPY"};
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    final CurrencyUnit commonCross = CurrencyUnit.USD;
    for (String currency : currencies) {
      matrix.setLiveData(commonCross, CurrencyUnit.of(currency), UniqueIdentifier.of(SecurityUtils.BLOOMBERG_TICKER.toString(), currency + " Curncy"));
    }
    for (String currency : currencies) {
      final CurrencyUnit target = CurrencyUnit.of(currency);
      for (String currency2 : currencies) {
        if (!currency.equals(currency2)) {
          matrix.setCrossConversion(CurrencyUnit.of(currency2), target, commonCross);
        }
      }
    }
    dumpMatrix(matrix);
    return matrix;
  }
  
  public static void dumpMatrix(final CurrencyMatrix matrix) {
    StringBuilder sb = new StringBuilder();
    sb.append('\n');
    for (CurrencyUnit x : matrix.getTargetCurrencies()) {
      sb.append('\t').append(x.getCode());
    }
    for (CurrencyUnit y : matrix.getSourceCurrencies()) {
      sb.append('\n').append(y.getCode());
      for (CurrencyUnit x : matrix.getTargetCurrencies()) {
        sb.append('\t').append(matrix.getConversion(y, x));
      }
    }
    s_logger.debug("Currency matrix = {}", sb);
  }

}
