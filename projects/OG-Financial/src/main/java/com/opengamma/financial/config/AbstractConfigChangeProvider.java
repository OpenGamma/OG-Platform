/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.config;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.ChangeType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionReinitializer;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link ChangeProvider} that translates configuration changes from document identifiers to type summaries.
 * <p>
 * This can be used for re-initialization of functions that look up configuration items by name.
 */
public abstract class AbstractConfigChangeProvider implements ChangeProvider {

  /**
   * The scheme used in object identifiers that this generates change notifications for.
   */
  public static final String CONFIG_TYPE_SCHEME = "ConfigItemType";

  /**
   * Requests the function be reinitialized whenever configurations of the given type change.
   * 
   * @param context the compilation context, not null
   * @param function the function, not null
   * @param type the type to watch
   */
  public static void reinitOnChanges(final FunctionCompilationContext context, final FunctionDefinition function, final Class<?> type) {
    final FunctionReinitializer reinit = context.getFunctionReinitializer();
    if (reinit != null) {
      reinit.reinitializeFunction(function, ObjectId.of(CONFIG_TYPE_SCHEME, type.getName()));
    }
  }

  private final ChangeListener _changeListener = new ChangeListener() {
    @Override
    public void entityChanged(final ChangeEvent event) {
      switch (event.getType()) {
        case ADDED:
          configAdded(event);
          break;
        case CHANGED:
          configChanged(event);
          break;
        case REMOVED:
          configRemoved(event);
          break;
        default:
          throw new UnsupportedOperationException(event.toString());
      }
    }
  };

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

  protected abstract ChangeProvider getUnderlying();

  protected VersionCorrection getNewVersion(final ChangeEvent event) {
    return VersionCorrection.of(event.getVersionFrom(), event.getVersionInstant());
  }

  protected VersionCorrection getOldVersion(final ChangeEvent event) {
    return VersionCorrection.of(event.getVersionFrom().minusNanos(1L), event.getVersionInstant());
  }

  protected abstract void configAdded(ChangeEvent event);

  protected abstract void configChanged(ChangeEvent event);

  protected abstract void configRemoved(ChangeEvent event);

  private void notifyListeners(final ChangeEvent event, final Class<?> clazz, final ChangeType type) {
    _changeManager.entityChanged(type, ObjectId.of(CONFIG_TYPE_SCHEME, clazz.getName()), event.getVersionFrom(), event.getVersionTo(), event.getVersionInstant());
  }

  protected void added(final ChangeEvent event, final Class<?> type) {
    notifyListeners(event, type, ChangeType.ADDED);
  }

  protected void changed(final ChangeEvent event, final Class<?> type) {
    notifyListeners(event, type, ChangeType.CHANGED);
  }

  protected void removed(final ChangeEvent event, final Class<?> type) {
    notifyListeners(event, type, ChangeType.REMOVED);
  }

  // ChangeProvider

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
