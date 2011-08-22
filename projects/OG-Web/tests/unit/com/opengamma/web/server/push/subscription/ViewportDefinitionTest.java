package com.opengamma.web.server.push.subscription;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.web.server.WebGridCell;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 *
 */
public class ViewportDefinitionTest {

  @Test
  public void isReadable() {
    ViewportDefinitionReader reader = new ViewportDefinitionReader();
    assertTrue(reader.isReadable(ViewportDefinition.class, null, null, null));
    // a random other class that shouldn't be readable
    assertFalse(reader.isReadable(ViewportRow.class, null, null, null));
  }

  @Test
  public void readFrom() throws IOException {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"rows\": [[3, 12345678], [4, 12345679], [5, 12345680]], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]}";
    ViewportDefinition viewportDefinition = ViewportDefinition.fromJSON(json);

    assertEquals("testViewDefName", viewportDefinition.getViewDefinitionName());
    assertEquals(UniqueId.of("Tst", "123"), viewportDefinition.getSnapshotId());

    List<ViewportRow> rows = viewportDefinition.getRows();
    assertNotNull(rows);
    assertEquals(3, rows.size());

    ViewportRow viewportRow1 = rows.get(0);
    assertNotNull(viewportRow1);
    assertEquals(3, viewportRow1.getRowId());
    assertEquals(12345678L, viewportRow1.getTimestamp());

    ViewportRow viewportRow2 = rows.get(1);
    assertNotNull(viewportRow2);
    assertEquals(4, viewportRow2.getRowId());
    assertEquals(12345679L, viewportRow2.getTimestamp());

    ViewportRow viewportRow3 = rows.get(2);
    assertNotNull(viewportRow3);
    assertEquals(5, viewportRow3.getRowId());
    assertEquals(12345680L, viewportRow3.getTimestamp());

    List<WebGridCell> depGraphCells = viewportDefinition.getDependencyGraphCells();
    assertNotNull(depGraphCells);
    assertEquals(3, depGraphCells.size());

    WebGridCell cell1 = depGraphCells.get(0);
    assertNotNull(cell1);
    assertEquals(3, cell1.getRowId());
    assertEquals(1, cell1.getColumnId());

    WebGridCell cell2 = depGraphCells.get(1);
    assertNotNull(cell2);
    assertEquals(3, cell2.getRowId());
    assertEquals(2, cell2.getColumnId());

    WebGridCell cell3 = depGraphCells.get(2);
    assertNotNull(cell1);
    assertEquals(4, cell3.getRowId());
    assertEquals(3, cell3.getColumnId());
  }

  // TODO dependencyGraphCells optional? or compulsory even when it's empty?
  // TODO sanity check that the dep graph cells are all in the specified rows?


  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void rowsMissing() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]}";
    ViewportDefinition.fromJSON(json);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void rowsEmpty() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"snapshotId\": \"Tst~123\", " +
        "\"rows\": [], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]}";
    ViewportDefinition.fromJSON(json);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void depGraphCellsNotInRows() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"rows\": [[3, 12345678], [4, 12345679], [5, 12345680]], " +
        "\"dependencyGraphCells\": [[3, 1], [6, 2], [4, 3]]}";
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

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void duplicateRowIds() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"rows\": [[3, 12345678], [3, 12345679], [5, 12345680]], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]}";
    ViewportDefinition.fromJSON(json);
  }

  @Test
  public void noDepGraphCells() {
    String json = "{" +
        "\"viewDefinitionName\": \"testViewDefName\", " +
        "\"rows\": [[3, 12345678], [4, 12345679], [5, 12345680]]}";
    ViewportDefinition definition = ViewportDefinition.fromJSON(json);
    List<WebGridCell> dependencyGraphCells = definition.getDependencyGraphCells();
    assertNotNull(dependencyGraphCells);
    assertTrue(dependencyGraphCells.isEmpty());
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void noViewDefName() {
    String json = "{" +
        "\"rows\": [[3, 12345678], [4, 12345679], [5, 12345680]], " +
        "\"dependencyGraphCells\": [[3, 1], [3, 2], [4, 3]]}";
    ViewportDefinition.fromJSON(json);
  }
}
