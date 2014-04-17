/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;

/**
 * Test the provider of issuer provider with bond futures volatility.
 */
public class BlackBondFuturesFlatProviderDiscountTest {

  /** Curves for a specific issuer name */
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVES = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  /** Surface with Black vols */
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_LINEAR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);
  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_DEL = InterpolatedDoublesSurface.from(
      new double[] {0.20, 0.20, 0.20, 0.45, 0.45, 0.45 },
      new double[] {0.00, 0.08, 0.16, 0.00, 0.08, 0.16 },
      new double[] {0.35, 0.34, 0.32, 0.30, 0.26, 0.24 },
      INTERPOLATOR_LINEAR_2D);
  /** The legal entity */
  private static final LegalEntity[] LEGAL_ENTITIES = IssuerProviderDiscountDataSets.getIssuers();
  private static final LegalEntity LEGAL_ENTITY_GERMANY = LEGAL_ENTITIES[2];

  private static final BlackBondFuturesFlatProviderDiscount BLACK_FLAT_BNDFUT = new BlackBondFuturesFlatProviderDiscount(ISSUER_SPECIFIC_MULTICURVES,
      BLACK_SURFACE_EXP_DEL, LEGAL_ENTITY_GERMANY);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullProvider() {
    new BlackBondFuturesFlatProviderDiscount(null, BLACK_SURFACE_EXP_DEL, LEGAL_ENTITY_GERMANY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSurface() {
    new BlackBondFuturesFlatProviderDiscount(ISSUER_SPECIFIC_MULTICURVES, null, LEGAL_ENTITY_GERMANY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLegalEntity() {
    new BlackBondFuturesFlatProviderDiscount(ISSUER_SPECIFIC_MULTICURVES, BLACK_SURFACE_EXP_DEL, null);
  }

  @Test
  public void getter() {
    assertEquals("BlackBondFuturesFlatProviderDiscount: getter", ISSUER_SPECIFIC_MULTICURVES, BLACK_FLAT_BNDFUT.getIssuerProvider());
    assertEquals("BlackBondFuturesFlatProviderDiscount: getter", BLACK_SURFACE_EXP_DEL, BLACK_FLAT_BNDFUT.getBlackParameters());
    assertEquals("BlackBondFuturesFlatProviderDiscount: getter", LEGAL_ENTITY_GERMANY, BLACK_FLAT_BNDFUT.getLegalEntity());
  }

  @Test
  public void equalHash() {
    assertTrue("BlackBondFuturesFlatProviderDiscount: equal - hash", BLACK_FLAT_BNDFUT.equals(BLACK_FLAT_BNDFUT));
    final BlackBondFuturesFlatProviderDiscount duplicated = new BlackBondFuturesFlatProviderDiscount(ISSUER_SPECIFIC_MULTICURVES,
        BLACK_SURFACE_EXP_DEL, LEGAL_ENTITY_GERMANY);
    assertTrue("BlackBondFuturesFlatProviderDiscount: equal - hash", BLACK_FLAT_BNDFUT.equals(duplicated));
    assertTrue("BlackBondFuturesFlatProviderDiscount: equal - hash", BLACK_FLAT_BNDFUT.hashCode() == duplicated.hashCode());
    BlackBondFuturesFlatProviderDiscount modified;
    final IssuerProviderDiscount providerOther = IssuerProviderDiscountDataSets.getIssuerSpecificProviderAus();
    modified = new BlackBondFuturesFlatProviderDiscount(providerOther, BLACK_SURFACE_EXP_DEL, LEGAL_ENTITY_GERMANY);
    assertFalse("BlackBondFuturesFlatProviderDiscount: equal - hash", BLACK_FLAT_BNDFUT.equals(modified));
    final InterpolatedDoublesSurface surfaceOther = InterpolatedDoublesSurface.from(
        new double[] {0.20, 0.20, 0.25, 0.45, 0.45, 0.45 },
        new double[] {0.00, 0.08, 0.17, 0.00, 0.08, 0.16 },
        new double[] {0.35, 0.34, 0.32, 0.30, 0.26, 0.24 },
        INTERPOLATOR_LINEAR_2D);
    modified = new BlackBondFuturesFlatProviderDiscount(ISSUER_SPECIFIC_MULTICURVES, surfaceOther, LEGAL_ENTITY_GERMANY);
    assertFalse("BlackBondFuturesFlatProviderDiscount: equal - hash", BLACK_FLAT_BNDFUT.equals(modified));
    modified = new BlackBondFuturesFlatProviderDiscount(ISSUER_SPECIFIC_MULTICURVES, BLACK_SURFACE_EXP_DEL, LEGAL_ENTITIES[0]);
    assertFalse("BlackBondFuturesFlatProviderDiscount: equal - hash", BLACK_FLAT_BNDFUT.equals(modified));
    assertFalse("BlackBondFuturesFlatProviderDiscount: equal - hash", BLACK_FLAT_BNDFUT.equals(null));
    assertFalse("BlackBondFuturesFlatProviderDiscount: equal - hash", BLACK_FLAT_BNDFUT.equals(LINEAR_FLAT));
  }

}
