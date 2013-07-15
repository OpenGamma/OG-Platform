/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RectangularViewportDefinitionTest {

  @Test
  public void iterator() {
    RectangularViewportDefinition viewportCells = new RectangularViewportDefinition(0,
                                                                                    ImmutableList.of(3, 4, 6),
                                                                                    ImmutableList.of(8, 10, 11),
                                                                                    TypeFormatter.Format.EXPANDED,
                                                                                    false);
    ImmutableList<GridCell> gridCells = ImmutableList.copyOf(viewportCells.iterator());
    List<GridCell> expectedCells = ImmutableList.of(
        new GridCell(3, 8, TypeFormatter.Format.EXPANDED),
        new GridCell(3, 10, TypeFormatter.Format.EXPANDED),
        new GridCell(3, 11, TypeFormatter.Format.EXPANDED),
        new GridCell(4, 8, TypeFormatter.Format.EXPANDED),
        new GridCell(4, 10, TypeFormatter.Format.EXPANDED),
        new GridCell(4, 11, TypeFormatter.Format.EXPANDED),
        new GridCell(6, 8, TypeFormatter.Format.EXPANDED),
        new GridCell(6, 10, TypeFormatter.Format.EXPANDED),
        new GridCell(6, 11, TypeFormatter.Format.EXPANDED));
    assertEquals(expectedCells, gridCells);
  }

  @Test
  public void isValidForGrid() {
    RectangularViewportDefinition viewportCells = new RectangularViewportDefinition(0,
                                                                                    ImmutableList.of(3, 4, 6),
                                                                                    ImmutableList.of(8, 10),
                                                                                    TypeFormatter.Format.EXPANDED,
                                                                                    false);
    GridStructure validStructure = mock(GridStructure.class);
    stub(validStructure.getColumnCount()).toReturn(20);
    stub(validStructure.getRowCount()).toReturn(10);
    assertTrue(viewportCells.isValidFor(validStructure));

    GridStructure invalidColumns = mock(GridStructure.class);
    stub(invalidColumns.getColumnCount()).toReturn(5);
    stub(invalidColumns.getRowCount()).toReturn(10);
    assertFalse(viewportCells.isValidFor(invalidColumns));

    GridStructure invalidRows = mock(GridStructure.class);
    stub(invalidRows.getColumnCount()).toReturn(15);
    stub(invalidRows.getRowCount()).toReturn(5);
    assertFalse(viewportCells.isValidFor(invalidRows));
  }
}
