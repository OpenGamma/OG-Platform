/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import static org.testng.Assert.assertEquals;

import java.util.Comparator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CDSIndexDefinitionComponentBundleTest {

  private static Comparator<CreditDefaultSwapIndexComponent> WEIGHT_COMPARATOR =
      new Comparator<CreditDefaultSwapIndexComponent>() {
        @Override
        public int compare(CreditDefaultSwapIndexComponent o1, CreditDefaultSwapIndexComponent o2) {
          return (int) (100 * (o1.getWeight() - o2.getWeight()));
        }
      };

  private CreditDefaultSwapIndexComponent _c1;
  private CreditDefaultSwapIndexComponent _c2;
  private CreditDefaultSwapIndexComponent _c3;
  private CreditDefaultSwapIndexComponent _c4;
  private CreditDefaultSwapIndexComponent _c5;



  @BeforeMethod
  public void setUp() {
    _c1 = createComponent("d", "Maroon", 0.05);
    _c2 = createComponent("h", "Green", 0.23);
    _c3 = createComponent("a", "Yellow", 0.01);
    _c4 = createComponent("b", "Blue", 0.17);
    _c5 = createComponent("g", "Grey", 0.09);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyBundleIsNotAllowed() {
    CDSIndexComponentBundle.of();
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullComparatorIsNotAllowed() {
    CDSIndexComponentBundle.of(_c1).withCustomIdOrdering(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoNullsAllowedInComponents() {
    CDSIndexComponentBundle.of(_c1, _c2, null, _c4, _c5);
  }

  @Test
  public void testDefaultElementOrdering() {

    CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1, _c2, _c3, _c4, _c5);

    assertEquals(ImmutableList.copyOf(bundle.getComponents()),
                 ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c4, _c1, _c5, _c2));

  }

  @Test
  public void testElementsAreSortedWhenAdded() {
    CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1)
        .withCDSIndexComponents(_c2)
        .withCDSIndexComponents(_c3);

    assertEquals(ImmutableList.copyOf(bundle.getComponents()),
                 ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c1, _c2));

    CDSIndexComponentBundle updated = bundle
        .withCDSIndexComponents(_c4)
        .withCDSIndexComponents(_c5);

    assertEquals(ImmutableList.copyOf(updated.getComponents()),
                 ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c4, _c1, _c5, _c2));

  }

  @Test
  public void testCustomElementOrdering() {

    CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1, _c2, _c3, _c4, _c5).withCustomIdOrdering(WEIGHT_COMPARATOR);

    assertEquals(ImmutableList.copyOf(bundle.getComponents()),
                 ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c1, _c5, _c4, _c2));

  }

  @Test
  public void testElementsAreSortedWhenAddedToSortedBundle() {

    CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1, _c2, _c3).withCustomIdOrdering(WEIGHT_COMPARATOR);

    assertEquals(ImmutableList.copyOf(bundle.getComponents()),
                 ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c1, _c2));

    CDSIndexComponentBundle updated = bundle
        .withCDSIndexComponents(_c4)
        .withCDSIndexComponents(_c5);

    assertEquals(ImmutableList.copyOf(updated.getComponents()),
                 ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c1, _c5, _c4, _c2));

  }

  @Test
  public void testUpdatingComponentIsPossible() {

    CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1);

    // New component has same red code so should act as an update, not a new insertion
    CreditDefaultSwapIndexComponent c = createComponent("d", "Purple", 0.15);

    CDSIndexComponentBundle updated = bundle.withCDSIndexComponents(c);

    Iterable<CreditDefaultSwapIndexComponent> components = updated.getComponents();
    assertEquals(ImmutableList.copyOf(components),
                 ImmutableList.<CreditDefaultSwapIndexComponent>of(c));
  }

  @Test
  public void testUpdatingAndInsertingComponentIsPossible() {

    CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1);

    // New component has same red code so should act as an update, not a new insertion
    CreditDefaultSwapIndexComponent c1 = createComponent("d", "Purple", 0.15);
    CreditDefaultSwapIndexComponent c2 = createComponent("b", "Brown", 0.25);

    CDSIndexComponentBundle updated = bundle.withCDSIndexComponents(c1, c2);

    Iterable<CreditDefaultSwapIndexComponent> components = updated.getComponents();
    assertEquals(ImmutableList.copyOf(components),
                 ImmutableList.<CreditDefaultSwapIndexComponent>of(c2, c1));
  }

  @Test
  public void testUpdatingSameComponentIsPossible() {

    CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1);

    // New component has same red code so should act as an update, not a new insertion
    CreditDefaultSwapIndexComponent c1 = createComponent("d", "Purple", 0.15);
    // But this is also an update to the same
    CreditDefaultSwapIndexComponent c2 = createComponent("d", "Lilac", 0.25);

    CDSIndexComponentBundle updated = bundle.withCDSIndexComponents(c1, c2);

    Iterable<CreditDefaultSwapIndexComponent> components = updated.getComponents();
    assertEquals(ImmutableList.copyOf(components),
                 ImmutableList.<CreditDefaultSwapIndexComponent>of(c2));
  }

  @Test
  public void testCreatingSameComponentIsPossible() {

    // New component has same red code so should act as an update, not a new insertion
    CreditDefaultSwapIndexComponent c1 = createComponent("d", "Purple", 0.15);

    CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1, c1);

    Iterable<CreditDefaultSwapIndexComponent> components = bundle.getComponents();
    assertEquals(ImmutableList.copyOf(components),
                 ImmutableList.<CreditDefaultSwapIndexComponent>of(c1));
  }


  private CreditDefaultSwapIndexComponent createComponent(String red, String name, double weight) {
    return new CreditDefaultSwapIndexComponent(name, redCode(red), weight, null);
  }

  private ExternalId redCode(String red) {
    return ExternalSchemes.markItRedCode(red);
  }

}