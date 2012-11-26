/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.util.ArgumentChecker;

/**
 * Listens for changes to viewports and updates the logging configuration in the {@link ViewClient}.
 */
/* package */ class LoggingViewportListener implements ViewportListener {

  private final ViewClient _viewClient;
  /**
   *
   */
  private final Map<ValueSpecification, Integer> _refCounts = Maps.newHashMap();

  /* package */ LoggingViewportListener(ViewClient viewClient) {
    ArgumentChecker.notNull(viewClient, "viewClient");
    _viewClient = viewClient;
  }

  @Override
  public void viewportCreated(ViewportDefinition viewportDef, GridStructure gridStructure) {
    if (viewportDef.enableLogging()) {
      enableLogging(viewportDef, gridStructure);
    }
  }

  private void enableLogging(ViewportDefinition viewportDef, GridStructure gridStructure) {
    Set<ValueSpecification> newVals = Sets.newHashSet();
    for (GridCell cell : viewportDef) {
      ValueSpecification valueSpec = gridStructure.getValueSpecificationForCell(cell.getRow(), cell.getColumn());
      Integer refCount = _refCounts.get(valueSpec);
      Integer newRefCount;
      if (refCount == null) {
        newRefCount = 1;
        newVals.add(valueSpec);
      } else {
        newRefCount = refCount + 1;
      }
      _refCounts.put(valueSpec, newRefCount);
    }
    _viewClient.setMinimumLogMode(ExecutionLogMode.FULL, newVals);
  }

  @Override
  public void viewportUpdated(ViewportDefinition currentDef, ViewportDefinition newDef, GridStructure gridStructure) {
    if (currentDef.enableLogging() && newDef.enableLogging()) {
      // logging enabled for both versions of the viewport
      Set<GridCell> currentCells = Sets.newHashSet(currentDef.iterator());
      Set<GridCell> newCells = Sets.newHashSet(newDef.iterator());
      Set<GridCell> cellsRemoved = Sets.difference(currentCells, newCells);
      Set<GridCell> cellsAdded = Sets.difference(newCells, currentCells);
      enableLogging(valueSpecsFor(cellsAdded.iterator(), gridStructure));
      disableLogging(valueSpecsFor(cellsRemoved.iterator(), gridStructure));
    } else if (!currentDef.enableLogging() && newDef.enableLogging()) {
      // no logging for current viewport, increase log level for all cells
      enableLogging(valueSpecsFor(newDef.iterator(), gridStructure));
    } else if (currentDef.enableLogging() && !newDef.enableLogging()) {
      // reduce logging level for all cells in current viewport
      disableLogging(valueSpecsFor(currentDef.iterator(), gridStructure));
    }
  }

  private Set<ValueSpecification> valueSpecsFor(Iterator<GridCell> cellIterator, GridStructure gridStructure) {
    Set<ValueSpecification> valueSpecs = Sets.newHashSet();
    while (cellIterator.hasNext()) {
      GridCell cell = cellIterator.next();
      valueSpecs.add(gridStructure.getValueSpecificationForCell(cell.getRow(), cell.getColumn()));
    }
    return valueSpecs;
  }

  @Override
  public void viewportDeleted(ViewportDefinition viewportDef, GridStructure gridStructure) {
    if (viewportDef.enableLogging()) {
      disableLogging(valueSpecsFor(viewportDef.iterator(), gridStructure));
    }
  }

  /**
   * Increments the ref count for the value specs and returns the set of specs whose ref count was previously zero.
   * @param valuesSpecs The referenced specs
   */
  private void enableLogging(Set<ValueSpecification> valuesSpecs) {
    Set<ValueSpecification> newlyReferenced = Sets.newHashSet();
    for (ValueSpecification valueSpec : valuesSpecs) {
      Integer refCount = _refCounts.get(valueSpec);
      Integer newRefCount;
      if (refCount == null) {
        newRefCount = 1;
        newlyReferenced.add(valueSpec);
      } else {
        newRefCount = refCount + 1;
      }
      _refCounts.put(valueSpec, newRefCount);
    }
    _viewClient.setMinimumLogMode(ExecutionLogMode.FULL, newlyReferenced);
  }

  /**
   * Decrements the ref count for the value specs and returns the set of specs whose ref count is now zero.
   * @param valuesSpecs The referenced specs
   */
  private void disableLogging(Set<ValueSpecification> valuesSpecs) {
    Set<ValueSpecification> dereferenced = Sets.newHashSet();
    for (ValueSpecification valueSpec : valuesSpecs) {
      Integer refCount = _refCounts.get(valueSpec);
      Integer newRefCount;
      if (refCount != null) {
        if (refCount.equals(1)) {
          _refCounts.remove(valueSpec);
          dereferenced.add(valueSpec);
        } else {
          newRefCount = refCount - 1;
          _refCounts.put(valueSpec, newRefCount);
        }
      }
    }
    _viewClient.setMinimumLogMode(ExecutionLogMode.INDICATORS, dereferenced);
  }
}
