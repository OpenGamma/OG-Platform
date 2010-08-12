/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.server;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.DeltaDefinition;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * Fudge message builder for {@link ViewDefinition} and {@link ViewCalculationConfiguration}. 
 */
public class ViewDefinitionBuilder implements FudgeBuilder<ViewDefinition> {

  private static final String NAME_FIELD = "name";
  private static final String IDENTIFIER_FIELD = "identifier";
  private static final String USER_FIELD = "user";
  private static final String DELTA_RECALC_PERIOD_FIELD = "deltaRecalcPeriod";
  private static final String FULL_RECALC_PERIOD_FIELD = "fullRecalcPeriod";
  private static final String CALCULATION_CONFIGURATION_FIELD = "calculationConfiguration";
  private static final String PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD = "portfolioRequirementsBySecurityType";
  private static final String SPECIFIC_REQUIREMENTS_FIELD = "specificRequirements";
  private static final String DISABLE_POSITION_OUTPUTS_FIELD = "disablePositionOutputs";
  private static final String DISABLE_AGGREGATE_POSITION_OUTPUTS_FIELD = "disableAggregatePositionOutputs";
  private static final String DELTA_DEFINITION_FIELD = "deltaDefinition";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ViewDefinition viewDefinition) {
    MutableFudgeFieldContainer message = context.newMessage();
    message.add(NAME_FIELD, null, viewDefinition.getName());
    context.objectToFudgeMsg(message, IDENTIFIER_FIELD, null, viewDefinition.getPortfolioId());
    context.objectToFudgeMsg(message, USER_FIELD, null, viewDefinition.getLiveDataUser());
    if (viewDefinition.getDeltaRecalculationPeriod() != null) {
      message.add(DELTA_RECALC_PERIOD_FIELD, null, viewDefinition.getDeltaRecalculationPeriod());
    }
    if (viewDefinition.getFullRecalculationPeriod() != null) {
      message.add(FULL_RECALC_PERIOD_FIELD, null, viewDefinition.getFullRecalculationPeriod());
    }
    Map<String, ViewCalculationConfiguration> calculationConfigurations = viewDefinition.getAllCalculationConfigurationsByName();
    for (ViewCalculationConfiguration calcConfig : calculationConfigurations.values()) {
      MutableFudgeFieldContainer calcConfigMsg = context.newMessage();
      calcConfigMsg.add(NAME_FIELD, null, calcConfig.getName());
      context.objectToFudgeMsg(calcConfigMsg, PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD, null, calcConfig.getPortfolioRequirementsBySecurityType());
      calcConfigMsg.add(DISABLE_POSITION_OUTPUTS_FIELD, null, calcConfig.isPositionOutputsDisabled());
      calcConfigMsg.add(DISABLE_AGGREGATE_POSITION_OUTPUTS_FIELD, null, calcConfig.isAggregatePositionOutputsDisabled());
      context.objectToFudgeMsg(calcConfigMsg, SPECIFIC_REQUIREMENTS_FIELD, null, calcConfig.getSpecificRequirements());
      context.objectToFudgeMsg(calcConfigMsg, DELTA_DEFINITION_FIELD, null, calcConfig.getDeltaDefinition());
      message.add(CALCULATION_CONFIGURATION_FIELD, null, calcConfigMsg);
    }
    return message;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ViewDefinition buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    ViewDefinition viewDefinition = new ViewDefinition(
        message.getFieldValue(String.class, message.getByName(NAME_FIELD)),
        context.fieldValueToObject(UniqueIdentifier.class, message.getByName(IDENTIFIER_FIELD)),
        context.fieldValueToObject(UserPrincipal.class, message.getByName(USER_FIELD)));
    if (message.hasField(DELTA_RECALC_PERIOD_FIELD)) {
      viewDefinition.setDeltaRecalculationPeriod(message.getLong(DELTA_RECALC_PERIOD_FIELD));
    }
    if (message.hasField(FULL_RECALC_PERIOD_FIELD)) {
      viewDefinition.setFullRecalculationPeriod(message.getLong(FULL_RECALC_PERIOD_FIELD));
    }
    List<FudgeField> calcConfigs = message.getAllByName(CALCULATION_CONFIGURATION_FIELD);
    for (FudgeField calcConfigField : calcConfigs) {
      FudgeFieldContainer calcConfigMsg = message.getFieldValue(FudgeFieldContainer.class, calcConfigField);
      ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, message.getFieldValue(String.class, calcConfigMsg.getByName(NAME_FIELD)));
      FudgeField portfolioOutputsField = calcConfigMsg.getByName(PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD);
      if (portfolioOutputsField != null) {
        Map<String, Set<String>> data = context.fieldValueToObject(Map.class, portfolioOutputsField);
        for (Map.Entry<String, Set<String>> d : data.entrySet()) {
          calcConfig.addPortfolioRequirements(d.getKey(), d.getValue());
        }
      }
      calcConfig.setPositionOutputsDisabled(calcConfigMsg.getBoolean(DISABLE_POSITION_OUTPUTS_FIELD));
      calcConfig.setAggregatePositionOutputsDisabled(calcConfigMsg.getBoolean(DISABLE_AGGREGATE_POSITION_OUTPUTS_FIELD));
      FudgeField specificOutputsField = calcConfigMsg.getByName(SPECIFIC_REQUIREMENTS_FIELD);
      if (specificOutputsField != null) {
        calcConfig.addSpecificRequirements(context.fieldValueToObject(Set.class, specificOutputsField));
      }
      calcConfig.setDeltaDefinition(context.fieldValueToObject(DeltaDefinition.class, calcConfigMsg.getByName(DELTA_DEFINITION_FIELD)));
      viewDefinition.addViewCalculationConfiguration(calcConfig);
    }
    return viewDefinition;
  }

}
