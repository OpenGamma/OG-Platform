/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.LinkedList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Lists;
import com.opengamma.engine.function.config.FunctionConfigurationDefinition;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;

/**
 * Fudge message builder for {@link FunctionConfigurationDefinition}. 
 */
@FudgeBuilderFor(FunctionConfigurationDefinition.class)
public class FunctionConfigurationDefinitionFudgeBuilder implements FudgeBuilder<FunctionConfigurationDefinition> {

  private static final String NAME_FIELD = "name";
  private static final String FUNCTION_CONFIG_DEFINITION_FIELD = "configName";
  private static final String STATIC_FUNCTION_FIELD = "staticFunction";
  private static final String PARAMETERIZED_FUNCTION_FIELD = "parameterizedFunction";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FunctionConfigurationDefinition functionConfig) {

    MutableFudgeMsg message = serializer.newMessage();

    message.add(NAME_FIELD, null, functionConfig.getName());
    
    List<String> configurationDefinitions = functionConfig.getFunctionConfigurationDefinitions();
    if (!configurationDefinitions.isEmpty()) {
      for (String functionConfigName : configurationDefinitions) {
        message.add(FUNCTION_CONFIG_DEFINITION_FIELD, null, functionConfigName);
      }
    }
    
    List<StaticFunctionConfiguration> staticFunctions = functionConfig.getStaticFunctions();
    if (!staticFunctions.isEmpty()) {
      for (StaticFunctionConfiguration staticFunctionConfiguration : staticFunctions) {
        message.add(STATIC_FUNCTION_FIELD, null, staticFunctionConfiguration.getDefinitionClassName());
      }
    }
    
    List<ParameterizedFunctionConfiguration> parameterizedFunctions = functionConfig.getParameterizedFunctions();
    if (!parameterizedFunctions.isEmpty()) {
      for (ParameterizedFunctionConfiguration configuration : parameterizedFunctions) {
        MutableFudgeMsg parametizedMsg = serializer.newMessage();
        parametizedMsg.add("func", null, configuration.getDefinitionClassName());
        for (String parameter : configuration.getParameter()) {
          parametizedMsg.add("param", null, parameter);
        }
        message.add(PARAMETERIZED_FUNCTION_FIELD, null, parametizedMsg);
      }
    }
    return message;
  }

  @Override
  public FunctionConfigurationDefinition buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    
    final String name = message.getString(NAME_FIELD);
    
    final List<String> functionConfigurationDefinitions = new LinkedList<String>();
    final List<StaticFunctionConfiguration> staticFunctions = new LinkedList<StaticFunctionConfiguration>();
    final List<ParameterizedFunctionConfiguration> parameterizedFunctions = new LinkedList<ParameterizedFunctionConfiguration>();
    
    if (message.hasField(FUNCTION_CONFIG_DEFINITION_FIELD)) {
      List<FudgeField> allConfigs = message.getAllByName(FUNCTION_CONFIG_DEFINITION_FIELD);
      for (FudgeField fudgeField : allConfigs) {
        functionConfigurationDefinitions.add((String) fudgeField.getValue());
      }
    }
    
    if (message.hasField(STATIC_FUNCTION_FIELD)) {
      List<FudgeField> allStaticFunctions = message.getAllByName(STATIC_FUNCTION_FIELD);
      for (FudgeField fudgeField : allStaticFunctions) {
        staticFunctions.add(new StaticFunctionConfiguration((String) fudgeField.getValue()));
      }
    }
    
    if (message.hasField(PARAMETERIZED_FUNCTION_FIELD)) {
      List<FudgeField> allConfigs = message.getAllByName(PARAMETERIZED_FUNCTION_FIELD);
      for (FudgeField configField : allConfigs) {
        FudgeMsg parameterizedMsg = (FudgeMsg) configField.getValue();
        String definitionClassName = parameterizedMsg.getString("func");
        List<FudgeField> parameterFields = parameterizedMsg.getAllByName("param");
        List<String> parameters = Lists.newArrayList();
        for (FudgeField parameterField : parameterFields) {
          parameters.add((String) parameterField.getValue());
        }
        parameterizedFunctions.add(new ParameterizedFunctionConfiguration(definitionClassName, parameters));
      }
    }
    
    return new FunctionConfigurationDefinition(name, functionConfigurationDefinitions, staticFunctions, parameterizedFunctions);
  }

}
