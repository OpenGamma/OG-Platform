/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.centralcounterparty;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.analytics.financial.credit.sampleobligors.SampleObligors;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CentralCounterpartyTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Define two obligors A and B
  private static final Obligor A = SampleObligors.getObligor_ABC();
  private static final Obligor B = SampleObligors.getObligor_XYZ();

  // Define a reference entity C
  private static final Obligor C = SampleObligors.getObligor_RefEnt();

  // Build a standard CDS that is not centrally cleared but transacted OTC; prot buyer A, seller B and ref entity C

  // Build a standard collateralised CDS that is centrally cleared; prot buyer A, seller B and ref entity C

  // Define a CSA between A and B
  // Define a collateral object between A and B

  // Build a CCP

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
