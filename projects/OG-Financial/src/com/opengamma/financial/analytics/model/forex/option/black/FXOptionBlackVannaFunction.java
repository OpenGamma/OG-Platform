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
 * Produces Vanna for FXOption's in the Black (Garman-Kohlhagen) world.
 * This is the spot vanna, ie the 2nd order cross-sensitivity of the present value to the spot and implied vol,
 *          $\frac{\partial^2 (PV)}{\partial spot \partial \sigma}$
 */
public class FXOptionBlackVannaFunction extends FXOptionBlackSingleValuedFunction {

  public FXOptionBlackVannaFunction() {
    super(ValueRequirementNames.VALUE_VANNA);
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

    ArgumentChecker.isTrue(forex instanceof ForexOptionVanilla, "FXOptionBlackVannaFunction only handles ForexOptionVanilla. Contact Quant team.");
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final CurrencyAmount vannaCcy = METHOD.vanna((ForexOptionVanilla) forex, data);
      final double vannaValue = vannaCcy.getAmount(); // FIXME: Confirm scaling
      return Collections.singleton(new ComputedValue(spec, vannaValue));
    }
    throw new OpenGammaRuntimeException("Can only calculate vanna for vol surfaces with smiles");
  }

}
