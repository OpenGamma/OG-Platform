/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class LoggingViewportListenerTest {

  private final GridCell _cell1 = new GridCell(0, 1, TypeFormatter.Format.CELL);
  private final GridCell _cell2 = new GridCell(0, 2, TypeFormatter.Format.CELL);
  private final GridCell _cell3 = new GridCell(0, 3, TypeFormatter.Format.CELL);
  private final List<GridCell> _cells12 = Lists.newArrayList(_cell1, _cell2);
  private final List<GridCell> _cells23 = Lists.newArrayList(_cell2, _cell3);
  private final GridStructure _gridStructure = gridStructure(_cell1, _cell2, _cell3);

  /**
   * creates a viewport with logging enabled and then deletes it
   */
  @Test
  public void createDeleteWithLogging() {
    final ViewClient viewClient = mock(ViewClient.class);
    final LoggingViewportListener listener = new LoggingViewportListener(viewClient);
    final ViewportDefinition viewportDef = viewportDef(true, _cells12);
    listener.viewportCreated(viewportDef, _gridStructure);
    verify(viewClient).setMinimumLogMode(ExecutionLogMode.FULL, resultSpecs(_cells12));
    listener.viewportDeleted(viewportDef, _gridStructure);
    verify(viewClient).setMinimumLogMode(ExecutionLogMode.INDICATORS, resultSpecs(_cells12));
  }

  /**
   * creates a deletes a viewport with no logging enabled
   */
  @Test
  @SuppressWarnings("unchecked")
  public void createDeleteNoLogging() {
    final ViewClient viewClient = mock(ViewClient.class);
    final LoggingViewportListener listener = new LoggingViewportListener(viewClient);
    final ViewportDefinition viewportDef = viewportDef(false, _cells12);
    listener.viewportCreated(viewportDef, _gridStructure);
    listener.viewportDeleted(viewportDef, _gridStructure);
    verify(viewClient, never()).setMinimumLogMode(any(ExecutionLogMode.class), anySet());
  }

  @Test
  public void createUpdateDeleteWithLogging() {
    final ViewClient viewClient = mock(ViewClient.class);
    final LoggingViewportListener listener = new LoggingViewportListener(viewClient);
    final ViewportDefinition viewportDef1 = viewportDef(true, _cells12);
    listener.viewportCreated(viewportDef1, _gridStructure);
    verify(viewClient).setMinimumLogMode(ExecutionLogMode.FULL, resultSpecs(_cells12));

    final ViewportDefinition viewportDef2 = viewportDef(true, _cells23);
    listener.viewportUpdated(viewportDef1, viewportDef2, _gridStructure);
    verify(viewClient).setMinimumLogMode(ExecutionLogMode.INDICATORS, resultSpecs(_cell1));
    verify(viewClient).setMinimumLogMode(ExecutionLogMode.FULL, resultSpecs(_cell3));

    listener.viewportDeleted(viewportDef2, _gridStructure);
    verify(viewClient).setMinimumLogMode(ExecutionLogMode.INDICATORS, resultSpecs(_cells23));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void createUpdateDeleteNoLogging() {
    final ViewClient viewClient = mock(ViewClient.class);
    final LoggingViewportListener listener = new LoggingViewportListener(viewClient);
    final ViewportDefinition viewportDef1 = viewportDef(false, _cells12);
    listener.viewportCreated(viewportDef1, _gridStructure);
    final ViewportDefinition viewportDef2 = viewportDef(false, _cells23);
    listener.viewportUpdated(viewportDef1, viewportDef2, _gridStructure);
    listener.viewportDeleted(viewportDef2, _gridStructure);
    verify(viewClient, never()).setMinimumLogMode(any(ExecutionLogMode.class), anySet());
  }

  @Test
  public void twoViewportsWithLoggingAndOverlappingCells() {
    final ViewClient viewClient = mock(ViewClient.class);
    final LoggingViewportListener listener = new LoggingViewportListener(viewClient);
    final ViewportDefinition viewportDef1 = viewportDef(true, _cells12);
    final ViewportDefinition viewportDef2 = viewportDef(true, _cells23);
    listener.viewportCreated(viewportDef1, _gridStructure);
    verify(viewClient).setMinimumLogMode(ExecutionLogMode.FULL, resultSpecs(_cell1, _cell2));
    listener.viewportCreated(viewportDef2, _gridStructure);
    verify(viewClient).setMinimumLogMode(ExecutionLogMode.FULL, resultSpecs(_cell3));
    listener.viewportDeleted(viewportDef1, _gridStructure);
    verify(viewClient).setMinimumLogMode(ExecutionLogMode.INDICATORS, resultSpecs(_cell1));
    listener.viewportDeleted(viewportDef2, _gridStructure);
    verify(viewClient).setMinimumLogMode(ExecutionLogMode.INDICATORS, resultSpecs(_cell2, _cell3));
  }

  //-------------------------------------------------------------------------
  private static ViewportDefinition viewportDef(final boolean enableLogging, final List<GridCell> cells) {
    return new ArbitraryViewportDefinition(0, cells, enableLogging);
  }

  private static GridStructure gridStructure(final GridCell... cells) {
    return gridStructure(Arrays.asList(cells));
  }

  private static GridStructure gridStructure(final List<GridCell> cells) {
    final GridStructure mock = mock(GridStructure.class);
    for (final GridCell cell : cells) {
      when(mock.getValueSpecificationForCell(cell.getRow(), cell.getColumn())).thenReturn(target(cell));
    }
    return mock;
  }

  private static Pair<String, ValueSpecification> target(final GridCell cell) {
    final int row = cell.getRow();
    final int col = cell.getColumn();
    final ComputationTargetSpecification target = new ComputationTargetSpecification(ComputationTargetType.POSITION,
                                                                               UniqueId.of("Cell", row + "," + col));
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "fnName").get();
    return Pairs.of("Default", new ValueSpecification("valueName(" + row + "," + col + ")", target, properties));
  }

  private Set<Pair<String, ValueSpecification>> resultSpecs(final GridCell... cells) {
    return resultSpecs(Arrays.asList(cells));
  }

  private Set<Pair<String, ValueSpecification>> resultSpecs(final List<GridCell> cells) {
    final Set<Pair<String, ValueSpecification>> specs = Sets.newHashSet();
    for (final GridCell cell : cells) {
      specs.add(target(cell));
    }
    return specs;
  }
}
