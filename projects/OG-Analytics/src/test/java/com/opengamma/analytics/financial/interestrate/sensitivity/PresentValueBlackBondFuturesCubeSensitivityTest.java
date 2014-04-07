/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.sensitivity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.util.amount.CubeValue;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Triple;

/**
 * Tests related to the present value Black volatility sensitivity.
 */
@Test(groups = TestGroup.UNIT)
public class PresentValueBlackBondFuturesCubeSensitivityTest {

  private static final CubeValue CUBE_VAL = new CubeValue();
  private static final Triple<Double, Double, Double> POINT1 = Triple.of(0.5, 0.25, 1.10);
  private static final Triple<Double, Double, Double> POINT2 = Triple.of(0.5, 0.25, 1.15);
  private static final double VALUE1 = 1234.5;
  private static final double VALUE2 = 5432.1;

  static {
    CUBE_VAL.add(POINT1, VALUE1);
    CUBE_VAL.add(POINT2, VALUE2);
  }
  /** The legal entity */
  private static final LegalEntity[] LEGAL_ENTITIES = IssuerProviderDiscountDataSets.getIssuers();
  private static final LegalEntity LEGAL_ENTITY_GERMANY = LEGAL_ENTITIES[2];
  private static final Currency CCY = Currency.AUD;

  private static final PresentValueBlackBondFuturesCubeSensitivity SENSITIVITY_EMPTY =
      new PresentValueBlackBondFuturesCubeSensitivity(CCY, LEGAL_ENTITY_GERMANY);

  private static final PresentValueBlackBondFuturesCubeSensitivity SENSITIVITY_CUBE =
      new PresentValueBlackBondFuturesCubeSensitivity(CUBE_VAL, CCY, LEGAL_ENTITY_GERMANY);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency1() {
    new PresentValueBlackBondFuturesCubeSensitivity(null, LEGAL_ENTITY_GERMANY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEntity1() {
    new PresentValueBlackBondFuturesCubeSensitivity(CCY, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCube() {
    new PresentValueBlackBondFuturesCubeSensitivity((CubeValue) null, CCY, LEGAL_ENTITY_GERMANY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency2() {
    new PresentValueBlackBondFuturesCubeSensitivity(CUBE_VAL, null, LEGAL_ENTITY_GERMANY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEntity2() {
    new PresentValueBlackBondFuturesCubeSensitivity(CUBE_VAL, CCY, null);
  }

  @Test
  public void getter() {
    assertEquals("PresentValueBlackBondFuturesCubeSensitivity: getter", CUBE_VAL, SENSITIVITY_CUBE.getSensitivity());
    assertEquals("PresentValueBlackBondFuturesCubeSensitivity: getter", CCY, SENSITIVITY_CUBE.getCurrency());
    assertEquals("PresentValueBlackBondFuturesCubeSensitivity: getter", LEGAL_ENTITY_GERMANY, SENSITIVITY_CUBE.getLegalEntiry());
    assertEquals("PresentValueBlackBondFuturesCubeSensitivity: getter", CCY, SENSITIVITY_EMPTY.getCurrency());
    assertEquals("PresentValueBlackBondFuturesCubeSensitivity: getter", LEGAL_ENTITY_GERMANY, SENSITIVITY_EMPTY.getLegalEntiry());
  }

  @Test
  public void addPoint() {
    final Triple<Double, Double, Double> POINT = Triple.of(0.75, 0.25, 1.10);
    final double value = 5555.5;
    final CubeValue cube = new CubeValue();
    cube.getMap().putAll(CUBE_VAL.getMap());
    PresentValueBlackBondFuturesCubeSensitivity sensi1 =
        new PresentValueBlackBondFuturesCubeSensitivity(cube.getMap(), CCY, LEGAL_ENTITY_GERMANY);
    sensi1.addSensitivity(POINT, value);
    cube.add(POINT, value);
    assertEquals("PresentValueBlackBondFuturesCubeSensitivity: add", sensi1.getSensitivity(), cube);
    cube.add(POINT, 2 * value);
    assertFalse("PresentValueBlackBondFuturesCubeSensitivity: add", sensi1.getSensitivity().equals(cube));
  }

  @Test
  public void multipliedBy() {
    final double factor = 2.0;
    final CubeValue cube = new CubeValue();
    cube.getMap().putAll(CUBE_VAL.getMap());
    PresentValueBlackBondFuturesCubeSensitivity sensi1 =
        new PresentValueBlackBondFuturesCubeSensitivity(cube.getMap(), CCY, LEGAL_ENTITY_GERMANY);
    final PresentValueBlackBondFuturesCubeSensitivity sensi2 = sensi1.multipliedBy(factor);
    assertEquals("PresentValueBlackBondFuturesCubeSensitivity: add", CubeValue.multiplyBy(sensi1.getSensitivity(), factor),
        sensi2.getSensitivity());
    assertEquals("PresentValueBlackBondFuturesCubeSensitivity: add", sensi1.getSensitivity(), cube);
  }

  @Test
  public void plus() {
    final CubeValue cube1 = new CubeValue();
    cube1.getMap().putAll(CUBE_VAL.getMap());
    PresentValueBlackBondFuturesCubeSensitivity sensi1 =
        new PresentValueBlackBondFuturesCubeSensitivity(cube1.getMap(), CCY, LEGAL_ENTITY_GERMANY);
    final Triple<Double, Double, Double> POINT = Triple.of(0.75, 0.25, 1.10);
    final double value = 5555.5;
    final CubeValue cube2 = new CubeValue();
    cube2.add(POINT, value);
    final PresentValueBlackBondFuturesCubeSensitivity sensi2 =
        new PresentValueBlackBondFuturesCubeSensitivity(cube2.getMap(), CCY, LEGAL_ENTITY_GERMANY);
    final PresentValueBlackBondFuturesCubeSensitivity sensi3 = sensi1.plus(sensi2);
    assertEquals("PresentValueBlackBondFuturesCubeSensitivity: add", CubeValue.plus(sensi1.getSensitivity(), sensi2.getSensitivity()),
        sensi3.getSensitivity());
    assertEquals("PresentValueBlackBondFuturesCubeSensitivity: add", sensi1.getSensitivity(), cube1);
  }

}
