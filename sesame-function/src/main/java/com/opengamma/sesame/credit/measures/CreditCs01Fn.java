/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.measures;

import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.trade.IndexCDSTrade;
import com.opengamma.sesame.trade.LegacyCDSTrade;
import com.opengamma.sesame.trade.StandardCDSTrade;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;

/**
 * CS01 function for credit types.
 */
public interface CreditCs01Fn extends CreditRiskMeasureFn<CurrencyAmount> {
  
  @Override
  @Output(OutputNames.CS01)
  Result<CurrencyAmount> priceStandardCds(Environment env, StandardCDSSecurity cds);

  @Override
  @Output(OutputNames.CS01)
  Result<CurrencyAmount> priceIndexCds(Environment env, IndexCDSSecurity cds);

  @Override
  @Output(OutputNames.CS01)
  Result<CurrencyAmount> priceLegacyCds(Environment env, LegacyCDSSecurity cds);

  @Override
  @Output(OutputNames.PRESENT_VALUE)
  Result<CurrencyAmount> priceStandardCds(Environment env, StandardCDSTrade cds);

  @Override
  @Output(OutputNames.PRESENT_VALUE)
  Result<CurrencyAmount> priceIndexCds(Environment env, IndexCDSTrade cds);

  @Override
  @Output(OutputNames.PRESENT_VALUE)
  Result<CurrencyAmount> priceLegacyCds(Environment env, LegacyCDSTrade cds);
  
}
