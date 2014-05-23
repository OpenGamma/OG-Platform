/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ObjectId;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link ChangeProvider} that translates historical time series changes from a {@link HistoricalTimeSeriesSource} to one common representing changes in any hts.
 */
public class HistoricalTimeSeriesSourceChangeProvider implements ChangeProvider {

  /**
   * Object used for registering changes in HTS
   */
  public static final ObjectId ALL_HISTORICAL_TIME_SERIES = ObjectId.of("OpenGamma", "AllHistoricalTimeSeries");

  private final HistoricalTimeSeriesSource _htsSource;

  public HistoricalTimeSeriesSourceChangeProvider(final HistoricalTimeSeriesSource htsSource) {
    _htsSource = ArgumentChecker.notNull(htsSource, "htsSource");
  }

  private final BasicChangeManager _changeManager = new BasicChangeManager() {

    @Override
    public synchronized void addChangeListener(final ChangeListener listener) {
      ArgumentChecker.notNull(listener, "listener");
      if (getListeners().isEmpty()) {
        getUnderlying().changeManager().addChangeListener(_changeListener);
      }
      super.addChangeListener(listener);
    }

    @Override
    public synchronized void removeChangeListener(final ChangeListener listener) {
      super.removeChangeListener(listener);
      if (getListeners().isEmpty()) {
        getUnderlying().changeManager().removeChangeListener(_changeListener);
      }
    }

  };

  private final ChangeListener _changeListener = new ChangeListener() {
    @Override
    public void entityChanged(final ChangeEvent event) {
      _changeManager.entityChanged(ChangeType.CHANGED, ALL_HISTORICAL_TIME_SERIES, event.getVersionFrom(), event.getVersionTo(), event.getVersionInstant());
    }
  };

  protected ChangeProvider getUnderlying() {
    return _htsSource;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }
}
