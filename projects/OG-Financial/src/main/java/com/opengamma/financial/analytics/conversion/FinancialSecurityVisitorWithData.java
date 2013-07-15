/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.financial.security.future.FutureSecurity;

/**
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The type of the result
 */
public interface FinancialSecurityVisitorWithData<DATA_TYPE, RESULT_TYPE> {

  RESULT_TYPE visit(FutureSecurity security, DATA_TYPE data);

}
