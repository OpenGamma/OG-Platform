/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.language.async.AsynchronousExecution;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Extracts a primitive value from a view cycle result.
 */
public class ViewPrimitiveCycleValueFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ViewPrimitiveCycleValueFunction INSTANCE = new ViewPrimitiveCycleValueFunction();
  
  private final MetaFunction _meta;
  
  private static List<MetaParameter> parameters() {
    final MetaParameter resultModel = new MetaParameter("resultModel", JavaTypeInfo.builder(ViewComputationResultModel.class).get());
    final MetaParameter targetId = new MetaParameter("targetId", JavaTypeInfo.builder(UniqueId.class).get());
    final MetaParameter valueRequirement = new MetaParameter("valueRequirement", JavaTypeInfo.builder(String.class).get());
    final MetaParameter notAvailableValue = new MetaParameter("notAvailable_value", JavaTypeInfo.builder(String.class).allowNull().get());
    final MetaParameter flattenValue = new MetaParameter("flattenValue", JavaTypeInfo.builder(Boolean.class).allowNull().get());
    return Arrays.asList(resultModel, targetId, valueRequirement, notAvailableValue, flattenValue);
  }
  
  private ViewPrimitiveCycleValueFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "ViewPrimitiveCycleValue", getParameters(), this));
  }
  
  protected ViewPrimitiveCycleValueFunction() {
    this(new DefinitionAnnotater(ViewPrimitiveCycleValueFunction.class));
  }
  
  public static Object invoke(ViewComputationResultModel resultModel, String calcConfigName, ValueRequirement valueRequirement, String notAvailableValue, boolean flattenValue) {
    ValueSpecification valueSpec = findValueSpecification(resultModel, valueRequirement);
    if (valueSpec == null) {
      // TODO should return #NA if notAvailableValue is null
      return notAvailableValue;
    }
    ViewCalculationResultModel calcResultModel = resultModel.getCalculationResult(calcConfigName);
    if (calcResultModel == null) {
      // TODO should return #NA if notAvailableValue is null 
      return notAvailableValue;
    }
    Map<Pair<String, ValueProperties>, ComputedValue> targetResults = calcResultModel.getValues(valueSpec.getTargetSpecification());
    ComputedValue result = targetResults.get(Pair.of(valueSpec.getValueName(), valueSpec.getProperties()));
    Object resultValue = result != null ? result.getValue() : null;
    if (resultValue != null) {
      return flattenValue ? resultValue.toString() : resultValue;
    }
    return notAvailableValue;
  }

  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
    ViewComputationResultModel resultModel = (ViewComputationResultModel) parameters[0];
    UniqueId targetId = (UniqueId) parameters[1];
    Triple<String, String, ValueProperties> requirement = ValueRequirementUtils.parseRequirement((String) parameters[2]);
    String notAvailableValue = (String) parameters[3];
    boolean flattenValue = BooleanUtils.isTrue((Boolean) parameters[4]);
    ComputationTargetSpecification target = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, targetId);
    return invoke(resultModel, requirement.getFirst(), new ValueRequirement(requirement.getSecond(), target, requirement.getThird()),
        notAvailableValue, flattenValue);
  }
  
  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }
  
  //-------------------------------------------------------------------------
  private static ValueSpecification findValueSpecification(ViewComputationResultModel resultModel, ValueRequirement valueRequirement) {
    for (Map.Entry<ValueSpecification, Set<ValueRequirement>> entry : resultModel.getRequirementToSpecificationMapping().entrySet()) {
      if (entry.getValue().contains(valueRequirement)) {
        return entry.getKey();
      }
    }
    return null;
  }

}
