/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;

/**
 * Class uses an organization source to extract the Obligor from a CDS.
 */
public abstract class CdsOptionValueExtractor<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(CdsOptionValueExtractor.class);

  /**
   * Extract the value (T) from the CDS Option if it can be found.
   *
   * @param cdsOption the CDS Option to extract the value from
   * @return the extracted value if found, null otherwise
   */
  abstract public T extract(CreditDefaultSwapOptionSecurity cdsOption);

}
