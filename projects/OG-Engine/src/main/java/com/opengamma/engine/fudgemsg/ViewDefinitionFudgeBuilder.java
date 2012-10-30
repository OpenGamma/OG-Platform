/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.function.resolver.ResolutionRuleTransform;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.DeltaDefinition;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Fudge message builder for {@link ViewDefinition} and {@link ViewCalculationConfiguration}. 
 */
@GenericFudgeBuilderFor(ViewDefinition.class)
public class ViewDefinitionFudgeBuilder implements FudgeBuilder<ViewDefinition> {

//  private static final String UNIQUE_ID_FIELD = "uniqueId";
  private static final String NAME_FIELD = "name";
  private static final String PORTFOLIO_ID_FIELD = "identifier";
  private static final String USER_FIELD = "user";
  private static final String MIN_DELTA_CALC_PERIOD_FIELD = "minDeltaCalcPeriod";
  private static final String MAX_DELTA_CALC_PERIOD_FIELD = "maxDeltaCalcPeriod";
  private static final String MIN_FULL_CALC_PERIOD_FIELD = "minFullCalcPeriod";
  private static final String MAX_FULL_CALC_PERIOD_FIELD = "maxFullCalcPeriod";
  private static final String RESULT_MODEL_DEFINITION_FIELD = "resultModelDefinition";
  private static final String CALCULATION_CONFIGURATION_FIELD = "calculationConfiguration";
  private static final String PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD = "portfolioRequirementsBySecurityType";
  private static final String SECURITY_TYPE_FIELD = "securityType";
  private static final String PORTFOLIO_REQUIREMENT_FIELD = "portfolioRequirement";
  private static final String PORTFOLIO_REQUIREMENT_REQUIRED_OUTPUT_FIELD = "requiredOutput";
  private static final String PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD = "constraints";
  
