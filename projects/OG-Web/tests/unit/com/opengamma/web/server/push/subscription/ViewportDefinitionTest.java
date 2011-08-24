package com.opengamma.web.server.push.subscription;

import com.opengamma.id.UniqueId;
import com.opengamma.web.server.WebGridCell;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 *
 */
public class ViewportDefinitionTest {

  // TODO this need to be moved
  @Test
  public void isReadable() {
    ViewportDefinitionReader reader = new ViewportDefinitionReader();
    assertTrue(reader.isReadable(ViewportDefinition.class, null, null, null));
    // a random other class that shouldn't be readable
    assertFalse(reader.isReadable(ViewportRow.class, null, null, null));
  }

  @Test
  public void fromJSON() throws IOException {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"portfolioViewport\": {" +
        "\"rows\": [[3, 12345678], [4, 12345679], [5, 12345680]], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]" +
        "}, " +
        "\"primitiveViewport\": {" +
        "\"rows\": [[5, 12345681], [6, 12345682], [7, 12345683]], " +
        "\"dependencyGraphCells\": [[5, 1], [5, 2], [7, 3]]" +
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

    List<WebGridCell> portfolioDepGraphCells = viewportDefinition.getPortfolioDependencyGraphCells();
    assertNotNull(portfolioDepGraphCells);
    assertEquals(3, portfolioDepGraphCells.size());

    WebGridCell prtCell1 = portfolioDepGraphCells.get(0);
    assertNotNull(prtCell1);
    assertEquals(3, prtCell1.getRowId());
    assertEquals(1, prtCell1.getColumnId());

    WebGridCell prtCell2 = portfolioDepGraphCells.get(1);
    assertNotNull(prtCell2);
    assertEquals(3, prtCell2.getRowId());
    assertEquals(2, prtCell2.getColumnId());

    WebGridCell prtCell3 = portfolioDepGraphCells.get(2);
    assertNotNull(prtCell1);
    assertEquals(4, prtCell3.getRowId());
    assertEquals(3, prtCell3.getColumnId());

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

    List<WebGridCell> primitiveDepGraphCells = viewportDefinition.getPrimitiveDependencyGraphCells();
    assertNotNull(primitiveDepGraphCells);
    assertEquals(3, primitiveDepGraphCells.size());

    WebGridCell prmCell1 = primitiveDepGraphCells.get(0);
    assertNotNull(prmCell1);
    assertEquals(5, prmCell1.getRowId());
    assertEquals(1, prmCell1.getColumnId());

    WebGridCell prmCell2 = primitiveDepGraphCells.get(1);
    assertNotNull(prmCell2);
    assertEquals(5, prmCell2.getRowId());
    assertEquals(2, prmCell2.getColumnId());

    WebGridCell prmCell3 = primitiveDepGraphCells.get(2);
    assertNotNull(prmCell1);
    assertEquals(7, prmCell3.getRowId());
    assertEquals(3, prmCell3.getColumnId());
  }

  // TODO dependencyGraphCells optional? or compulsory even when it's empty?
  // TODO sanity check that the dep graph cells are all in the specified rows?

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void rowsMissing() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
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
        "\"portfolioViewport\": {" +
        "\"rows\": [[3, 12345678], [4, 12345679], [5, 12345680]], " +
        "\"dependencyGraphCells\": [[3, 1], [6, 2], [4, 3]]" +
        "}" +
        "}";
    ViewportDefinition.fromJSON(json);
  }

  @Test
  public void nullSnapshotId() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"rows\": [[3, 12345678], [4, 12345679], [5, 12345680]], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]}";
    ViewportDefinition definition = ViewportDefinition.fromJSON(json);
    assertNull(definition.getSnapshotId());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void duplicateRowIds() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"portfolioViewport\": {" +
        "\"rows\": [[3, 12345678], [3, 12345679], [5, 12345680]], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]" +
        "}" +
        "}";
    ViewportDefinition.fromJSON(json);
  }

  @Test
  public void noDepGraphCells() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"rows\": [[3, 12345678], [4, 12345679], [5, 12345680]]}";
    ViewportDefinition definition = ViewportDefinition.fromJSON(json);
    List<WebGridCell> dependencyGraphCells = definition.getPortfolioDependencyGraphCells();
    assertNotNull(dependencyGraphCells);
    assertTrue(dependencyGraphCells.isEmpty());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noViewDefName() {
    String json = "{" +
        "\"rows\": [[3, 12345678], [4, 12345679], [5, 12345680]], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]}";
    ViewportDefinition.fromJSON(json);
  }
}
