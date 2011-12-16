/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.id.UniqueId;
import com.opengamma.web.server.WebGridCell;
import com.opengamma.web.server.push.rest.ViewportDefinitionMessageBodyReader;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 *
 */
public class ViewportDefinitionTest {

  // TODO this need to be moved
  @Test
  public void isReadable() {
    ViewportDefinitionMessageBodyReader reader = new ViewportDefinitionMessageBodyReader();
    assertTrue(reader.isReadable(ViewportDefinition.class, null, null, null));
    // a random other class that shouldn't be readable
    assertFalse(reader.isReadable(Viewport.class, null, null, null));
  }

  @Test
  public void fromJSON() throws IOException {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"marketDataType\": \"snapshot\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"portfolioViewport\": {" +
        "\"rowIds\": [3, 4, 5], " +
        "\"lastTimestamps\": [12345678, 12345679, 12345680], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]," +
        "\"fullConversionModeCells\": [[5, 1], [5, 3]]" +
        "}, " +
        "\"primitiveViewport\": {" +
        "\"rowIds\": [5, 6, 7], " +
        "\"lastTimestamps\": [12345681, 12345682, 12345683], " +
        "\"dependencyGraphCells\": [[5, 1], [5, 2], [7, 3]]," +
        "\"fullConversionModeCells\": [[6, 2], [7, 3]]" +
        "}" +
        "}";
    ViewportDefinition viewportDefinition = ViewportDefinition.fromJSON(json);

    assertEquals("testViewDefName", viewportDefinition.getViewDefinitionName());
    assertEquals(UniqueId.of("Tst", "123"), viewportDefinition.getSnapshotId());

    // portfolio viewport -----

    SortedMap<Integer,Long> portfolioRows = viewportDefinition.getPortfolioRows();
    assertNotNull(portfolioRows);
    assertEquals(3, portfolioRows.size());

    Set<Integer> portfolioRowIds = portfolioRows.keySet();
    assertTrue(portfolioRowIds.contains(3));
    assertTrue(portfolioRowIds.contains(4));
    assertTrue(portfolioRowIds.contains(5));
    assertEquals(12345678L, (long) portfolioRows.get(3));
    assertEquals(12345679L, (long) portfolioRows.get(4));
    assertEquals(12345680L, (long) portfolioRows.get(5));

    Set<WebGridCell> portfolioDepGraphCells = viewportDefinition.getPortfolioDependencyGraphCells();
    assertNotNull(portfolioDepGraphCells);
    assertEquals(3, portfolioDepGraphCells.size());
    assertTrue(portfolioDepGraphCells.contains(new WebGridCell(3, 1)));
    assertTrue(portfolioDepGraphCells.contains(new WebGridCell(3, 2)));
    assertTrue(portfolioDepGraphCells.contains(new WebGridCell(4, 3)));

    Set<WebGridCell> portfolioFullConversionModeCells = viewportDefinition.getPortfolioFullConversionModeCells();
    assertNotNull(portfolioFullConversionModeCells);
    assertEquals(2, portfolioFullConversionModeCells.size());
    assertTrue(portfolioFullConversionModeCells.contains(new WebGridCell(5, 1)));
    assertTrue(portfolioFullConversionModeCells.contains(new WebGridCell(5, 3)));

    Set<WebGridCell> primitiveFullConversionModeCells = viewportDefinition.getPrimitiveFullConversionModeCells();
    assertNotNull(primitiveFullConversionModeCells);
    assertEquals(2, primitiveFullConversionModeCells.size());
    assertTrue(primitiveFullConversionModeCells.contains(new WebGridCell(6, 2)));
    assertTrue(primitiveFullConversionModeCells.contains(new WebGridCell(7, 3)));

    // primitive viewport -----

    SortedMap<Integer,Long> primitiveRows = viewportDefinition.getPrimitiveRows();
    assertNotNull(primitiveRows);
    assertEquals(3, primitiveRows.size());

    Set<Integer> primitiveRowIds = primitiveRows.keySet();
    assertTrue(primitiveRowIds.contains(5));
    assertTrue(primitiveRowIds.contains(6));
    assertTrue(primitiveRowIds.contains(7));
    assertEquals(12345681L, (long) primitiveRows.get(5));
    assertEquals(12345682L, (long) primitiveRows.get(6));
    assertEquals(12345683L, (long) primitiveRows.get(7));

    Set<WebGridCell> primitiveDepGraphCells = viewportDefinition.getPrimitiveDependencyGraphCells();
    assertNotNull(primitiveDepGraphCells);
    assertEquals(3, primitiveDepGraphCells.size());
    assertTrue(primitiveDepGraphCells.contains(new WebGridCell(5, 1)));
    assertTrue(primitiveDepGraphCells.contains(new WebGridCell(5, 2)));
    assertTrue(primitiveDepGraphCells.contains(new WebGridCell(7, 3)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void rowsMissing() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"marketDataType\": \"snapshot\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"portfolioViewport\": {" +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]" +
        "}" +
        "}";
    ViewportDefinition.fromJSON(json);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void depGraphCellsNotInRows() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"marketDataType\": \"live\", " +
        "\"portfolioViewport\": {" +
        "\"rowIds\": [3, 4, 5], " +
        "\"lastTimestamps\": [12345678, 12345679, 12345680], " +
        "\"dependencyGraphCells\": [[3, 1], [6, 2], [4, 3]]" +
        "}" +
        "}";
    ViewportDefinition.fromJSON(json);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void duplicateRowIds() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"marketDataType\": \"live\", " +
        "\"portfolioViewport\": {" +
        "\"rowIds\": [3, 3, 5], " +
        "\"lastTimestamps\": [12345678, 12345679, 12345680], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]" +
        "}" +
        "}";
    ViewportDefinition.fromJSON(json);
  }

  @Test
  public void noDepGraphCells() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"marketDataType\": \"live\", " +
        "\"portfolioViewport\": {" +
        "\"rowIds\": [3, 4, 5], " +
        "\"lastTimestamps\": [12345678, 12345679, 12345680]" +
        "}" +
        "}";
    ViewportDefinition definition = ViewportDefinition.fromJSON(json);
    Set<WebGridCell> dependencyGraphCells = definition.getPortfolioDependencyGraphCells();
    assertNotNull(dependencyGraphCells);
    assertTrue(dependencyGraphCells.isEmpty());
  }

