/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Updates a view client descriptor to include a different execution flag.
 */
public class SetViewClientExecutionFlagFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetViewClientExecutionFlagFunction INSTANCE = new SetViewClientExecutionFlagFunction();

  private final MetaFunction _meta;

  private static final int VIEW_CLIENT_DESCRIPTOR = 0;
  private static final int FLAG = 1;
  private static final int INCLUDE = 2;

  // TODO: allow the "flag" parameter to take a set of flags (e.g. a vector from R or a range from Excel)

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("viewClient", JavaTypeInfo.builder(ViewClientDescriptor.class).get()),
        new MetaParameter("flag", JavaTypeInfo.builder(ViewExecutionFlags.class).get()),
        new MetaParameter("include", JavaTypeInfo.builder(Boolean.class).defaultValue(Boolean.TRUE).get()));
  }

  private SetViewClientExecutionFlagFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "SetViewClientExecutionFlag", getParameters(), this));
  }

  protected SetViewClientExecutionFlagFunction() {
    this(new DefinitionAnnotater(SetViewClientExecutionFlagFunction.class));
  }

  public static ViewClientDescriptor invoke(final ViewClientDescriptor viewClient, final ViewExecutionFlags flag, final boolean include) {
    final ViewExecutionOptions options = viewClient.getExecutionOptions();
    final EnumSet<ViewExecutionFlags> flags = options.getFlags();
    if (include) {
      flags.add(flag);
    } else {
      flags.remove(flag);
    }
    return new ViewClientDescriptor(viewClient.getViewId(), new ExecutionOptions(options.getExecutionSequence(), flags, options.getMaxSuccessiveDeltaCycles(),
        options.getDefaultExecutionOptions()));
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ViewClientDescriptor) parameters[VIEW_CLIENT_DESCRIPTOR], (ViewExecutionFlags) parameters[FLAG], (Boolean) parameters[INCLUDE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
