/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
//import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link WatchSetProvider} that takes a config reference and replaces it with an alternative scheme and name of the document.
 */
public class ConfigDbOverrideWatchSetProvider implements WatchSetProvider {

  private final String _configScheme;
//  private final Map<ConfigTypeMaster<?>, String> _mastersToSchemes;

  // REVIEW 2011-01-06 Andrew -- How can the config document be retrieved with just a UID? The config type is stored in the database

  public ConfigDbOverrideWatchSetProvider(final String configScheme, final ConfigMaster configMaster, final Map<Class<?>, String> classesToSchemes) {
    ArgumentChecker.notNull(configScheme, "configScheme");
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(classesToSchemes, "classesToSchemes");
    _configScheme = configScheme;
//    _mastersToSchemes = new HashMap<ConfigTypeMaster<?>, String>();
    for (Map.Entry<Class<?>, String> classToScheme : classesToSchemes.entrySet()) {
//      _mastersToSchemes.put(configMaster.typed(classToScheme.getKey()), classToScheme.getValue());
    }
  }

  @Override
  public Set<UniqueIdentifier> getAdditionalWatchSet(final Set<UniqueIdentifier> watchSet) {
    final Set<UniqueIdentifier> result = new HashSet<UniqueIdentifier>();
//    for (Map.Entry<ConfigTypeMaster<?>, String> masterToScheme : _mastersToSchemes.entrySet()) {
//      for (UniqueIdentifier watch : watchSet) {
//        if (_configScheme.equals(watch.getScheme())) {
//          try {
//            final ConfigDocument<?> doc = masterToScheme.getKey().get(watch);
//            result.add(UniqueIdentifier.of(masterToScheme.getValue(), doc.getName()));
//          } catch (DataNotFoundException ex) {
//            // ignore
//          }
//        }
//      }
//    }
    return result;
  }

}
