/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import java.util.Arrays;
import java.util.List;

import org.threeten.bp.Instant;

import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
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
 * Returns the set of value requirement names likely to be calculable from a portfolio.
 */
public class GetAvailableOutputsFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetAvailableOutputsFunction INSTANCE = new GetAvailableOutputsFunction();

  private final MetaFunction _meta;

  private static final int DEFAULT_NODE_SAMPLE = 2;
  private static final int DEFAULT_POSITION_SAMPLE = 2;

  // TODO: these defaults should be configurable from a properties file or something

  private static final int PORTFOLIO = 0;
  private static final int NODE_SAMPLE = 1;
  private static final int POSITION_SAMPLE = 2;
  private static final int EVALUATION_TIME = 3;

  private static List<MetaParameter> parameters() {
    final MetaParameter portfolio = new MetaParameter("portfolio", JavaTypeInfo.builder(UniqueId.class).get());
    final MetaParameter nodeSample = new MetaParameter("nodeSample", JavaTypeInfo.builder(Integer.class).defaultValue(DEFAULT_NODE_SAMPLE).get());
    final MetaParameter positionSample = new MetaParameter("positionSample", JavaTypeInfo.builder(Integer.class).defaultValue(DEFAULT_POSITION_SAMPLE).get());
    final MetaParameter evaluationTime = new MetaParameter("evaluationTime", JavaTypeInfo.builder(Instant.class).allowNull().get());
    return Arrays.asList(portfolio, nodeSample, positionSample, evaluationTime);
  }

  private GetAvailableOutputsFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VALUE, "GetAvailableOutputs", getParameters(), this));
  }

  public GetAvailableOutputsFunction() {
    this(new DefinitionAnnotater(GetAvailableOutputsFunction.class));
  }

  public static AvailableOutputs invoke(final AvailableOutputsProvider provider, final UniqueId portfolio, final Integer nodeSample, final Integer positionSample, final Instant evaluationTime) {
    return provider.getPortfolioOutputs(portfolio, evaluationTime, nodeSample, positionSample);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext.getGlobalContext().getAvailableOutputsProvider(), (UniqueId) parameters[PORTFOLIO], (Integer) parameters[NODE_SAMPLE], (Integer) parameters[POSITION_SAMPLE],
        (Instant) parameters[EVALUATION_TIME]);
  }

}
