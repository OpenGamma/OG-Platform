package com.opengamma.financial.currency;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.PlatformConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
   * @param configMaster For saving the currency pairs
   * @param dataFilePath Path to the text file with the list of currency pairs
   * @param configName Name for the {@link CurrencyPairs} in the config master
   */
  public CurrencyPairsConfigDocumentLoader(ConfigMaster configMaster, String dataFilePath, String configName) {
    _configMaster = configMaster;
    _dataFilePath = dataFilePath;
    _configName = configName;
  }

  /**
   * @param args 1) path to the currency pair data file
   *             2) name under which the {@link CurrencyPairs} should be saved in the config master
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      throw new IllegalArgumentException("2 args required: dataFile, configName");
    }
    PlatformConfigUtils.configureSystemProperties(PlatformConfigUtils.RunMode.SHAREDDEV);
    ApplicationContext applicationContext = new ClassPathXmlApplicationContext("com/opengamma/financial/demoMasters.xml");
    ConfigMaster configMaster = (ConfigMaster) applicationContext.getBean("sharedConfigMaster");
    CurrencyPairsConfigDocumentLoader loader = new CurrencyPairsConfigDocumentLoader(configMaster, args[0], args[1]);
    loader.savePairs();
  }

  /**
   * Saves the {@link CurrencyPairs} in the config master.  If there is an existing {@link CurrencyPairs} in the
   * master with the same name then it is updated.
   */
  private void savePairs() {
    ConfigDocument<CurrencyPairs> existingPairs = findExisting();
    Set<CurrencyPair> pairs = loadPairs();
    CurrencyPairs currencyPairs = new CurrencyPairs(pairs);
    ConfigDocument<CurrencyPairs> configDocument = new ConfigDocument<CurrencyPairs>(CurrencyPairs.class);
    configDocument.setName(_configName);
    configDocument.setValue(currencyPairs);

    if (existingPairs == null) {
      _configMaster.add(configDocument);
    } else {
      configDocument.setUniqueId(existingPairs.getUniqueId());
      _configMaster.update(configDocument);
    }
  }

  /**
   * @return {@link CurrencyPairs} in the config master with a name matching {@link #_configName} or null if there
   * isn't a match
   */
  private ConfigDocument<CurrencyPairs> findExisting() {
    ConfigSearchRequest<CurrencyPairs> searchRequest = new ConfigSearchRequest<CurrencyPairs>(CurrencyPairs.class);
    searchRequest.setName(_configName);
    ConfigSearchResult<CurrencyPairs> searchResult = _configMaster.search(searchRequest);
    return searchResult.getFirstDocument();
  }

  /**
   * Loads and returns the currency pairs from the text file specified by {@link #_dataFilePath}.
   * @return A set of {@link CurrencyPair}s loaded from the file
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
   * @param reader Each line is expected to contain a currency pair in the form AAA/BBB
   * @return A set of currency pairs from {@code reader}
   * @throws IOException If {@code reader} can't be read
   */
  /* package */ Set<CurrencyPair> readPairs(BufferedReader reader) throws IOException {
    String pairStr;
    Set<CurrencyPair> pairs = new HashSet<CurrencyPair>();
    while ((pairStr = reader.readLine()) != null) {
      try {
        CurrencyPair pair = CurrencyPair.of(pairStr.trim());
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
