/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.converter;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.credit.IsdaCreditCurve;
import com.opengamma.util.result.Result;

/**
 * Converts a {@link IndexCDSSecurity} to a its equivalent analytics type.
 */
public interface IndexCdsConverterFn {
  
  /**
   * Convert the given standard cds to its equivalent analytics type.
   * 
   * @param env the pricing environment
   * @param cds the index cds
   * @param curve the curve resolved for the cds
   * @return the constructed cds analytic
   */
  Result<CDSAnalytic> toCdsAnalytic(Environment env, IndexCDSSecurity cds, IsdaCreditCurve curve);
  
}
