/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of {@link FunctionConfigurationSource} that supports listening to changes from an underlying provider.
 */
public abstract class DynamicFunctionConfigurationSource implements FunctionConfigurationSource {

  private static final Logger s_logger = LoggerFactory.getLogger(DynamicFunctionConfigurationSource.class);

  private final ChangeManager _underlying;

  private final ChangeListener _changeListener = new ChangeListener() {
    @Override
    public void entityChanged(final ChangeEvent event) {
      if (isPropogateEvent(event)) {
        s_logger.info("Function configuration change at {} caused by {}", DynamicFunctionConfigurationSource.this, event);
        _changeManager.entityChanged(ChangeType.CHANGED, FunctionConfigurationSource.OBJECT_ID, event.getVersionFrom(), event.getVersionTo(), event.getVersionInstant());
      } else {
        s_logger.debug("Ignoring event {} at {}", event, DynamicFunctionConfigurationSource.this);
      }
    }
  };

  private final BasicChangeManager _changeManager = new BasicChangeManager() {

    @Override
    public synchronized void addChangeListener(final ChangeListener listener) {
      ArgumentChecker.notNull(listener, "listener");
      if (getListeners().isEmpty()) {
        s_logger.info("Registering listener for {}", DynamicFunctionConfigurationSource.this);
        _underlying.addChangeListener(_changeListener);
      }
      super.addChangeListener(listener);
    }

    @Override
    public synchronized void removeChangeListener(final ChangeListener listener) {
      super.removeChangeListener(listener);
      if (getListeners().isEmpty()) {
        s_logger.info("Removing listener for {}", DynamicFunctionConfigurationSource.this);
        _underlying.removeChangeListener(_changeListener);
      }
    }

  };

  public DynamicFunctionConfigurationSource(final ChangeManager underlying) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
  }

  public DynamicFunctionConfigurationSource(final ChangeProvider underlying) {
    this(underlying.changeManager());
  }

  protected abstract boolean isPropogateEvent(ChangeEvent event);

  protected abstract FunctionConfigurationBundle getFunctionConfiguration(VersionCorrection version);

  @Override
  public FunctionConfigurationBundle getFunctionConfiguration(Instant version) {
    return getFunctionConfiguration(VersionCorrection.of(version, version));
  }

  @Override
  public final ChangeManager changeManager() {
    return _changeManager;
  }

}
