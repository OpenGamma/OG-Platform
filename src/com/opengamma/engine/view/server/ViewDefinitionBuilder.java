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

import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * Fudge message builder for {@code ViewDefinition}.
 */
public class ViewDefinitionBuilder implements FudgeBuilder<ViewDefinition> {

  private static final String FIELD_NAME = "name";
  private static final String FIELD_IDENTIFIER = "identifier";
  private static final String FIELD_USER = "user";
  private static final String FIELD_DELTA_RECALC_PERIOD = "deltaRecalcPeriod";
  private static final String FIELD_FULL_RECALC_PERIOD = "fullRecalcPeriod";
  private static final String FIELD_CALCULATIONCONFIGURATION = "calculationConfiguration";
  private static final String FIELD_VALUEREQUIREMENTS = "valueRequirements";
  private static final String FIELD_COMPUTE_PORTFOLIO_NODE_CALCULATIONS = "computePortfolioNodeCalculations";
  private static final String FIELD_COMPUTE_POSITION_NODE_CALCULATIONS = "computePositionNodeCalculations";
  private static final String FIELD_COMPUTE_SECURITY_NODE_CALCULATIONS = "computeSecurityNodeCalculations";
  private static final String FIELD_COMPUTE_PRIMITIVE_NODE_CALCULATIONS = "computePrimitiveNodeCalculations";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ViewDefinition viewDefinition) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(FIELD_NAME, null, viewDefinition.getName());
    context.objectToFudgeMsg(message, FIELD_IDENTIFIER, null, viewDefinition.getPortfolioId());
    context.objectToFudgeMsg(message, FIELD_USER, null, viewDefinition.getLiveDataUser());
    if (viewDefinition.getDeltaRecalculationPeriod() != null) {
      message.add(FIELD_DELTA_RECALC_PERIOD, null, viewDefinition.getDeltaRecalculationPeriod());
    }
    if (viewDefinition.getFullRecalculationPeriod() != null) {
      message.add(FIELD_FULL_RECALC_PERIOD, null, viewDefinition.getFullRecalculationPeriod());
    }
    message.add(FIELD_COMPUTE_PORTFOLIO_NODE_CALCULATIONS, null, viewDefinition.isComputePortfolioNodeCalculations());
    message.add(FIELD_COMPUTE_POSITION_NODE_CALCULATIONS, null, viewDefinition.isComputePositionNodeCalculations());
    message.add(FIELD_COMPUTE_SECURITY_NODE_CALCULATIONS, null, viewDefinition.isComputeSecurityNodeCalculations());
    message.add(FIELD_COMPUTE_PRIMITIVE_NODE_CALCULATIONS, null, viewDefinition.isComputePrimitiveNodeCalculations());
    Map<String, ViewCalculationConfiguration> calculationConfigurations = viewDefinition.getAllCalculationConfigurationsByName();
    for (ViewCalculationConfiguration calculationConfiguration : calculationConfigurations.values()) {
      final MutableFudgeFieldContainer config = context.newMessage();
      config.add(FIELD_NAME, null, calculationConfiguration.getName());
      context.objectToFudgeMsg(config, FIELD_VALUEREQUIREMENTS, null, calculationConfiguration.getValueRequirementsBySecurityTypes());
      message.add(FIELD_CALCULATIONCONFIGURATION, null, config);
    }
    return message;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ViewDefinition buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    final ViewDefinition viewDefinition = new ViewDefinition(
        message.getFieldValue(String.class, message.getByName(FIELD_NAME)),
        context.fieldValueToObject(UniqueIdentifier.class, message.getByName(FIELD_IDENTIFIER)),
        context.fieldValueToObject(UserPrincipal.class, message.getByName(FIELD_USER)));
    if (message.hasField(FIELD_DELTA_RECALC_PERIOD)) {
      viewDefinition.setDeltaRecalculationPeriod(message.getLong(FIELD_DELTA_RECALC_PERIOD));
    }
    if (message.hasField(FIELD_FULL_RECALC_PERIOD)) {
      viewDefinition.setFullRecalculationPeriod(message.getLong(FIELD_FULL_RECALC_PERIOD));
    }
    viewDefinition.setComputePortfolioNodeCalculations(message.getBoolean(FIELD_COMPUTE_PORTFOLIO_NODE_CALCULATIONS));
    viewDefinition.setComputePositionNodeCalculations(message.getBoolean(FIELD_COMPUTE_POSITION_NODE_CALCULATIONS));
    viewDefinition.setComputeSecurityNodeCalculations(message.getBoolean(FIELD_COMPUTE_SECURITY_NODE_CALCULATIONS));
    viewDefinition.setComputePrimitiveNodeCalculations(message.getBoolean(FIELD_COMPUTE_PRIMITIVE_NODE_CALCULATIONS));
    final List<FudgeField> calcConfigs = message.getAllByName(FIELD_CALCULATIONCONFIGURATION);
    for (FudgeField calcConfigField : calcConfigs) {
      final FudgeFieldContainer calcConfig = message.getFieldValue(FudgeFieldContainer.class, calcConfigField);
      final ViewCalculationConfiguration viewCalculationConfiguration = new ViewCalculationConfiguration(viewDefinition, message.getFieldValue(String.class, calcConfig.getByName(FIELD_NAME)));
      final Map<String, Set<String>> data = context.fieldValueToObject(Map.class, calcConfig.getByName(FIELD_VALUEREQUIREMENTS));
      for (Map.Entry<String, Set<String>> d : data.entrySet()) {
        viewCalculationConfiguration.addValueRequirements(d.getKey(), d.getValue());
      }
      viewDefinition.addViewCalculationConfiguration(viewCalculationConfiguration);
    }
    return viewDefinition;
  }

}
