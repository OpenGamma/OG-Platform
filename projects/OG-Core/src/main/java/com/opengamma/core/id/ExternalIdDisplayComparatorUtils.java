/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Utility class to enable easy fetching of display comparator for externalId bundles.
 */
public class ExternalIdDisplayComparatorUtils {
  private static final Logger s_logger = LoggerFactory.getLogger(ExternalIdDisplayComparatorUtils.class);
  
  /**
   * Default name for config object defining behavior of ExternalIdDisplayComparator
   */
  public static final String DEFAULT_CONFIG_NAME = "DEFAULT";
  
  public static ExternalIdDisplayComparator getComparator(ConfigSource configSource, String name) {
    ArgumentChecker.notNull(name, "name");
    ExternalIdOrderConfig config = null;
    if (configSource == null) {
      s_logger.error("null config source, defaulting to default configuration");
      return new ExternalIdDisplayComparator(ExternalIdOrderConfig.DEFAULT_CONFIG);
    } else {
      try {
        config = configSource.getLatestByName(ExternalIdOrderConfig.class, name);
      } catch (UniformInterfaceException404NotFound fourOhFour) {
        // this happens if there's nothing of this type in there...  swallow it
        config = null;
      }
      if (config == null) {
        s_logger.error("No ExternalIdOrderConfig object called " + name + " in config database, defaulting");
        return new ExternalIdDisplayComparator(ExternalIdOrderConfig.DEFAULT_CONFIG);
      } else {
        return new ExternalIdDisplayComparator(config);
      }
    }
  }
}
