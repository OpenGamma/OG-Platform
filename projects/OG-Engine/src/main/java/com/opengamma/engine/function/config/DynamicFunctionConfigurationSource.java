/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper for a {@link AbstractFunctionConfigurationBean} that supports dynamic configurations by recreating the bean for each version timestamp.
 */
public abstract class DynamicFunctionConfigurationSource implements FunctionConfigurationSource {

  private static final Logger s_logger = LoggerFactory.getLogger(DynamicFunctionConfigurationSource.class);

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
        addListenerToUnderlyings(_changeListener);
      }
      super.addChangeListener(listener);
    }

    @Override
    public synchronized void removeChangeListener(final ChangeListener listener) {
      super.removeChangeListener(listener);
      if (getListeners().isEmpty()) {
        s_logger.info("Removing listener for {}", DynamicFunctionConfigurationSource.this);
        removeListenerFromUnderlyings(_changeListener);
      }
    }

  };

  protected void addListenerToUnderlyings(ChangeListener listener) {
    getUnderlyingChangeManager().addChangeListener(listener);
  }

  protected void removeListenerFromUnderlyings(ChangeListener listener) {
    getUnderlyingChangeManager().removeChangeListener(listener);
  }

  protected abstract ChangeManager getUnderlyingChangeManager();

  protected abstract boolean isPropogateEvent(ChangeEvent event);

  protected abstract VersionedFunctionConfigurationBean createConfiguration();

  @Override
  public FunctionConfigurationBundle getFunctionConfiguration(Instant version) {
    final VersionedFunctionConfigurationBean factory = createConfiguration();
    factory.setVersionCorrection(VersionCorrection.of(version, version));
    return factory.createObject().getFunctionConfiguration(version);
  }

  @Override
  public final ChangeManager changeManager() {
    return _changeManager;
  }

}
