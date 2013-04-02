/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCollateralizedVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyFixedRecoveryCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyForwardStartingCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyMuniCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyQuantoCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyRecoveryLockCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacySovereignCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardCollateralizedVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardFixedRecoveryCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardForwardStartingCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardMuniCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardQuantoCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardRecoveryLockCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardSovereignCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardVanillaCreditDefaultSwapDefinition;

/**
 * 
 */
public abstract class CreditInstrumentDefinitionVisitorAdapter<DATA_TYPE, RESULT_TYPE> implements CreditInstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> {

  @Override
  public RESULT_TYPE visitStandardVanillaCDS(final StandardVanillaCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardVanillaCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardVanillaCDS(final StandardVanillaCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardVanillaCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardFixedRecoveryCDS(final StandardFixedRecoveryCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardFixedRecoveryCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardFixedRecoveryCDS(final StandardFixedRecoveryCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardFixedRecoveryCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardForwardStartingCDS(final StandardForwardStartingCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardForwardStartingCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardForwardStartingCDS(final StandardForwardStartingCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardForwardStartingCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardMuniCDS(final StandardMuniCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardMuniCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardMuniCDS(final StandardMuniCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardMuniCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardQuantoCDS(final StandardQuantoCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardQuantoCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardQuantoCDS(final StandardQuantoCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardQuantoCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardRecoveryLockCDS(final StandardRecoveryLockCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardRecoveryLockCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardRecoveryLockCDS(final StandardRecoveryLockCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardRecoveryLockCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardSovereignCDS(final StandardSovereignCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardSovereignCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardSovereignCDS(final StandardSovereignCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardSovereignCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardCollateralizedVanillaCDS(final StandardCollateralizedVanillaCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardCollateralizedVanillaCDS()");
  }

  @Override
  public RESULT_TYPE visitStandardCollateralizedVanillaCDS(final StandardCollateralizedVanillaCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitStandardCollateralizedVanillaCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyVanillaCDS(final LegacyVanillaCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyVanillaCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyVanillaCDS(final LegacyVanillaCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyVanillaCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyFixedRecoveryCDS(final LegacyFixedRecoveryCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyFixedRecoveryCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyFixedRecoveryCDS(final LegacyFixedRecoveryCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyFixedRecoveryCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyForwardStartingCDS(final LegacyForwardStartingCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyForwardStartingCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyForwardStartingCDS(final LegacyForwardStartingCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyForwardStartingCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyMuniCDS(final LegacyMuniCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyMuniCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyMuniCDS(final LegacyMuniCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyMuniCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyQuantoCDS(final LegacyQuantoCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyQuantoCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyQuantoCDS(final LegacyQuantoCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyQuantoCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyRecoveryLockCDS(final LegacyRecoveryLockCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyRecoveryLockCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyRecoveryLockCDS(final LegacyRecoveryLockCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyRecoveryLockCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacySovereignCDS(final LegacySovereignCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacySovereignCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacySovereignCDS(final LegacySovereignCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacySovereignCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyCollateralizedVanillaCDS(final LegacyCollateralizedVanillaCreditDefaultSwapDefinition cds, final DATA_TYPE data) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyCollateralizedVanillaCDS()");
  }

  @Override
  public RESULT_TYPE visitLegacyCollateralizedVanillaCDS(final LegacyCollateralizedVanillaCreditDefaultSwapDefinition cds) {
    throw new NotImplementedException("This visitor (" + getClass().getName() + ") does not implement visitLegacyCollateralizedVanillaCDS()");
  }
}
