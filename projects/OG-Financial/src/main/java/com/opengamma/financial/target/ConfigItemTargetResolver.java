/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.target;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.target.resolver.AbstractSourceResolver;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A target resolver implementation to connect a config source to the engine framework.
 * 
 * @param <T> the type of the item encoded in configuration
 */
public class ConfigItemTargetResolver<T extends UniqueIdentifiable> extends AbstractSourceResolver<ConfigItem<?>, ConfigSource> {

  private static final ExternalScheme DEFAULT_SCHEME = ExternalScheme.of("ConfigSource");

  private final Class<T> _itemType;

  public ConfigItemTargetResolver(final Class<T> itemType, final ExternalScheme namingScheme, final ConfigSource source) {
    super(namingScheme, source);
    ArgumentChecker.notNull(itemType, "itemType");
    _itemType = itemType;
  }

  public ConfigItemTargetResolver(final Class<T> itemType, final ConfigSource source) {
    this(itemType, DEFAULT_SCHEME, source);
  }

  protected Class<T> getItemType() {
    return _itemType;
  }

  @Override
  protected UniqueIdentifiable lookupByName(final String name, final VersionCorrection versionCorrection) {
    return (T) getUnderlying().getSingle(getItemType(), name, versionCorrection);
  }

}
