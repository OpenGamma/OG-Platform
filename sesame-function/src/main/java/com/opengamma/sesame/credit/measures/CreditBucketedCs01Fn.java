/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.measures;

import com.opengamma.financial.analytics.TenorLabelledMatrix1D;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.function.Output;
import com.opengamma.util.result.Result;

/**
 * Bucketed CS01 function for credit types.
 */
public interface CreditBucketedCs01Fn extends CreditRiskMeasureFn<TenorLabelledMatrix1D> {
  
  @Override
  @Output(OutputNames.BUCKETED_CS01)
  Result<TenorLabelledMatrix1D> priceStandardCds(Environment env, StandardCDSSecurity cds);

  @Override
  @Output(OutputNames.BUCKETED_CS01)
  Result<TenorLabelledMatrix1D> priceIndexCds(Environment env, IndexCDSSecurity cds);

  @Override
  @Output(OutputNames.BUCKETED_CS01)
  Result<TenorLabelledMatrix1D> priceLegacyCds(Environment env, LegacyCDSSecurity cds);
  
}
