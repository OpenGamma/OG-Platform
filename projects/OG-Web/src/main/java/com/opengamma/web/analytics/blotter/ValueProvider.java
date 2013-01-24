/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.master.security.ManageableSecurity;

/**
*
*/
public interface ValueProvider<T extends ManageableSecurity> {

  Object getValue(T security);
}
