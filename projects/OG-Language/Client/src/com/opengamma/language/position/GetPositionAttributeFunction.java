/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.position;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.position.Position;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Updates an attribute of a position
 */
public class GetPositionAttributeFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetPositionAttributeFunction INSTANCE = new GetPositionAttributeFunction();

  private final MetaFunction _meta;

  private static final int POSITION = 0;
  private static final int ATTRIBUTE = 1;

  private static List<MetaParameter> parameters() {
    final MetaParameter security = new MetaParameter("position", JavaTypeInfo.builder(Position.class).get());
    final MetaParameter attribute = new MetaParameter("attribute", JavaTypeInfo.builder(String.class).get());
    return Arrays.asList(security, attribute);
  }

  private GetPositionAttributeFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.POSITION, "GetPositionAttribute", getParameters(), this));
  }

  protected GetPositionAttributeFunction() {
    this(new DefinitionAnnotater(GetPositionAttributeFunction.class));
  }

  public static String invoke(final Position position, final String attribute) {
    return position.getAttributes().get(attribute);
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((Position) parameters[POSITION], (String) parameters[ATTRIBUTE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
