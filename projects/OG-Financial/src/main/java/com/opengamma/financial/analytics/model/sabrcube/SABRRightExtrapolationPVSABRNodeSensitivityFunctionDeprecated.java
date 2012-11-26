/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivitySABRRightExtrapolationCalculator;
import com.opengamma.analytics.financial.interestrate.SABRSensitivityNodeCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see SABRRightExtrapolationPVSABRNodeSensitivityFunction
 */
@Deprecated
public abstract class SABRRightExtrapolationPVSABRNodeSensitivityFunctionDeprecated extends SABRRightExtrapolationFunctionDeprecated {

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final ValueProperties properties = getResultProperties(createValueProperties().get(), currency);
    return Collections.singleton(new ValueSpecification(getValueRequirement(), target.toSpecification(), properties));
  }

  @Override
  protected Object getResult(final InstrumentDerivative derivative, final SABRInterestRateDataBundle data, final ValueRequirement desiredValue) {
    final Double cutoff = Double.parseDouble(desiredValue.getConstraint(SABRRightExtrapolationFunctionDeprecated.PROPERTY_CUTOFF_STRIKE));
    final Double mu = Double.parseDouble(desiredValue.getConstraint(SABRRightExtrapolationFunctionDeprecated.PROPERTY_TAIL_THICKNESS_PARAMETER));
    final PresentValueSABRSensitivitySABRRightExtrapolationCalculator calculator = new PresentValueSABRSensitivitySABRRightExtrapolationCalculator(cutoff, mu);
    final PresentValueSABRSensitivityDataBundle result = derivative.accept(calculator, data);
    return getResultAsMatrix(SABRSensitivityNodeCalculator.calculateNodeSensitivities(result, data.getSABRParameter()));
  }

  protected abstract DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities);

  /**
   * @deprecated Function to get the sensitivity to the alpha parameter
   */
  @Deprecated
  public static class Alpha extends SABRRightExtrapolationPVSABRNodeSensitivityFunctionDeprecated {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY;
    }

    @Override
    protected DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities) {
      return SABRCubeUtil.toDoubleLabelledMatrix2D(sensitivities.getAlpha());
    }

  }

  /**
   * @deprecated Function to get the sensitivity to the rho parameter
   */
  @Deprecated
  public static class Rho extends SABRRightExtrapolationPVSABRNodeSensitivityFunctionDeprecated {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY;
    }

    @Override
    protected DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities) {
      return SABRCubeUtil.toDoubleLabelledMatrix2D(sensitivities.getRho());
    }

  }

  /**
   * @deprecated Function to get the sensitivity to the nu parameter
   */
  @Deprecated
  public static class Nu extends SABRRightExtrapolationPVSABRNodeSensitivityFunctionDeprecated {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY;
    }

    @Override
    protected DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities) {
      return SABRCubeUtil.toDoubleLabelledMatrix2D(sensitivities.getNu());
    }

  }

}
