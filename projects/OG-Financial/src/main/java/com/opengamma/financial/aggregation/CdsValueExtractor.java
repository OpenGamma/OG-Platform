/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;

/**
 * Class uses an organization source to extract the Obligor from a CDS.
 * 
 * @param <T>  the value type
 */
public abstract class CdsValueExtractor<T> {

  /**
   * Extract the value (T) from the CDS if it can be found.
   *
   * @param cds  the CDS to extract the value from, not null
   * @return the extracted value if found, null otherwise
   */
  public abstract T extract(AbstractCreditDefaultSwapSecurity cds);

}
