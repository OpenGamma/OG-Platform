/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractMaster;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class MasterNotificationListener<D extends AbstractDocument,
                                               M extends AbstractMaster<D> & ChangeProvider>  // CSIGNORE
    implements ChangeListener, AutoCloseable {

  private final M _master;
  private final AnalyticsView _view;

  /* package */ MasterNotificationListener(M master, AnalyticsView view) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(view, "view");
    _master = master;
    _view = view;
    _master.changeManager().addChangeListener(this);
  }

  @Override
  public void entityChanged(ChangeEvent event) {
    _view.entityChanged(new MasterChangeNotification<>(event, _master));
  }

  @Override
  public void close() {
    _master.changeManager().removeChangeListener(this);
  }
}
