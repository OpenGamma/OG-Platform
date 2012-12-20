/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.core.security.Security;

/**
 * A factory of {@link Summary} instances for a particular security type.
 * 
 * @param <T>  the type of security
 */
public interface SummaryFactory<T extends Security> {

  String getSecurityType();
  
  Summary getSummary(T security);
  
}
