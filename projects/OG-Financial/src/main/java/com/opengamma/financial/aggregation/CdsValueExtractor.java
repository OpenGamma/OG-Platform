/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;

/**
 * Class uses an organization source to extract the Obligor from a CDS.
 */
public abstract class CdsValueExtractor<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(CdsValueExtractor.class);

  /**
   * Extract the value (T) from the CDS if it can be found.
   *
   * @param cds the CDS to extract the value from
   * @return the extracted value if found, null otherwise
   */
  abstract public T extract(AbstractCreditDefaultSwapSecurity cds);

}
