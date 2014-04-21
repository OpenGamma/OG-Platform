/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewAutoStartManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Manages the set of automatically started views and listens to the
 * config source such that any changes to the set is detected.
 *
 * It is expected that this will be used in conjunction with a scheduled
 * monitoring job such that views that have been shutdown will get
 * restarted and the configuration of new auto-start views will be
 * automatically detected and started.
 * */
public class ListeningViewAutoStartManager implements ViewAutoStartManager {

  /**
   * The config source, containing the auto start view definitions.
   */
  private final ConfigSource _configSource;

  /**
   * The current set of views which are eligible for auto-starting.
   */
  private final ConcurrentMap<ObjectId, Pair<String, AutoStartViewDefinition>> _autoStartViews = new ConcurrentHashMap<>();

  /**
   * Indicates if the initialization method has been run.
   */
  private boolean _isInitialized;

  /**
   * Constructor.
   *
   * @param configSource the config source containing the auto start
   * view definitions.
   */
  public ListeningViewAutoStartManager(ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
    _configSource.changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {

        ObjectId configItemId = event.getObjectId();

        switch (event.getType()) {
          case ADDED:
            // Whether item is new or updated, just want to add into the map, so fall through
          case CHANGED:
            VersionCorrection versionCorrection = VersionCorrection.ofVersionAsOf(event.getVersionFrom());
            ConfigItem<?> configItem = _configSource.get(configItemId, versionCorrection);
            if (configItem.getType() == AutoStartViewDefinition.class) {
              _autoStartViews.put(configItemId, Pairs.of(configItem.getName(),
                                                         (AutoStartViewDefinition) configItem.getValue()));
            }
            break;
          case REMOVED:
            _autoStartViews.remove(configItemId);
            break;
        }
      }
    });
  }

  @Override
  public void initialize() {
    for (ConfigItem<AutoStartViewDefinition> configItem : _configSource.getAll(AutoStartViewDefinition.class, VersionCorrection.LATEST)) {
      _autoStartViews.put(configItem.getObjectId(), Pairs.of(configItem.getName(), configItem.getValue()));
    }
    _isInitialized = true;
  }

  @Override
  public Map<String, AutoStartViewDefinition> getAutoStartViews() {

    if (!_isInitialized) {
      throw new IllegalStateException("Manager has not been initialized, cannot access data");
    }

    ImmutableMap.Builder<String, AutoStartViewDefinition> builder = ImmutableMap.builder();
    for (Pair<String, AutoStartViewDefinition> pair : _autoStartViews.values()) {
      builder.put(pair.getFirst(), pair.getSecond());
    }
    return builder.build();
  }
}
