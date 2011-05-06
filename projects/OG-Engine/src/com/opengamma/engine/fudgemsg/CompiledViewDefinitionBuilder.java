/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;

import javax.time.Instant;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;

/**
 * Fudge message builder for {@link CompiledViewDefinition}
 */
@GenericFudgeBuilderFor(CompiledViewDefinition.class)
public class CompiledViewDefinitionBuilder implements FudgeBuilder<CompiledViewDefinition> {

  private static final String VIEW_DEFINITION_FIELD = "viewDefinition";
  private static final String PORTFOLIO_FIELD = "portfolio";
  private static final String COMPILED_CALCULATION_CONFIGURATIONS_FIELD = "compiledCalculationConfigurations";
  private static final String EARLIEST_VALIDITY_FIELD = "earliestValidity";
  private static final String LATEST_VALIDITY_FIELD = "latestValidity";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, CompiledViewDefinition object) {
    MutableFudgeMsg msg = context.newMessage();
    context.addToMessage(msg, VIEW_DEFINITION_FIELD, null, object.getViewDefinition());
    context.addToMessage(msg, PORTFOLIO_FIELD, null, object.getPortfolio());
    context.addToMessage(msg, COMPILED_CALCULATION_CONFIGURATIONS_FIELD, null, object.getCompiledCalculationConfigurations());
    context.addToMessage(msg, EARLIEST_VALIDITY_FIELD, null, object.getValidFrom());
    context.addToMessage(msg, LATEST_VALIDITY_FIELD, null, object.getValidTo());
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CompiledViewDefinition buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    ViewDefinition viewDefinition = context.fieldValueToObject(ViewDefinition.class, message.getByName(VIEW_DEFINITION_FIELD));
    Portfolio portfolio = context.fieldValueToObject(Portfolio.class, message.getByName(PORTFOLIO_FIELD));
    Collection<CompiledViewCalculationConfiguration> compiledCalculationConfigurations = context.fieldValueToObject(Collection.class, message.getByName(COMPILED_CALCULATION_CONFIGURATIONS_FIELD));
    FudgeField earliestValidityField = message.getByName(EARLIEST_VALIDITY_FIELD);
    Instant earliestValidity = earliestValidityField != null ? context.fieldValueToObject(Instant.class, earliestValidityField) : null;
    FudgeField latestValidityField = message.getByName(LATEST_VALIDITY_FIELD);
    Instant latestValidity = latestValidityField != null ? context.fieldValueToObject(Instant.class, latestValidityField) : null;
    return new CompiledViewDefinitionImpl(viewDefinition, portfolio, compiledCalculationConfigurations, earliestValidity, latestValidity);
  }

}
