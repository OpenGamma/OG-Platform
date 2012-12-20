/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalIdOrderConfig;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;

/**
 * Class to save a reasonable default ExternalIdOrderConfig in the config database
 */
public class ExternalIdOrderConfigDocumentLoader {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(CurrencyPairsConfigDocumentLoader.class);
  
  private final ConfigMaster _configMaster;
  private final String _configName;

  /**
   * Creates an instance.
   * 
   * @param configMaster  the master for saving the currency pairs
   * @param configName  the name for the {@link CurrencyPairs} in the config master
   */
  public ExternalIdOrderConfigDocumentLoader(ConfigMaster configMaster, String configName) {
    _configMaster = configMaster;
    _configName = configName;
  }
  
  public void run() {
    ConfigItem<ExternalIdOrderConfig> doc = ConfigItem.of(ExternalIdOrderConfig.DEFAULT_CONFIG);
    doc.setName(_configName);
    ConfigMasterUtils.storeByName(_configMaster, doc);
  }

}
