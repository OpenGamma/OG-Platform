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
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link ChangeProvider} that translates configuration changes from a {@link ConfigMaster} to type summaries.
 */
public class ConfigMasterChangeProvider extends AbstractConfigChangeProvider {

  private static final ConcurrentMap<ConfigMaster, ConfigMasterChangeProvider> s_instances = new MapMaker().weakValues().makeMap();

  private final ConfigMaster _configMaster;

  public ConfigMasterChangeProvider(final ConfigMaster configMaster) {
    _configMaster = ArgumentChecker.notNull(configMaster, "configMaster");
  }

  public static ConfigMasterChangeProvider of(final ConfigMaster configMaster) {
    ConfigMasterChangeProvider changes = s_instances.get(configMaster);
    if (changes == null) {
      changes = new ConfigMasterChangeProvider(configMaster);
      final ConfigMasterChangeProvider existing = s_instances.putIfAbsent(configMaster, changes);
      if (existing != null) {
        return existing;
      }
    }
    return changes;
  }

  @Override
  protected ChangeProvider getUnderlying() {
    return _configMaster;
  }

  @Override
  protected void configAdded(final ChangeEvent event) {
    final ConfigDocument document = _configMaster.get(event.getObjectId(), getNewVersion(event));
    added(event, document.getType());
  }

  @Override
  protected void configChanged(final ChangeEvent event) {
    final ConfigDocument oldDocument = _configMaster.get(event.getObjectId(), getOldVersion(event));
    final ConfigDocument newDocument = _configMaster.get(event.getObjectId(), getNewVersion(event));
    if (oldDocument.getType().equals(newDocument.getType())) {
      changed(event, newDocument.getType());
    } else {
      removed(event, oldDocument.getType());
      added(event, newDocument.getType());
    }
  }

  @Override
  protected void configRemoved(final ChangeEvent event) {
    final ConfigDocument document = _configMaster.get(event.getObjectId(), getOldVersion(event));
    removed(event, document.getType());
  }

}
