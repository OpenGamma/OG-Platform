/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.fra;

import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.function.Output;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Calculate analytics values for a FRA.
 */
public interface FRAFn {

  /**
   * Calculate the par rate for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the FRA to calculate the rate for
   * @return result containing the rate if successful, a Failure otherwise
   */
  @Output(OutputNames.PAR_RATE)
  Result<Double> calculateParRate(Environment env, FRASecurity security);

  /**
   * Calculate the present value for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the FRA to calculate the PV for
   * @return result containing the present value if successful, a Failure otherwise
   */
  @Output(OutputNames.PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculatePV(Environment env, FRASecurity security);
  
  /**
   * Calculate the par rate for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the FRA to calculate the rate for
   * @return result containing the rate if successful, a Failure otherwise
   */
  @Output(OutputNames.PAR_RATE)
  Result<Double> calculateParRate(Environment env, ForwardRateAgreementSecurity security);

  /**
   * Calculate the present value for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the FRA to calculate the PV for
   * @return result containing the present value if successful, a Failure otherwise
   */
  @Output(OutputNames.PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculatePV(Environment env, ForwardRateAgreementSecurity security);

  /**
   * Calculate the PV01 for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the fra to calculate the PV01 for
   * @return result containing the PV01 if successful, a Failure otherwise
   */
  @Output(value = OutputNames.PV01)
  Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, ForwardRateAgreementSecurity security);

  /**
   * Calculate the PV01 for a FRA security.
   *
   * @param env the environment used for calculation
   * @param security the FRA to calculate the PV01 for
   * @return result containing the present value if successful, a Failure otherwise
   */
  @Output(OutputNames.PRESENT_VALUE)
  Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01(Environment env, FRASecurity security);

}