  /**
   * definition must include {@link ViewportDefinition#VIEW_DEFINITION_NAME}
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noViewDefName() {
    String json = "{" +
        "\"marketDataType\": \"live\", " +
        "\"portfolioViewport\": {" +
        "\"rowIds\": [3, 4, 5], " +
        "\"lastTimestamps\": [12345678, 12345679, 12345680], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]" +
        "}" +
        "}";
    ViewportDefinition.fromJSON(json);
  }

  /**
   * valid market data types are here: {@link ViewportDefinition.MarketDataType}
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void invalidMarketDataType() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"marketDataType\": \"foo\", " +
        "\"portfolioViewport\": {" +
        "\"rowIds\": [3, 4, 5], " +
        "\"lastTimestamps\": [12345678, 12345679, 12345680], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]" +
        "}" +
        "}";
    ViewportDefinition.fromJSON(json);
  }

  /**
   * If the market data type is "snapshot" the JSON must include {@link ViewportDefinition#SNAPSHOT_ID}
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void snapshotMarketDataTypeButNoSnapshot() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"marketDataType\": \"snapshot\", " +
        "\"portfolioViewport\": {" +
        "\"rowIds\": [3, 4, 5], " +
        "\"lastTimestamps\": [12345678, 12345679, 12345680], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]" +
        "}" +
        "}";
    ViewportDefinition.fromJSON(json);
  }

  /**
   * snapshot ID must be parseable as a {@link UniqueId}
   * @see UniqueId#parse(String)
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void invalidSnapshotId() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"marketDataType\": \"snapshot\", " +
        "\"snapshotId\": \"not-a-UniqueId\", " +
        "\"portfolioViewport\": {" +
        "\"rowIds\": [3, 4, 5], " +
        "\"lastTimestamps\": [12345678, 12345679, 12345680], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]" +
        "}" +
        "}";
    ViewportDefinition.fromJSON(json);
  }
}
