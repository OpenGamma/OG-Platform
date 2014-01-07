/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.client.PortfolioFilter;
import com.opengamma.livedata.UserPrincipal;

/**
 * Responsible for providing a filter for
 * portfolio/user permissioning.
 */
public interface ViewPortfolioPermissionProvider {

  PortfolioFilter createPortfolioFilter(UserPrincipal user);
}
