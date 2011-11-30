/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import java.util.Arrays;
import java.util.List;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Creates a view definition
 */
public class ViewDefinitionFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ViewDefinitionFunction INSTANCE = new ViewDefinitionFunction();

  private final MetaFunction _meta;

  private static final int NAME = 0;
  private static final int PORTFOLIO = 1;
  private static final int PORTFOLIO_REQUIREMENTS = 2;
  private static final int SPECIFIC_REQUIREMENTS = 3;

  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).get());
    final MetaParameter portfolio = new MetaParameter("portfolio", JavaTypeInfo.builder(UniqueId.class).get());
    final MetaParameter portfolioRequirements = new MetaParameter("portfolioRequirements", JavaTypeInfo.builder(String.class).get().arrayOfWithAllowNull(true));
    final MetaParameter specificRequirements = new MetaParameter("specificRequirements", JavaTypeInfo.builder(ValueRequirement.class).get().arrayOfWithAllowNull(true));
    return Arrays.asList(name, portfolio, portfolioRequirements, specificRequirements);
  }

  private ViewDefinitionFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "ViewDefinition", getParameters(), this));
  }

  protected ViewDefinitionFunction() {
    this(new DefinitionAnnotater(ViewDefinitionFunction.class));
  }

  public static Object invoke(final String viewName) {
    // TODO
    return null;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((String) parameters[NAME]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
