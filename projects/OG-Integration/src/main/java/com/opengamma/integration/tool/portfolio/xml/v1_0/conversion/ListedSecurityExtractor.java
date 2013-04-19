/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import com.opengamma.master.security.ManageableSecurity;

/**
 * Extractor for listed securities.
 */
public interface ListedSecurityExtractor {

  /**
   * Extracts the securities.
   * 
   * @return the securities, not null
   */
  ManageableSecurity[] extract();

}
