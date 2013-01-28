/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.core.security.Security;

/**
 * @param <T> The security type TODO does this need to be extend Security?
 */
public interface CellValueProvider<T extends Security> {

  Object getValue(T security);
}
