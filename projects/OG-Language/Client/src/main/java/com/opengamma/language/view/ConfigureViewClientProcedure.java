/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.financial.marketdata.MarketDataAddOperation;
import com.opengamma.financial.marketdata.MarketDataMultiplyOperation;
import com.opengamma.language.config.ConfigurationDelta;
import com.opengamma.language.config.ConfigurationItem;
import com.opengamma.language.config.ConfigurationItemVisitor;
import com.opengamma.language.config.EnableCycleAccess;
import com.opengamma.language.config.MarketDataOverride;
import com.opengamma.language.config.ValueProperty;
import com.opengamma.language.config.ViewCalculationRate;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;

/**
 * Makes a configuration change to a view client.
 */
public class ConfigureViewClientProcedure extends AbstractProcedureInvoker.NoResult implements PublishedProcedure {

  private static final Logger s_logger = LoggerFactory.getLogger(ConfigureViewClientProcedure.class);

  /**
   * Default instance.
   */
  public static final ConfigureViewClientProcedure INSTANCE = new ConfigureViewClientProcedure();

  private final MetaProcedure _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter viewClient = new MetaParameter("viewClient", JavaTypeInfo.builder(ViewClientHandle.class).get());
    final MetaParameter configuration = new MetaParameter("configuration", JavaTypeInfo.builder(Set.class).allowNull().parameter(JavaTypeInfo.builder(ConfigurationItem.class).get()).get());
    return Arrays.asList(viewClient, configuration);
  }

  private ConfigureViewClientProcedure(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaProcedure(Categories.VIEW, "ConfigureViewClient", getParameters(), this));
  }

  protected ConfigureViewClientProcedure() {
    this(new DefinitionAnnotater(ConfigureViewClientProcedure.class));
  }

  private abstract static class ConfigurationVisitor implements ConfigurationItemVisitor<Boolean> {

    private final UserViewClient _viewClient;

    public ConfigurationVisitor(final UserViewClient viewClient) {
      _viewClient = viewClient;
    }

    protected UserViewClient getViewClient() {
      return _viewClient;
    }

    @Override
    public final Boolean visitValueProperty(final ValueProperty valueProperty) {
      s_logger.debug("Ignoring {}", valueProperty);
      return Boolean.FALSE;
    }

    @Override
    public final Boolean visitViewCalculationRate(final ViewCalculationRate viewCalculationRate) {
      s_logger.debug("Ignoring {}", viewCalculationRate);
      return Boolean.FALSE;
    }

  }

  private static final class AddConfiguration extends ConfigurationVisitor {

    public AddConfiguration(final UserViewClient viewClient) {
      super(viewClient);
    }

    @Override
    public Boolean visitEnableCycleAccess(final EnableCycleAccess enableCycleAccess) {
      s_logger.debug("Applying {}", enableCycleAccess);
      getViewClient().getViewClient().setViewCycleAccessSupported(true);
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitMarketDataOverride(final MarketDataOverride marketDataOverride) {
      s_logger.debug("Applying {}", marketDataOverride);
      final MarketDataInjector injector = getViewClient().getViewClient().getLiveDataOverrideInjector();
      Object value = marketDataOverride.getValue();
      if (marketDataOverride.getOperation() != null) {
        switch (marketDataOverride.getOperation()) {
          case ADD:
            if (value instanceof Number) {
              value = new MarketDataAddOperation(((Number) value).doubleValue());
            } else {
              throw new InvokeInvalidArgumentException("Invalid ADD override - " + marketDataOverride.toString());
            }
            break;
          case MULTIPLY:
            if (value instanceof Number) {
              value = new MarketDataMultiplyOperation(((Number) value).doubleValue());
            } else {
              throw new InvokeInvalidArgumentException("Invalid MULTIPLY override - " + marketDataOverride.toString());
            }
            break;
          default:
            throw new UnsupportedOperationException(marketDataOverride.getOperation().toString());
        }
      }
      injector.addValue(marketDataOverride.getValueRequirement(), value);
      return Boolean.TRUE;
    }

  }

  private static final class RemoveConfiguration extends ConfigurationVisitor {

    public RemoveConfiguration(final UserViewClient viewClient) {
      super(viewClient);
    }

    @Override
    public Boolean visitEnableCycleAccess(final EnableCycleAccess enableCycleAccess) {
      s_logger.debug("Removing {}", enableCycleAccess);
      getViewClient().getViewClient().setViewCycleAccessSupported(false);
      return Boolean.TRUE;
    }

    @Override
    public Boolean visitMarketDataOverride(final MarketDataOverride marketDataOverride) {
      s_logger.debug("Removing {}", marketDataOverride);
      final MarketDataInjector injector = getViewClient().getViewClient().getLiveDataOverrideInjector();
      injector.removeValue(marketDataOverride.getValueRequirement());
      return Boolean.TRUE;
    }

  }

  public static void invoke(final UserViewClient viewClient, final Set<ConfigurationItem> configuration) {
    final Set<ConfigurationItem> previousConfiguration = viewClient.getAndSetConfiguration(configuration);
    final ConfigurationDelta delta = ConfigurationDelta.of(previousConfiguration, configuration);
    int itemsAdded = 0;
    int itemsRemoved = 0;
    if (delta.hasChanged()) {
      //NOTE: order is important
      final RemoveConfiguration removeVisitor = new RemoveConfiguration(viewClient);
      for (final ConfigurationItem item : delta.getRemoved()) {
        if (item.accept(removeVisitor) == Boolean.TRUE) {
          itemsRemoved++;
        }
      }

      final AddConfiguration addVisitor = new AddConfiguration(viewClient);
      for (final ConfigurationItem item : delta.getAdded()) {
        if (item.accept(addVisitor) == Boolean.TRUE) {
          itemsAdded++;
        }
      }
    }
    s_logger.info("{} items added, {} items removed from view client configuration", itemsAdded, itemsRemoved);
  }

  // AbstractProcedureInvoker.NoResult

  @SuppressWarnings("unchecked")
  @Override
  protected void invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ViewClientHandle viewClient = (ViewClientHandle) parameters[0];
    final Set<ConfigurationItem> configuration = (Set<ConfigurationItem>) parameters[1];
    invoke(viewClient.get(), configuration);
    viewClient.unlock();
  }

  // PublishedProcedure

  @Override
  public MetaProcedure getMetaProcedure() {
    return _meta;
  }

}
