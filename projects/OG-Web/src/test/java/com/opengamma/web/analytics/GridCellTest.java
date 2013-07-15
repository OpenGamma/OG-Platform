/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GridCellTest {

  @Test
  public void testCompareTo() throws Exception {
    List<GridCell> cells = Lists.newArrayList(
        new GridCell(1, 0, TypeFormatter.Format.CELL),
        new GridCell(1, 1, TypeFormatter.Format.EXPANDED),
        new GridCell(2, 1, TypeFormatter.Format.CELL),
        new GridCell(0, 1, TypeFormatter.Format.CELL),
        new GridCell(1, 1, TypeFormatter.Format.CELL),
        new GridCell(0, 3, TypeFormatter.Format.CELL),
        new GridCell(2, 0, TypeFormatter.Format.CELL));
    Collections.sort(cells);
    ImmutableList<GridCell> expected = ImmutableList.of(
        new GridCell(0, 1, TypeFormatter.Format.CELL),
        new GridCell(0, 3, TypeFormatter.Format.CELL),
        new GridCell(1, 0, TypeFormatter.Format.CELL),
        new GridCell(1, 1, TypeFormatter.Format.CELL),
        new GridCell(1, 1, TypeFormatter.Format.EXPANDED),
        new GridCell(2, 0, TypeFormatter.Format.CELL),
        new GridCell(2, 1, TypeFormatter.Format.CELL));
    assertEquals(expected, cells);
  }

  @Test
  public void buildFromString() {
    GridCell fromString = new GridCell("1, 2, CELL");
    assertEquals(new GridCell(1, 2, TypeFormatter.Format.CELL), fromString);
  }
}
