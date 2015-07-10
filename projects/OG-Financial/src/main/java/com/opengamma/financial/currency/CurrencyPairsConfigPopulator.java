/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;

/**
 * Loads a default currency pair into the configuration database.
 */
public class CurrencyPairsConfigPopulator {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(CurrencyPairsConfigPopulator.class);

  public static ConfigMaster populateCurrencyPairsConfigMaster(final ConfigMaster cfgMaster) {
    storeCurrencyPairs(cfgMaster, CurrencyPairs.DEFAULT_CURRENCY_PAIRS, createCurrencyPairs());
    return cfgMaster;
  }

  public static CurrencyPairs createCurrencyPairs() {
    final InputStream inputStream = CurrencyPairsConfigPopulator.class.getResourceAsStream("market-convention-currency-pairs.csv");
    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String pairStr;
    final Set<CurrencyPair> pairs = new HashSet<CurrencyPair>();
    try {
      while ((pairStr = reader.readLine()) != null) {
        try {
          final CurrencyPair pair = CurrencyPair.parse(pairStr.trim());
          pairs.add(pair);
        } catch (final IllegalArgumentException e) {
          s_logger.debug/*warn*/("Unable to create currency pair from " + pairStr, e);
        }
      }
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Problem loading currency pairs into configuration database", ex);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return CurrencyPairs.of(pairs);
  }

  private static void storeCurrencyPairs(final ConfigMaster cfgMaster, final String name, final CurrencyPairs currencyPairs) {
    final ConfigItem<CurrencyPairs> doc = ConfigItem.of(currencyPairs);
    doc.setName(name);
    ConfigMasterUtils.storeByName(cfgMaster, doc);
  }

}
