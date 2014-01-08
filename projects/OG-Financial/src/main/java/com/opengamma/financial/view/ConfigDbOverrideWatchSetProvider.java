/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link WatchSetProvider} that takes a config reference and replaces it with an alternative scheme and name of the document.
 */
public class ConfigDbOverrideWatchSetProvider implements WatchSetProvider {

  private final String _configScheme;
  private final ConfigMaster _configMaster;
  private final Set<String> _schemes;

  public ConfigDbOverrideWatchSetProvider(final String configScheme, final ConfigMaster configMaster, final Set<String> schemes) {
    ArgumentChecker.notNull(configScheme, "configScheme");
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(schemes, "schemes");
    _configScheme = configScheme;
    _configMaster = configMaster;
    _schemes = new HashSet<String>(schemes);
  }

  @Override
  public Set<ObjectId> getAdditionalWatchSet(final Set<ObjectId> watchSet) {
    final Set<ObjectId> result = new HashSet<ObjectId>();
    for (String scheme : _schemes) {
      for (ObjectId watch : watchSet) {
        if (_configScheme.equals(watch.getScheme())) {
          try {
            final ConfigDocument doc = _configMaster.get(watch, VersionCorrection.LATEST);
            result.add(ObjectId.of(scheme, doc.getName()));
          } catch (DataNotFoundException ex) {
            // ignore
          }
        }
      }
    }
    return result;
  }

}
