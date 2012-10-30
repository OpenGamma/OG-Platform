/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class GridCellTest {

  @Test
  public void testCompareTo() throws Exception {
    List<GridCell> cells = Lists.newArrayList(
        new GridCell(1, 0),
        new GridCell(2, 1),
        new GridCell(0, 1),
        new GridCell(1, 1),
        new GridCell(0, 3),
        new GridCell(2, 0));
    Collections.sort(cells);
    ImmutableList<GridCell> expected = ImmutableList.of(
        new GridCell(0, 1),
        new GridCell(0, 3),
        new GridCell(1, 0),
        new GridCell(1, 1),
        new GridCell(2, 0),
        new GridCell(2, 1));
    AssertJUnit.assertEquals(expected, cells);
  }
}