  private static final String SPECIFIC_REQUIREMENT_FIELD = "specificRequirement";
  private static final String DELTA_DEFINITION_FIELD = "deltaDefinition";
  private static final String CURRENCY_FIELD = "currency";
  private static final String DEFAULT_PROPERTIES_FIELD = "defaultProperties";
  private static final String RESOLUTION_RULE_TRANSFORM_FIELD = "resolutionRuleTransform";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ViewDefinition viewDefinition) {
    // REVIEW jonathan 2010-08-13 -- This is really messy, but we're fighting against two problems:
    // - ViewDefinitions are stored in Mongo, which means they need field names for absolutely everything
    // - There's a cycle of references between ViewDefinition and ViewCalculationConfiguration, so we have to handle
    // both at once.
    MutableFudgeMsg message = serializer.newMessage();
//    message.add(UNIQUE_ID_FIELD, null, viewDefinition.getUniqueId());
    message.add(NAME_FIELD, null, viewDefinition.getName());
    serializer.addToMessage(message, PORTFOLIO_ID_FIELD, null, viewDefinition.getPortfolioId());
    serializer.addToMessage(message, USER_FIELD, null, viewDefinition.getMarketDataUser());
    serializer.addToMessage(message, RESULT_MODEL_DEFINITION_FIELD, null, viewDefinition.getResultModelDefinition());

    Currency defaultCurrency = viewDefinition.getDefaultCurrency();
    if (defaultCurrency != null) {
      message.add(CURRENCY_FIELD, null, defaultCurrency.getCode());
    }

    if (viewDefinition.getMinDeltaCalculationPeriod() != null) {
      message.add(MIN_DELTA_CALC_PERIOD_FIELD, null, viewDefinition.getMinDeltaCalculationPeriod());
    }
    if (viewDefinition.getMaxDeltaCalculationPeriod() != null) {
      message.add(MAX_DELTA_CALC_PERIOD_FIELD, null, viewDefinition.getMaxDeltaCalculationPeriod());
    }
    if (viewDefinition.getMinFullCalculationPeriod() != null) {
      message.add(MIN_FULL_CALC_PERIOD_FIELD, null, viewDefinition.getMinFullCalculationPeriod());
    }
    if (viewDefinition.getMaxFullCalculationPeriod() != null) {
      message.add(MAX_FULL_CALC_PERIOD_FIELD, null, viewDefinition.getMaxFullCalculationPeriod());
    }
    Map<String, ViewCalculationConfiguration> calculationConfigurations = viewDefinition.getAllCalculationConfigurationsByName();
    for (ViewCalculationConfiguration calcConfig : calculationConfigurations.values()) {
      MutableFudgeMsg calcConfigMsg = serializer.newMessage();
      calcConfigMsg.add(NAME_FIELD, null, calcConfig.getName());
      // Can't use the default map serialisation here because every field needs to have a name for Mongo
      for (Map.Entry<String, Set<Pair<String, ValueProperties>>> securityTypeRequirements : calcConfig.getPortfolioRequirementsBySecurityType().entrySet()) {
        MutableFudgeMsg securityTypeRequirementsMsg = serializer.newMessage();
        securityTypeRequirementsMsg.add(SECURITY_TYPE_FIELD, securityTypeRequirements.getKey());
        for (Pair<String, ValueProperties> requirement : securityTypeRequirements.getValue()) {
          MutableFudgeMsg reqMsg = serializer.newMessage();
          reqMsg.add(PORTFOLIO_REQUIREMENT_REQUIRED_OUTPUT_FIELD, requirement.getFirst());
          serializer.addToMessage(reqMsg, PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD, null, requirement.getSecond());
          securityTypeRequirementsMsg.add(PORTFOLIO_REQUIREMENT_FIELD, reqMsg);
        }
        calcConfigMsg.add(PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD, securityTypeRequirementsMsg);
      }
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        calcConfigMsg.add(SPECIFIC_REQUIREMENT_FIELD, serializer.objectToFudgeMsg(specificRequirement));
      }
      serializer.addToMessage(calcConfigMsg, DELTA_DEFINITION_FIELD, null, calcConfig.getDeltaDefinition());
      serializer.addToMessage(calcConfigMsg, DEFAULT_PROPERTIES_FIELD, null, calcConfig.getDefaultProperties());
      serializer.addToMessage(calcConfigMsg, RESOLUTION_RULE_TRANSFORM_FIELD, null, calcConfig.getResolutionRuleTransform());
      message.add(CALCULATION_CONFIGURATION_FIELD, null, calcConfigMsg);
    }
    serializer.addToMessageWithClassHeaders(message, "uniqueId", null, viewDefinition.getUniqueId(), UniqueId.class);
    return message;
  }

  @Override
  public ViewDefinition buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    
    final FudgeField portfolioIdField = message.getByName(PORTFOLIO_ID_FIELD);
    final UniqueId portfolioId = portfolioIdField == null ? null : deserializer.fieldValueToObject(UniqueId.class, portfolioIdField);
    final String name = message.getFieldValue(String.class, message.getByName(NAME_FIELD));
    final UserPrincipal user = deserializer.fieldValueToObject(UserPrincipal.class, message.getByName(USER_FIELD));
    final ResultModelDefinition model = deserializer.fieldValueToObject(ResultModelDefinition.class, message.getByName(RESULT_MODEL_DEFINITION_FIELD)); 
    ViewDefinition viewDefinition = new ViewDefinition(
        name,
        portfolioId,
        user,
        model);

    // FudgeField currencyField = message.getByName(CURRENCY_FIELD);
    // if (currencyField != null) {
    // viewDefinition.setDefaultCurrency(context.fieldValueToObject(Currency.class, currencyField));
    // }

    if (message.hasField(CURRENCY_FIELD)) {
      String isoCode = message.getString(CURRENCY_FIELD);
      viewDefinition.setDefaultCurrency(Currency.of(isoCode));
    }

    if (message.hasField(MIN_DELTA_CALC_PERIOD_FIELD)) {
      viewDefinition.setMinDeltaCalculationPeriod(message.getLong(MIN_DELTA_CALC_PERIOD_FIELD));
    }
    if (message.hasField(MAX_DELTA_CALC_PERIOD_FIELD)) {
      viewDefinition.setMaxDeltaCalculationPeriod(message.getLong(MAX_DELTA_CALC_PERIOD_FIELD));
    }
    if (message.hasField(MIN_FULL_CALC_PERIOD_FIELD)) {
      viewDefinition.setMinFullCalculationPeriod(message.getLong(MIN_FULL_CALC_PERIOD_FIELD));
    }
    //for backward compatibility of renaming fullDeltaCalcPeriod to minFullCalcPeriod
    if (message.hasField("fullDeltaCalcPeriod")) {
      viewDefinition.setMinFullCalculationPeriod(message.getLong("fullDeltaCalcPeriod"));
    }
    if (message.hasField(MAX_FULL_CALC_PERIOD_FIELD)) {
      viewDefinition.setMaxFullCalculationPeriod(message.getLong(MAX_FULL_CALC_PERIOD_FIELD));
    }
    List<FudgeField> calcConfigs = message.getAllByName(CALCULATION_CONFIGURATION_FIELD);
    for (FudgeField calcConfigField : calcConfigs) {
      FudgeMsg calcConfigMsg = message.getFieldValue(FudgeMsg.class, calcConfigField);
      final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, message.getFieldValue(String.class, calcConfigMsg.getByName(NAME_FIELD)));
      for (FudgeField securityTypeRequirementsField : calcConfigMsg.getAllByName(PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD)) {
        FudgeMsg securityTypeRequirementsMsg = (FudgeMsg) securityTypeRequirementsField.getValue();
        String securityType = securityTypeRequirementsMsg.getString(SECURITY_TYPE_FIELD);
        Set<Pair<String, ValueProperties>> requirements = new HashSet<Pair<String, ValueProperties>>();
        for (FudgeField requirement : securityTypeRequirementsMsg.getAllByName(PORTFOLIO_REQUIREMENT_FIELD)) {
          FudgeMsg reqMsg = (FudgeMsg) requirement.getValue();
          String requiredOutput = reqMsg.getString(PORTFOLIO_REQUIREMENT_REQUIRED_OUTPUT_FIELD);
          ValueProperties constraints = (ValueProperties) deserializer.fieldValueToObject(ValueProperties.class, reqMsg.getByName(PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD));
          requirements.add(Pair.of(requiredOutput, constraints));
        }
        calcConfig.addPortfolioRequirements(securityType, requirements);
      }
      for (FudgeField specificRequirementField : calcConfigMsg.getAllByName(SPECIFIC_REQUIREMENT_FIELD)) {
        calcConfig.addSpecificRequirement(deserializer.fieldValueToObject(ValueRequirement.class, specificRequirementField));
      }
      if (calcConfigMsg.hasField(DELTA_DEFINITION_FIELD)) {
        calcConfig.setDeltaDefinition(deserializer.fieldValueToObject(DeltaDefinition.class, calcConfigMsg.getByName(DELTA_DEFINITION_FIELD)));
      }
      if (calcConfigMsg.hasField(DEFAULT_PROPERTIES_FIELD)) {
        calcConfig.setDefaultProperties(deserializer.fieldValueToObject(ValueProperties.class, calcConfigMsg.getByName(DEFAULT_PROPERTIES_FIELD)));
      }
      if (calcConfigMsg.hasField(RESOLUTION_RULE_TRANSFORM_FIELD)) {
        calcConfig.setResolutionRuleTransform(deserializer.fieldValueToObject(ResolutionRuleTransform.class, calcConfigMsg.getByName(RESOLUTION_RULE_TRANSFORM_FIELD)));
      }
      viewDefinition.addViewCalculationConfiguration(calcConfig);
    }
    FudgeField uniqueId = message.getByName("uniqueId");
    if (uniqueId != null) {
      viewDefinition.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
    }
    return viewDefinition;
  }

}
