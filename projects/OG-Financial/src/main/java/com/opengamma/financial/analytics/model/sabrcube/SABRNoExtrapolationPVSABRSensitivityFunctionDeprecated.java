/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see SABRNoExtrapolationPVSABRSensitivityFunction
 */
@Deprecated
public abstract class SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated extends SABRNoExtrapolationFunctionDeprecated {

  private static final PresentValueSABRSensitivitySABRCalculator CALCULATOR = PresentValueSABRSensitivitySABRCalculator.getInstance();

  @Override
  protected Object getResult(final InstrumentDerivative derivative, final SABRInterestRateDataBundle data, final ValueRequirement desiredValue) {
    return getResultAsMatrix(derivative.accept(CALCULATOR, data));
  }

  protected abstract DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities);

  /**
   * @deprecated Function to get the sensitivity to the alpha parameter
   */
  @Deprecated
  public static class Alpha extends SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY;
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
  public static class Rho extends SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY;
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
  public static class Nu extends SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY;
    }

    @Override
    protected DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities) {
      return SABRCubeUtil.toDoubleLabelledMatrix2D(sensitivities.getNu());
    }

  }

}
