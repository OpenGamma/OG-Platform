/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.position;

import java.util.Arrays;
import java.util.List;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Position;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Function to retrieve a security from a position
 */
public class GetPositionSecurityFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance
   */
  public static final GetPositionSecurityFunction INSTANCE = new GetPositionSecurityFunction();

  private final MetaFunction _meta;

  private static final int POSITION = 0;

  private static List<MetaParameter> parameters() {
    final MetaParameter identifier = new MetaParameter("position", JavaTypeInfo.builder(Position.class).get());
    return Arrays.asList(identifier);
  }

  private GetPositionSecurityFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.POSITION, "GetPositionSecurity", getParameters(), this));
  }

  protected GetPositionSecurityFunction() {
    this(new DefinitionAnnotater(GetPositionSecurityFunction.class));
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final Position position = (Position) parameters[POSITION];
    try {
      return position.getSecurityLink().resolve(sessionContext.getGlobalContext().getSecuritySource());
    } catch (DataNotFoundException e) {
      throw new InvokeInvalidArgumentException(POSITION, "Security identifier not found");
    }
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
