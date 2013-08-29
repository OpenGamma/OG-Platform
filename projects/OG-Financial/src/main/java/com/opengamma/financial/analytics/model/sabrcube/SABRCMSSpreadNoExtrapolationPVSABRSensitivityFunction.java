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
import com.opengamma.financial.analytics.model.sabr.SABRDiscountingFunction;

/**
 * @deprecated Use descendants of {@link SABRDiscountingFunction}
 */
@Deprecated
public abstract class SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction extends SABRCMSSpreadNoExtrapolationFunction {
  private static final PresentValueSABRSensitivitySABRCalculator CALCULATOR = PresentValueSABRSensitivitySABRCalculator.getInstance();

  @Override
  protected Object getResult(final InstrumentDerivative derivative, final SABRInterestRateDataBundle data, final ValueRequirement desiredValue) {
    return getResultAsMatrix(derivative.accept(CALCULATOR, data));
  }

  protected abstract DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities);

  /**
   * Function to get the sensitivity to the alpha parameter
   */
  public static class Alpha extends SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY;
    }

    @Override
    protected DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities) {
      return SABRCubeUtils.toDoubleLabelledMatrix2D(sensitivities.getAlpha());
    }

  }

  /**
   * Function to get the sensitivity to the rho parameter
   */
  public static class Rho extends SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY;
    }

    @Override
    protected DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities) {
      return SABRCubeUtils.toDoubleLabelledMatrix2D(sensitivities.getRho());
    }

  }

  /**
   * Function to get the sensitivity to the nu parameter
   */
  public static class Nu extends SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY;
    }

    @Override
    protected DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities) {
      return SABRCubeUtils.toDoubleLabelledMatrix2D(sensitivities.getNu());
    }

  }

}
