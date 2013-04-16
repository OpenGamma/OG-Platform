/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

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
 * Visitor interface for credit instruments.
 * @param <DATA_TYPE> The type of the market data
 * @param <RESULT_TYPE> The type of the result
 */
public interface CreditInstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> {

  /**
   * @param cds a standard vanilla credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitStandardVanillaCDS(StandardVanillaCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a standard vanilla credit default swap
   * @return A result.
   */
  RESULT_TYPE visitStandardVanillaCDS(StandardVanillaCreditDefaultSwapDefinition cds);

  /**
   * @param cds a standard fixed recovery credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitStandardFixedRecoveryCDS(StandardFixedRecoveryCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a standard fixed recovery credit default swap
   * @return A result.
   */
  RESULT_TYPE visitStandardFixedRecoveryCDS(StandardFixedRecoveryCreditDefaultSwapDefinition cds);

  /**
   * @param cds a standard forward starting credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitStandardForwardStartingCDS(StandardForwardStartingCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a standard forward starting credit default swap
   * @return A result.
   */
  RESULT_TYPE visitStandardForwardStartingCDS(StandardForwardStartingCreditDefaultSwapDefinition cds);

  /**
   * @param cds a standard municipal credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitStandardMuniCDS(StandardMuniCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a standard municipal credit default swap
   * @return A result.
   */
  RESULT_TYPE visitStandardMuniCDS(StandardMuniCreditDefaultSwapDefinition cds);

  /**
   * @param cds a standard quanto credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitStandardQuantoCDS(StandardQuantoCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a standard quanto credit default swap
   * @return A result.
   */
  RESULT_TYPE visitStandardQuantoCDS(StandardQuantoCreditDefaultSwapDefinition cds);

  /**
   * @param cds a standard recovery lock credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitStandardRecoveryLockCDS(StandardRecoveryLockCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a standard recovery lock credit default swap
   * @return A result.
   */
  RESULT_TYPE visitStandardRecoveryLockCDS(StandardRecoveryLockCreditDefaultSwapDefinition cds);

  /**
   * @param cds a standard sovereign credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitStandardSovereignCDS(StandardSovereignCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a standard fixed recovery credit default swap
   * @return A result.
   */
  RESULT_TYPE visitStandardSovereignCDS(StandardSovereignCreditDefaultSwapDefinition cds);

  /**
   * @param cds a standard collateralised credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitStandardCollateralizedVanillaCDS(StandardCollateralizedVanillaCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a standard collateralised credit default swap
   * @return A result.
   */
  RESULT_TYPE visitStandardCollateralizedVanillaCDS(StandardCollateralizedVanillaCreditDefaultSwapDefinition cds);

  /**
   * @param cds a legacy vanilla credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitLegacyVanillaCDS(LegacyVanillaCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a legacy vanilla credit default swap
   * @return A result.
   */
  RESULT_TYPE visitLegacyVanillaCDS(LegacyVanillaCreditDefaultSwapDefinition cds);

  /**
   * @param cds a legacy fixed recovery credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitLegacyFixedRecoveryCDS(LegacyFixedRecoveryCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a legacy fixed recovery credit default swap
   * @return A result.
   */
  RESULT_TYPE visitLegacyFixedRecoveryCDS(LegacyFixedRecoveryCreditDefaultSwapDefinition cds);

  /**
   * @param cds a legacy forward starting credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitLegacyForwardStartingCDS(LegacyForwardStartingCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a legacy forward starting credit default swap
   * @return A result.
   */
  RESULT_TYPE visitLegacyForwardStartingCDS(LegacyForwardStartingCreditDefaultSwapDefinition cds);

  /**
   * @param cds a legacy municipal credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitLegacyMuniCDS(LegacyMuniCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a legacy municipal credit default swap
   * @return A result.
   */
  RESULT_TYPE visitLegacyMuniCDS(LegacyMuniCreditDefaultSwapDefinition cds);

  /**
   * @param cds a legacy quanto credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitLegacyQuantoCDS(LegacyQuantoCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a legacy quanto credit default swap
   * @return A result.
   */
  RESULT_TYPE visitLegacyQuantoCDS(LegacyQuantoCreditDefaultSwapDefinition cds);

  /**
   * @param cds a legacy recovery lock credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitLegacyRecoveryLockCDS(LegacyRecoveryLockCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a legacy recovery lock credit default swap
   * @return A result.
   */
  RESULT_TYPE visitLegacyRecoveryLockCDS(LegacyRecoveryLockCreditDefaultSwapDefinition cds);

  /**
   * @param cds a legacy sovereign credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitLegacySovereignCDS(LegacySovereignCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a legacy fixed recovery credit default swap
   * @return A result.
   */
  RESULT_TYPE visitLegacySovereignCDS(LegacySovereignCreditDefaultSwapDefinition cds);

  /**
   * @param cds a legacy collateralised credit default swap
   * @param data The data to be used in calculations
   * @return A result.
   */
  RESULT_TYPE visitLegacyCollateralizedVanillaCDS(LegacyCollateralizedVanillaCreditDefaultSwapDefinition cds, DATA_TYPE data);

  /**
   * @param cds a legacy collateralised credit default swap
   * @return A result.
   */
  RESULT_TYPE visitLegacyCollateralizedVanillaCDS(LegacyCollateralizedVanillaCreditDefaultSwapDefinition cds);

}
