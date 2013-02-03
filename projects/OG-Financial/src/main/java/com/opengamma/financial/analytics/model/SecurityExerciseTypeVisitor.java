/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;

/**
 * Gets the exercise type (e.g. European, American) of options.
 */
public class SecurityExerciseTypeVisitor extends FinancialSecurityVisitorAdapter<ExerciseType> {

  /**
   * Default constructor
   */
  public SecurityExerciseTypeVisitor() {
  }

  @Override
  public ExerciseType visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    return security.getExerciseType();
  }

  @Override
  public ExerciseType visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    return security.getExerciseType();
  }

  @Override
  public ExerciseType visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    return security.getExerciseType();
  }

  @Override
  public ExerciseType visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return security.getExerciseType();
  }

  @Override
  public ExerciseType visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return security.getExerciseType();
  }

  @Override
  public ExerciseType visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return security.getExerciseType();
  }

  @Override
  public ExerciseType visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return security.getExerciseType();
  }

  @Override
  public ExerciseType visitFXOptionSecurity(final FXOptionSecurity security) {
    return security.getExerciseType();
  }

  @Override
  public ExerciseType visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    return security.getExerciseType();
  }

  @Override
  public ExerciseType visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return security.getExerciseType();
  }
}
