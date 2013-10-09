/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.util.ArgumentChecker;

/**
 * Immutable context class to hold providers of permission checks.
 */
public class ViewPermissionContext {

  private final ViewPermissionProvider _viewPermissionProvider;
  private final ViewPortfolioPermissionProvider _viewPortfolioPermissionProvider;

  public ViewPermissionContext(ViewPermissionProvider viewPermissionProvider,
                               ViewPortfolioPermissionProvider viewPortfolioPermissionProvider) {

    ArgumentChecker.notNull(viewPermissionProvider, "viewPermissionProvider");
    ArgumentChecker.notNull(viewPortfolioPermissionProvider, "viewPortfolioPermissionProvider");
    _viewPermissionProvider = viewPermissionProvider;
    _viewPortfolioPermissionProvider = viewPortfolioPermissionProvider;
  }

  public ViewPermissionProvider getViewPermissionProvider() {
    return _viewPermissionProvider;
  }

  public ViewPortfolioPermissionProvider getViewPortfolioPermissionProvider() {
    return _viewPortfolioPermissionProvider;
  }
}
