/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.async.AsynchronousExecution;
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

  public static Object invoke(final ViewComputationResultModel resultModel, final String calcConfigName, final String valueName, final ComputationTargetSpecification target,
      final ValueProperties constraints, final String notAvailableValue, final boolean flattenValue) {
    final ViewCalculationResultModel calcResultModel = resultModel.getCalculationResult(calcConfigName);
    if (calcResultModel == null) {
      // TODO should return #NA if notAvailableValue is null
      return notAvailableValue;
    }
    Collection<ComputedValueResult> results = calcResultModel.getAllValues(target);
    if (results == null) {
      // TODO should return #NA if notAvailableValue is null
      return notAvailableValue;
    }
    for (ComputedValueResult result : results) {
      if (valueName.equals(result.getSpecification().getValueName()) && constraints.isSatisfiedBy(result.getSpecification().getProperties())) {
        final Object resultValue = result.getValue();
        if (resultValue != null) {
          // TODO: Use more intelligent value flatteners than toString
          return flattenValue ? resultValue.toString() : resultValue;
        }
      }
    }
    return notAvailableValue;
  }

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) throws AsynchronousExecution {
    final ViewComputationResultModel resultModel = (ViewComputationResultModel) parameters[0];
    final UniqueId targetId = (UniqueId) parameters[1];
    final Triple<String, String, ValueProperties> requirement = ValueRequirementUtils.parseRequirement((String) parameters[2]);
    final String notAvailableValue = (String) parameters[3];
    final boolean flattenValue = BooleanUtils.isTrue((Boolean) parameters[4]);
    final ComputationTargetSpecification target = ComputationTargetSpecification.of(targetId);
    return invoke(resultModel, requirement.getFirst(), requirement.getSecond(), target, requirement.getThird(), notAvailableValue, flattenValue);
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
