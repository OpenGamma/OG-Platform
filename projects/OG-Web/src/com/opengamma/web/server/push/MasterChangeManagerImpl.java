package com.opengamma.web.server.push;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.util.ArgumentChecker;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 */
/* package */ class MasterChangeManagerImpl implements MasterChangeManager {

  private final Set<MasterChangeListener> _listeners = new CopyOnWriteArraySet<MasterChangeListener>();

  @Override
  public void addChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  @Override
  public void removeChangeListener(MasterChangeListener listener) {
    _listeners.remove(listener);
  }

  private void entityChanged(MasterType masterType) {
    for (MasterChangeListener listener : _listeners) {
      listener.masterChanged(masterType);
    }
  }
}
