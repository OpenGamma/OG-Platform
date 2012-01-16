/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.core.value.MarketDataRequirementNamesHelper;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.Parameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Returns the set of all market data requirement names defined in the {@link MarketDataRequirementNames} class.
 */
public class MarketDataRequirementNamesFunction implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final MarketDataRequirementNamesFunction INSTANCE = new MarketDataRequirementNamesFunction();

  private static final Set<String> s_marketDataRequirementNames = MarketDataRequirementNamesHelper.constructValidRequirementNames();

  public static Set<String> getMarketDataRequirementNames() {
    return Collections.unmodifiableSet(s_marketDataRequirementNames);
  }

  @Override
  public MetaFunction getMetaFunction() {
    final MetaFunction meta = new MetaFunction(Categories.VALUE, "MarketDataRequirementNames", Collections.<Parameter>emptyList(), new AbstractFunctionInvoker(Collections.<MetaParameter>emptyList()) {
      @Override
      protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
        return getMarketDataRequirementNames();
      }
    });
    meta.setDescription("Returns the set of standard Market Data Requirement Names defined within the system. " +
        "Note that the Market Data Requirements available from the current live data provider may differ");
    return meta;
  }

}
