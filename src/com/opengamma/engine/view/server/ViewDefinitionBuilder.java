/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.server;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.DeltaDefinition;
import com.opengamma.engine.view.ResultModelDefinition;
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
  private static final String RESULT_MODEL_DEFINITION_FIELD = "resultModelDefinition";
  private static final String CALCULATION_CONFIGURATION_FIELD = "calculationConfiguration";
  private static final String PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD = "portfolioRequirementsBySecurityType";
  private static final String SECURITY_TYPE_FIELD = "securityType";
  private static final String PORTFOLIO_REQUIREMENT_FIELD = "portfolioRequirement";
  private static final String SPECIFIC_REQUIREMENT_FIELD = "specificRequirement";
  private static final String DELTA_DEFINITION_FIELD = "deltaDefinition";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ViewDefinition viewDefinition) {
    // REVIEW jonathan 2010-08-13 -- This is really messy, but we're fighting against two problems:
    //  - ViewDefinitions are stored in Mongo, which means they need field names for absolutely everything
    //  - There's a cycle of references between ViewDefinition and ViewCalculationConfiguration, so we have to handle
    //    both at once. 
    MutableFudgeFieldContainer message = context.newMessage();
    message.add(NAME_FIELD, null, viewDefinition.getName());
    context.objectToFudgeMsg(message, IDENTIFIER_FIELD, null, viewDefinition.getPortfolioId());
    context.objectToFudgeMsg(message, USER_FIELD, null, viewDefinition.getLiveDataUser());
    context.objectToFudgeMsg(message, RESULT_MODEL_DEFINITION_FIELD, null, viewDefinition.getResultModelDefinition());
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
      // Can't use the default map serialisation here because every field needs to have a name for Mongo
      for (Map.Entry<String, Set<String>> securityTypeRequirements : calcConfig.getPortfolioRequirementsBySecurityType().entrySet()) {
        MutableFudgeFieldContainer securityTypeRequirementsMsg = context.newMessage();
        securityTypeRequirementsMsg.add(SECURITY_TYPE_FIELD, securityTypeRequirements.getKey());
        for (String requirement : securityTypeRequirements.getValue()) {
          securityTypeRequirementsMsg.add(PORTFOLIO_REQUIREMENT_FIELD, requirement);
        }
        calcConfigMsg.add(PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD, securityTypeRequirementsMsg);
      }
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        calcConfigMsg.add(SPECIFIC_REQUIREMENT_FIELD, context.objectToFudgeMsg(specificRequirement));
      }
      context.objectToFudgeMsg(calcConfigMsg, DELTA_DEFINITION_FIELD, null, calcConfig.getDeltaDefinition());
      message.add(CALCULATION_CONFIGURATION_FIELD, null, calcConfigMsg);
    }
    return message;
  }

  @Override
  public ViewDefinition buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    ViewDefinition viewDefinition = new ViewDefinition(
        message.getFieldValue(String.class, message.getByName(NAME_FIELD)),
        context.fieldValueToObject(UniqueIdentifier.class, message.getByName(IDENTIFIER_FIELD)),
        context.fieldValueToObject(UserPrincipal.class, message.getByName(USER_FIELD)),
        context.fieldValueToObject(ResultModelDefinition.class, message.getByName(RESULT_MODEL_DEFINITION_FIELD)));
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
      for (FudgeField securityTypeRequirementsField : calcConfigMsg.getAllByName(PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD)) {
        FudgeFieldContainer securityTypeRequirementsMsg = (FudgeFieldContainer) securityTypeRequirementsField.getValue();
        String securityType = securityTypeRequirementsMsg.getString(SECURITY_TYPE_FIELD);
        Set<String> requirements = new HashSet<String>();
        for (FudgeField requirement : securityTypeRequirementsMsg.getAllByName(PORTFOLIO_REQUIREMENT_FIELD)) {
          requirements.add((String) requirement.getValue());
        }
        calcConfig.addPortfolioRequirements(securityType, requirements);
      }
      for (FudgeField specificRequirementField : calcConfigMsg.getAllByName(SPECIFIC_REQUIREMENT_FIELD)) {
        calcConfig.addSpecificRequirement(context.fieldValueToObject(ValueRequirement.class, specificRequirementField));
      }
      calcConfig.setDeltaDefinition(context.fieldValueToObject(DeltaDefinition.class, calcConfigMsg.getByName(DELTA_DEFINITION_FIELD)));
      viewDefinition.addViewCalculationConfiguration(calcConfig);
    }
    return viewDefinition;
  }

}
