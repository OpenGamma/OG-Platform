/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Produces Vomma (aka Volga) for FXOption's in the Black (Garman-Kohlhagen) world.
 * This is the spot vomma, the 2nd order sensitivity of the present value to the implied vol,
 *          $\frac{\partial^2 (PV)}{\partial spot \partial \sigma}$
 */
public class FXOptionBlackVommaFunction extends FXOptionBlackSingleValuedFunction {

  public FXOptionBlackVommaFunction() {
    super(ValueRequirementNames.VALUE_VOMMA);
  }

  /** The pricing method, Black (Garman-Kohlhagen) */
  private static final ForexOptionVanillaBlackSmileMethod METHOD = ForexOptionVanillaBlackSmileMethod.getInstance();

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXOptionSecurity
        || target.getSecurity() instanceof NonDeliverableFXOptionSecurity;
  }


  @Override
  protected Set<ComputedValue> getResult(InstrumentDerivative forex, ForexOptionDataBundle<?> data, ComputationTarget target, Set<ValueRequirement> desiredValues, FunctionInputs inputs,
      ValueSpecification spec, FunctionExecutionContext executionContext) {

    ArgumentChecker.isTrue(forex instanceof ForexOptionVanilla, "FXOptionBlackVommaFunction only handles ForexOptionVanilla. Contact Quant team.");
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final CurrencyAmount vommaCcy = METHOD.vomma((ForexOptionVanilla) forex, data);
      final double vommaValue = vommaCcy.getAmount(); // FIXME: Confirm scaling
      return Collections.singleton(new ComputedValue(spec, vommaValue));
    }
    throw new OpenGammaRuntimeException("Can only calculate vomma for vol surfaces with smiles");
  }

}
