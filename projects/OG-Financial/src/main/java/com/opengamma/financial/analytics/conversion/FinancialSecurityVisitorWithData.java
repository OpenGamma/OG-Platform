/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;

/**
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The type of the result
 */
public interface FinancialSecurityVisitorWithData<DATA_TYPE, RESULT_TYPE> {

  RESULT_TYPE visit(FutureSecurity security, DATA_TYPE data);

  RESULT_TYPE visitInterestRateSwapSecurity(InterestRateSwapSecurity security, DATA_TYPE data);

  RESULT_TYPE visitForwardRateAgreementSecurity(ForwardRateAgreementSecurity security, DATA_TYPE data);

}
