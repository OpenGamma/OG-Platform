package com.opengamma.web.server.push;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 */
public class MasterChangeManagerImpl implements MasterChangeManager {

  private final Set<MasterChangeListener> _listeners = new CopyOnWriteArraySet<MasterChangeListener>();

  public MasterChangeManagerImpl(List<Pair<ChangeProvider, MasterType>> changeManagers) {
    for (Pair<ChangeProvider, MasterType> changeManagerAndType : changeManagers) {
      ChangeProvider changeProvider = changeManagerAndType.getFirst();
      final MasterType masterType = changeManagerAndType.getSecond();
      changeProvider.changeManager().addChangeListener(new ChangeListener() {
        @Override
        public void entityChanged(ChangeEvent event) {
          MasterChangeManagerImpl.this.entityChanged(masterType);
        }
      });
    }
  }

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
