/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

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
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.resolver.ResolutionRuleTransform;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.DeltaDefinition;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutput;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutputAggregationType;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

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
  private static final String PERSISTENT_FIELD = "persistent";
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
  private static final String SCENARIO_ID_FIELD = "scenarioId";
  private static final String SCENARIO_PARAMETERS_ID_FIELD = "scenarioParametersId";
  
  private static final String MERGED_OUTPUTS_FIELD = "mergedOutputs";
  private static final String MERGED_OUTPUT_FIELD = "mergedOutput";
  private static final String MERGED_OUTPUT_AGGREGATION_TYPE_FIELD = "aggregationType";

  // field names for column data
  private static final String COLUMNS_FIELD = "columns";
  private static final String HEADER_FIELD = "header";
  private static final String VALUE_NAME_FIELD = "valueName";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ViewDefinition viewDefinition) {
    // REVIEW jonathan 2010-08-13 -- This is really messy, but there's a cycle of references between ViewDefinition and
    // ViewCalculationConfiguration, so we have to handle both at once.
    MutableFudgeMsg message = serializer.newMessage();

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
    
    if (viewDefinition.isPersistent()) {
      message.add(PERSISTENT_FIELD, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
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
      UniqueId scenarioId = calcConfig.getScenarioId();
      if (scenarioId != null) {
        serializer.addToMessageWithClassHeaders(calcConfigMsg, SCENARIO_ID_FIELD, null, scenarioId, UniqueId.class);
      }
      UniqueId scenarioParametersId = calcConfig.getScenarioParametersId();
      if (scenarioParametersId != null) {
        serializer.addToMessageWithClassHeaders(calcConfigMsg, SCENARIO_PARAMETERS_ID_FIELD, null, scenarioParametersId, UniqueId.class);
      }
      if (!calcConfig.getMergedOutputs().isEmpty()) {
        MutableFudgeMsg mergedOutputsMsg = serializer.newMessage();
        for (ViewCalculationConfiguration.MergedOutput mergedOutput : calcConfig.getMergedOutputs()) {
          MutableFudgeMsg mergedOutputMsg = serializer.newMessage();
          serializer.addToMessage(mergedOutputMsg, NAME_FIELD, null, mergedOutput.getMergedOutputName());
          mergedOutputMsg.add(MERGED_OUTPUT_AGGREGATION_TYPE_FIELD, mergedOutput.getAggregationType().toString());
          for (Pair<String, ValueProperties> portfolioRequirement : mergedOutput.getPortfolioRequirements()) {
            MutableFudgeMsg reqMsg = serializer.newMessage();
            serializer.addToMessage(reqMsg, PORTFOLIO_REQUIREMENT_REQUIRED_OUTPUT_FIELD, null, portfolioRequirement.getFirst());
            serializer.addToMessage(reqMsg, PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD, null, portfolioRequirement.getSecond());
            serializer.addToMessage(mergedOutputMsg, PORTFOLIO_REQUIREMENT_FIELD, null, reqMsg);
          }
          serializer.addToMessage(mergedOutputsMsg, MERGED_OUTPUT_FIELD, null, mergedOutputMsg);
        }
        serializer.addToMessage(calcConfigMsg, MERGED_OUTPUTS_FIELD, null, mergedOutputsMsg);
      }
      MutableFudgeMsg columnsMsg = serializer.newMessage();
      for (ViewCalculationConfiguration.Column column : calcConfig.getColumns()) {
        MutableFudgeMsg columnMsg = serializer.newMessage();
        serializer.addToMessage(columnMsg, HEADER_FIELD, null, column.getHeader());
        serializer.addToMessage(columnMsg, VALUE_NAME_FIELD, null, column.getValueName());
        serializer.addToMessage(columnMsg, PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD, null, column.getProperties());
        serializer.addToMessage(columnsMsg, null, null, columnMsg);
      }
      serializer.addToMessage(calcConfigMsg, COLUMNS_FIELD, null, columnsMsg);
      message.add(CALCULATION_CONFIGURATION_FIELD, null, calcConfigMsg);
    }
    serializer.addToMessageWithClassHeaders(message, "uniqueId", null, viewDefinition.getUniqueId(), UniqueId.class);
    return message;
  }

  /**
   * Support translation from pre-2286 type specifications, such as CTSpec[PRIMITIVE, CurrencyISO~USD], to resolvable types.
   * 
   * @param valueRequirement the possibly legacy specification, not null
   * @return the updated specification, not null
   * @deprecated shouldn't be relying on this, a configuration database upgrade script to apply the transformation would be better
   */
  @Deprecated
  private ValueRequirement plat2286Translate(final ValueRequirement valueRequirement) {
    if (valueRequirement.getTargetReference() instanceof ComputationTargetSpecification) {
      final ComputationTargetSpecification targetSpec = valueRequirement.getTargetReference().getSpecification();
      final ComputationTargetType type;
      if (targetSpec.getUniqueId() == null) {
        return valueRequirement;
      }
      if (Currency.OBJECT_SCHEME.equals(targetSpec.getUniqueId().getScheme())) {
        type = ComputationTargetType.CURRENCY;
      } else if (UnorderedCurrencyPair.OBJECT_SCHEME.equals(targetSpec.getUniqueId().getScheme())) {
        type = ComputationTargetType.UNORDERED_CURRENCY_PAIR;
      } else {
        return valueRequirement;
      }
      return new ValueRequirement(valueRequirement.getValueName(), new ComputationTargetSpecification(type, targetSpec.getUniqueId()), valueRequirement.getConstraints());
    } else {
      return valueRequirement;
    }
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
    
    if (message.hasField(PERSISTENT_FIELD)) {
      viewDefinition.setPersistent(true);
    }
    
    List<FudgeField> calcConfigs = message.getAllByName(CALCULATION_CONFIGURATION_FIELD);
    for (FudgeField calcConfigField : calcConfigs) {
      FudgeMsg calcConfigMsg = message.getFieldValue(FudgeMsg.class, calcConfigField);
      final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, message.getFieldValue(String.class, calcConfigMsg.getByName(NAME_FIELD)));
      for (FudgeField securityTypeRequirementsField : calcConfigMsg.getAllByName(PORTFOLIO_REQUIREMENTS_BY_SECURITY_TYPE_FIELD)) {
        FudgeMsg securityTypeRequirementsMsg = (FudgeMsg) securityTypeRequirementsField.getValue();
        String securityType = securityTypeRequirementsMsg.getString(SECURITY_TYPE_FIELD);
        Set<Pair<String, ValueProperties>> requirements = Sets.newLinkedHashSet();
        for (FudgeField requirement : securityTypeRequirementsMsg.getAllByName(PORTFOLIO_REQUIREMENT_FIELD)) {
          FudgeMsg reqMsg = (FudgeMsg) requirement.getValue();
          String requiredOutput = reqMsg.getString(PORTFOLIO_REQUIREMENT_REQUIRED_OUTPUT_FIELD);
          ValueProperties constraints = deserializer.fieldValueToObject(ValueProperties.class, reqMsg.getByName(PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD));
          requirements.add(Pairs.of(requiredOutput, constraints));
        }
        calcConfig.addPortfolioRequirements(securityType, requirements);
      }
      for (FudgeField specificRequirementField : calcConfigMsg.getAllByName(SPECIFIC_REQUIREMENT_FIELD)) {
        calcConfig.addSpecificRequirement(plat2286Translate(deserializer.fieldValueToObject(ValueRequirement.class, specificRequirementField)));
      }
      if (calcConfigMsg.hasField(DELTA_DEFINITION_FIELD)) {
        calcConfig.setDeltaDefinition(deserializer.fieldValueToObject(DeltaDefinition.class, calcConfigMsg.getByName(DELTA_DEFINITION_FIELD)));
      }
      if (calcConfigMsg.hasField(DEFAULT_PROPERTIES_FIELD)) {
        calcConfig.setDefaultProperties(deserializer.fieldValueToObject(ValueProperties.class,
                                                                        calcConfigMsg.getByName(DEFAULT_PROPERTIES_FIELD)));
      }
      if (calcConfigMsg.hasField(RESOLUTION_RULE_TRANSFORM_FIELD)) {
        calcConfig.setResolutionRuleTransform(deserializer.fieldValueToObject(ResolutionRuleTransform.class,
                                                                              calcConfigMsg.getByName(
                                                                                  RESOLUTION_RULE_TRANSFORM_FIELD)));
      }
      if (calcConfigMsg.hasField(SCENARIO_ID_FIELD)) {
        calcConfig.setScenarioId(deserializer.fieldValueToObject(UniqueId.class,
                                                                 calcConfigMsg.getByName(SCENARIO_ID_FIELD)));
      }
      if (calcConfigMsg.hasField(SCENARIO_PARAMETERS_ID_FIELD)) {
        calcConfig.setScenarioParametersId(deserializer.fieldValueToObject(UniqueId.class,
                                                                 calcConfigMsg.getByName(SCENARIO_PARAMETERS_ID_FIELD)));
      }
      List<ViewCalculationConfiguration.Column> columns = Lists.newArrayList();
      if (calcConfigMsg.hasField(MERGED_OUTPUTS_FIELD)) {
        FudgeMsg mergedOutputsMsg = calcConfigMsg.getMessage(MERGED_OUTPUTS_FIELD);
        for (FudgeField mergedOutputField : mergedOutputsMsg.getAllByName(MERGED_OUTPUT_FIELD)) {
          FudgeMsg mergedOutputMsg = (FudgeMsg) mergedOutputField.getValue();
          String mergedOutputName = mergedOutputMsg.getString(NAME_FIELD);
          MergedOutputAggregationType aggregationType = MergedOutputAggregationType.valueOf(mergedOutputMsg.getString(MERGED_OUTPUT_AGGREGATION_TYPE_FIELD));
          MergedOutput mergedOutput = new MergedOutput(mergedOutputName, aggregationType);
          for (FudgeField reqField : mergedOutputMsg.getAllByName(PORTFOLIO_REQUIREMENT_FIELD)) {
            FudgeMsg reqMsg = (FudgeMsg) reqField.getValue();
            String valueName = reqMsg.getString(PORTFOLIO_REQUIREMENT_REQUIRED_OUTPUT_FIELD);
            ValueProperties constraints = deserializer.fieldValueToObject(ValueProperties.class, reqMsg.getByName(PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD));
            mergedOutput.addMergedRequirement(valueName, constraints);
          }
          calcConfig.addMergedOutput(mergedOutput);
        }
      }
      if (calcConfigMsg.hasField(COLUMNS_FIELD)) {
        FudgeMsg columnsMsg = calcConfigMsg.getMessage(COLUMNS_FIELD);
        for (FudgeField field : columnsMsg.getAllFields()) {
          FudgeMsg columnMsg = (FudgeMsg) field.getValue();
          String header = deserializer.fieldValueToObject(String.class, columnMsg.getByName(HEADER_FIELD));
          String valueName = deserializer.fieldValueToObject(String.class, columnMsg.getByName(VALUE_NAME_FIELD));
          ValueProperties properties = deserializer.fieldValueToObject(ValueProperties.class, columnMsg.getByName(PORTFOLIO_REQUIREMENT_CONSTRAINTS_FIELD));
          columns.add(new ViewCalculationConfiguration.Column(header, valueName, properties));
        }
      }
      calcConfig.setColumns(columns);
      viewDefinition.addViewCalculationConfiguration(calcConfig);
    }
    FudgeField uniqueId = message.getByName("uniqueId");
    if (uniqueId != null) {
      viewDefinition.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
    }
    return viewDefinition;
  }

}
