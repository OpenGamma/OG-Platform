/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.config;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link ChangeProvider} that translates configuration changes from a {@link ConfigSource} to type summaries.
 */
public class ConfigSourceChangeProvider extends AbstractConfigChangeProvider {

  private static final ConcurrentMap<ConfigSource, ConfigSourceChangeProvider> s_instances = new MapMaker().weakValues().makeMap();

  private final ConfigSource _configSource;

  public ConfigSourceChangeProvider(final ConfigSource configSource) {
    _configSource = ArgumentChecker.notNull(configSource, "configSource");
  }

  public static ConfigSourceChangeProvider of(final ConfigSource configSource) {
    ConfigSourceChangeProvider changes = s_instances.get(configSource);
    if (changes == null) {
      changes = new ConfigSourceChangeProvider(configSource);
      final ConfigSourceChangeProvider existing = s_instances.putIfAbsent(configSource, changes);
      if (existing != null) {
        return existing;
      }
    }
    return changes;
  }

  @Override
  protected ChangeProvider getUnderlying() {
    return _configSource;
  }

  @Override
  protected void configAdded(final ChangeEvent event) {
    final ConfigItem<?> item = _configSource.get(event.getObjectId(), getNewVersion(event));
    added(event, item.getType());
  }

  @Override
  protected void configChanged(final ChangeEvent event) {
    final ConfigItem<?> oldItem = _configSource.get(event.getObjectId(), getOldVersion(event));
    final ConfigItem<?> newItem = _configSource.get(event.getObjectId(), getNewVersion(event));
    if (oldItem.getType().equals(newItem.getType())) {
      changed(event, newItem.getType());
    } else {
      removed(event, oldItem.getType());
      added(event, newItem.getType());
    }
  }

  @Override
  protected void configRemoved(final ChangeEvent event) {
    final ConfigItem<?> item = _configSource.get(event.getObjectId(), getOldVersion(event));
    removed(event, item.getType());
  }

}
