/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public abstract class SABRRightExtrapolationPresentValueSABRSensitivityFunction extends SABRRightExtrapolationFunction {
  private static final PresentValueSABRSensitivitySABRCalculator CALCULATOR = PresentValueSABRSensitivitySABRCalculator.getInstance();

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final ValueProperties properties = getResultProperties(createValueProperties().get(), currency);
    return Collections.singleton(new ValueSpecification(getValueRequirement(), target.toSpecification(), properties));
  }

  @Override
  protected Object getResult(final InstrumentDerivative derivative, final SABRInterestRateDataBundle data) {
    return getResultAsMatrix(CALCULATOR.visit(derivative, data));
  }

  protected abstract Map<DoublesPair, Double> getDataForRequirement(final PresentValueSABRSensitivityDataBundle sensitivities);

  private DoubleLabelledMatrix2D getResultAsMatrix(final PresentValueSABRSensitivityDataBundle sensitivities) {
    final Map<DoublesPair, Double> data = getDataForRequirement(sensitivities);
    final Map.Entry<DoublesPair, Double> entry = data.entrySet().iterator().next();
    return new DoubleLabelledMatrix2D(new Double[] {entry.getKey().first}, new Double[] {entry.getKey().second}, new double[][] {new double[] {entry.getValue()}});
  }

  /**
   * Function to get the sensitivity to the alpha parameter
   */
  public static class Alpha extends SABRRightExtrapolationPresentValueSABRSensitivityFunction {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY;
    }

    @Override
    protected Map<DoublesPair, Double> getDataForRequirement(final PresentValueSABRSensitivityDataBundle sensitivities) {
      return sensitivities.getAlpha().getMap();
    }

  }

  /**
   * Function to get the sensitivity to the nu parameter
   */
  public static class Nu extends SABRRightExtrapolationPresentValueSABRSensitivityFunction {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY;
    }

    @Override
    protected Map<DoublesPair, Double> getDataForRequirement(final PresentValueSABRSensitivityDataBundle sensitivities) {
      return sensitivities.getNu().getMap();
    }

  }

  /**
   * Function to get the sensitivity to the rho parameter
   */
  public static class Rho extends SABRRightExtrapolationPresentValueSABRSensitivityFunction {

    @Override
    protected String getValueRequirement() {
      return ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY;
    }

    @Override
    protected Map<DoublesPair, Double> getDataForRequirement(final PresentValueSABRSensitivityDataBundle sensitivities) {
      return sensitivities.getRho().getMap();
    }

  }
}
