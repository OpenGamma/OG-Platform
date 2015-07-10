/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.isda.credit;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;

/**
 * An instance encapsulating an observed CDS price quote.
 */
public interface CdsQuote {
  
  /**
   * Convert this quote into its equivalent analytics type.
   * @return a {@link CDSQuoteConvention} instance
   */
  CDSQuoteConvention toQuoteConvention();
  
}
