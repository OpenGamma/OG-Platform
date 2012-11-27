/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import java.util.Arrays;
import java.util.List;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.id.UniqueId;
import com.opengamma.language.client.ContextRemoteClient;
import com.opengamma.language.client.MasterID;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;

/**
 * Writes a view definition to the view repository.
 */
public class StoreViewDefinitionProcedure extends AbstractProcedureInvoker.SingleResult implements PublishedProcedure {

  /**
   * Default instance.
   */
  public static final StoreViewDefinitionProcedure INSTANCE = new StoreViewDefinitionProcedure();

  private final MetaProcedure _meta;

  private static final int VIEW_DEFINITION = 0;
  private static final int IDENTIFIER = 1;
  private static final int MASTER = 2;

  private static List<MetaParameter> parameters() {
    final MetaParameter viewDefinition = new MetaParameter("viewDefinition", JavaTypeInfo.builder(ViewDefinition.class).get());
    final MetaParameter identifier = new MetaParameter("identifier", JavaTypeInfo.builder(UniqueId.class).allowNull().get());
    final MetaParameter target = new MetaParameter("master", JavaTypeInfo.builder(MasterID.class).defaultValue(MasterID.SESSION).get());
    return Arrays.asList(viewDefinition, identifier, target);
  }

  private StoreViewDefinitionProcedure(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaProcedure(Categories.VIEW, "StoreViewDefinition", getParameters(), this));
  }

  protected StoreViewDefinitionProcedure() {
    this(new DefinitionAnnotater(StoreViewDefinitionProcedure.class));
  }

  public static UniqueId invoke(final ConfigMaster configMaster, final ViewDefinition viewDefinition, final UniqueId identifier) {
    try {
      final ConfigItem<ViewDefinition> viewDefinitionConfigItem = ConfigItem.of(viewDefinition, viewDefinition.getName(), ViewDefinition.class);
      if (identifier != null) {
        viewDefinitionConfigItem.setUniqueId(identifier);
        final ConfigDocument document = configMaster.update(new ConfigDocument(viewDefinitionConfigItem));
        return document.getUniqueId();
      } else {
        final ConfigDocument document = configMaster.add(new ConfigDocument(viewDefinitionConfigItem));
        return document.getUniqueId();
      }
    } catch (final IllegalArgumentException e) {
      throw new InvokeInvalidArgumentException(VIEW_DEFINITION, e.getMessage());
    } catch (final DataNotFoundException e) {
      throw new InvokeInvalidArgumentException(IDENTIFIER, e.getMessage());
    }
  }

  public static UniqueId invoke(final SessionContext sessionContext, final ViewDefinition viewDefinition, final UniqueId identifier, final MasterID master) {
    final RemoteClient client = ContextRemoteClient.get(sessionContext, master);
    final ConfigMaster configMaster;
    try {
      configMaster = client.getConfigMaster();
    } catch (final UnsupportedOperationException e) {
      throw new InvokeInvalidArgumentException(MASTER, e);
    }
    return invoke(configMaster, viewDefinition, identifier);
  }

  // AbstractProcedureInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (ViewDefinition) parameters[VIEW_DEFINITION], (UniqueId) parameters[IDENTIFIER], (MasterID) parameters[MASTER]);
  }

  // PublishedProcedure

  @Override
  public MetaProcedure getMetaProcedure() {
    return _meta;
  }

}
