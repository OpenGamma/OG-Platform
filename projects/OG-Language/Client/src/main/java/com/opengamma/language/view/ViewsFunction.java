/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Returns a list of matching views, searched by name.
 */
public class ViewsFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ViewsFunction INSTANCE = new ViewsFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).allowNull().get());
    return Arrays.asList(name);
  }

  private ViewsFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "Views", getParameters(), this));
  }

  protected ViewsFunction() {
    this(new DefinitionAnnotater(ViewsFunction.class));
  }

  public static Map<UniqueId, String> invoke(final ConfigSource configSource, final String viewName) {
    final Map<UniqueId, String> entries = newHashMap();
    if (viewName == null) {
      final Collection<ConfigItem<ViewDefinition>> entries2 = configSource.getAll(ViewDefinition.class, VersionCorrection.LATEST);
      for (final ConfigItem<ViewDefinition> viewDefinitionConfigItem : entries2) {
        entries.put(viewDefinitionConfigItem.getUniqueId(), viewDefinitionConfigItem.getName());
      }
    } else {
      // TODO: the "viewName" could take wild-cards
      final Collection<ConfigItem<ViewDefinition>> viewDefinitionItems = configSource.get(ViewDefinition.class, viewName, VersionCorrection.LATEST);
      for (final ConfigItem<ViewDefinition> viewDefinitionItem : viewDefinitionItems) {
        entries.putAll(Collections.singletonMap(viewDefinitionItem.getUniqueId(), viewDefinitionItem.getName()));
      }
    }
    return entries;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ConfigSource configSource = sessionContext.getGlobalContext().getViewProcessor().getConfigSource();
    final String viewName = (String) parameters[0];
    return invoke(configSource, viewName);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
