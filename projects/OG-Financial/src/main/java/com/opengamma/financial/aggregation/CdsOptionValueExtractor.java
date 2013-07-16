/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;

/**
 * Class uses an organization source to extract the Obligor from a CDS.
 * 
 * @param <T>  the value type
 */
public abstract class CdsOptionValueExtractor<T> {

  /**
   * Extract the value (T) from the CDS Option if it can be found.
   *
   * @param cdsOption the CDS Option to extract the value from
   * @return the extracted value if found, null otherwise
   */
  public abstract T extract(CreditDefaultSwapOptionSecurity cdsOption);

}
