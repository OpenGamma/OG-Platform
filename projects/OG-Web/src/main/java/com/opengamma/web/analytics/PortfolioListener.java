/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.position.PositionSource;
import com.opengamma.id.ObjectId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class PortfolioListener implements ChangeListener, AutoCloseable {

  private final ObjectId _portfolioId;
  private final AnalyticsView _view;
  private final PositionSource _positionSource;

  /* package */ PortfolioListener(ObjectId portfolioId, AnalyticsView view, PositionSource positionSource) {
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(positionSource, "positionSource");
    _portfolioId = portfolioId;
    _view = view;
    _positionSource = positionSource;
    _positionSource.changeManager().addChangeListener(this);
  }

  @Override
  public void close() {
    _positionSource.changeManager().removeChangeListener(this);
  }

  @Override
  public void entityChanged(ChangeEvent event) {
    if (event.getObjectId().equals(_portfolioId)) {
      _view.portfolioChanged();
    }
  }
}
