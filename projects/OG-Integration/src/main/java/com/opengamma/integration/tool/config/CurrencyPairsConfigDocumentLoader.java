/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;

/**
 * Reads currency pairs from a text file and stores them in an instance of {@link CurrencyPairs} in the config master.
 * The pairs must be in the format AAA/BBB, one per line in the file.
 */
public class CurrencyPairsConfigDocumentLoader {

  private static final Logger s_logger = LoggerFactory.getLogger(CurrencyPairsConfigDocumentLoader.class);
  
  private final ConfigMaster _configMaster;
  private final String _dataFilePath;
  private final String _configName;

  /**
   * Creates an instance.
   * 
   * @param configMaster  the master for saving the currency pairs
   * @param dataFilePath  the path to the text file with the list of currency pairs
   * @param configName  the name for the {@link CurrencyPairs} in the config master
   */
  public CurrencyPairsConfigDocumentLoader(ConfigMaster configMaster, String dataFilePath, String configName) {
    _configMaster = configMaster;
    _dataFilePath = dataFilePath;
    _configName = configName;
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the loader.
   */
  public void run() {
    savePairs(loadPairs());
  }

  /**
   * Saves the {@link CurrencyPairs} in the config master.
   * If there is an existing {@link CurrencyPairs} in the master with the same name then it is updated.
   * 
   * @param pairs  the pairs to save
   */
  private void savePairs(Set<CurrencyPair> pairs) {
    CurrencyPairs currencyPairs = CurrencyPairs.of(pairs);
    ConfigItem<CurrencyPairs> configDocument = ConfigItem.of(currencyPairs, _configName, CurrencyPairs.class);    
    ConfigMasterUtils.storeByName(_configMaster, configDocument);
  }

  /**
   * Loads and returns the currency pairs from the text file specified by {@link #_dataFilePath}.
   * 
   * @return a set of {@link CurrencyPair}s loaded from the file
   */
  private Set<CurrencyPair> loadPairs() {
    BufferedReader reader = null;
    try {
      s_logger.debug("Loading currency pairs from " + _dataFilePath);
      reader = new BufferedReader(new FileReader(_dataFilePath));
      return readPairs(reader);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Unable to read data file " + _dataFilePath, e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          s_logger.warn("Failed to close reader", e);
        }
      }
    }
  }

  /**
   * Reads currency pairs from {@code reader} and creates instances of {@link CurrencyPair}.
   * 
   * @param reader  each line is expected to contain a currency pair in the form AAA/BBB
   * @return a set of currency pairs from {@code reader}
   * @throws IOException if {@code reader} can't be read
   */
  /* package */ Set<CurrencyPair> readPairs(BufferedReader reader) throws IOException {
    String pairStr;
    Set<CurrencyPair> pairs = new HashSet<CurrencyPair>();
    while ((pairStr = reader.readLine()) != null) {
      try {
        CurrencyPair pair = CurrencyPair.parse(pairStr.trim());
        if (pairs.add(pair)) {
          s_logger.debug("Added currency pair " + pair.getName());
        } else {
          s_logger.debug("Not adding duplicate currency pair " + pair.getName());
        }
      } catch (IllegalArgumentException e) {
        s_logger.warn("Unable to create currency pair from " + pairStr, e);
      }
    }
    return pairs;
  }
}
