/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.position;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.id.ExternalId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Creates a position from a security and quantity.
 */
public class PositionFunction extends AbstractFunctionInvoker implements PublishedFunction {

  // TODO: extend this to allow trades to be used

  /**
   * Default instance.
   */
  public static final PositionFunction INSTANCE = new PositionFunction();

  private final MetaFunction _meta;

  private static final int SECURITY = 0;
  private static final int QUANTITY = 1;

  private static List<MetaParameter> parameters() {
    final MetaParameter security = new MetaParameter("security", JavaTypeInfo.builder(ExternalId.class).get());
    // TODO: quantity should be BigDecimal and use type converter from double or integer
    final MetaParameter quantity = new MetaParameter("quantity", JavaTypeInfo.builder(Double.class).get());
    return Arrays.asList(security, quantity);
  }

  private PositionFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.POSITION, "Position", getParameters(), this));
  }

  protected PositionFunction() {
    this(new DefinitionAnnotater(PositionFunction.class));
  }

  public static Position invoke(final ExternalId security, final Double quantity) {
    return new SimplePosition(BigDecimal.valueOf(quantity), security);
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ExternalId) parameters[SECURITY], (Double) parameters[QUANTITY]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
