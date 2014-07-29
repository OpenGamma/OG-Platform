/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.AbstractConfigChangeProvider;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClassUtils;

/**
 * A {@link WatchSetProvider} that translate a configuration type identifier to object identifiers for the type instances.
 * <p>
 * This can be used for re-initialization of functions that look up configuration items by name.
 * 
 * @deprecated Use a sub-class of {@link AbstractConfigChangeProvider} to notify the {@link ViewProcessorManager} of changes instead
 */
@Deprecated
public class ConfigDocumentWatchSetProvider implements WatchSetProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(ConfigDocumentWatchSetProvider.class);

  /**
   * The scheme used in object identifiers that this matches.
   */
  public static final String CONFIG_TYPE_SCHEME = AbstractConfigChangeProvider.CONFIG_TYPE_SCHEME;

  private final ConfigSource _configSource;

  public ConfigDocumentWatchSetProvider(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  public static void reinitOnChanges(final FunctionCompilationContext context, final FunctionDefinition function, final Class<?> type) {
    AbstractConfigChangeProvider.reinitOnChanges(context, function, type);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes" })
  public Set<ObjectId> getAdditionalWatchSet(final Set<ObjectId> watchSet) {
    Set<ObjectId> toWatch = new HashSet<ObjectId>();
    for (ObjectId watch : watchSet) {
      if (CONFIG_TYPE_SCHEME.equals(watch.getScheme())) {
        s_logger.info("Creating watch on {}", watch);
        final Class clazz;
        try {
          clazz = ClassUtils.loadClass(watch.getValue());
        } catch (ClassNotFoundException e) {
          s_logger.error("Can't create watch for {}", watch);
          s_logger.warn("Caught exception", e);
          continue;
        }
        final Collection items = getConfigSource().getAll(clazz, VersionCorrection.LATEST);
        if ((items != null) && !items.isEmpty()) {
          for (Object item0 : items) {
            final ConfigItem item = (ConfigItem) item0;
            final ObjectId oid = item.getObjectId();
            s_logger.debug("Watching {} for {}", oid, watch);
            toWatch.add(oid);
          }
        }
      }
    }
    return toWatch;
  }

}
