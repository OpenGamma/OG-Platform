/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.definition;

import com.opengamma.financial.equity.future.derivative.EquityIndexDividendFuture;
import javax.time.calendar.ZonedDateTime;
import org.apache.commons.lang.Validate;

/**
 * Each time a view is recalculated, the security definition 
 * creates an analytic derivative for the current time. 
 */
public class EquityIndexDividendFutureDefinition {

  /*
   * See InterestRateFutureTransactionDefinition. Need to 
  
  public EquityIndexDividendFuture toDerivative(ZonedDateTime date, double priceMeaningWhat, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    
    double fixingDate = 0.0;
    double deliveryDate = 0.0;
    double strike = 0.0;
    double pointValue = 10.0;
    String indexName = "";
    String curveName = "";
    
    return EquityIndexDividendFuture(fixingDate, deliveryDate, priceMeaningWhat, pointValue,indexName, curveName); 
  }
   */

}
