/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Maps particular columns to and from the combination of parameters
 * that make up the appropriate lookup components for that model.
 * <p/>
 * Columns are:
 * <dl>
 *   <dt>0</dt>
 *   <dd>The trade definition column</dd>
 *   <dt>[1..n1]</dt>
 *   <dd>The columns in order for the first calculation configuration.</dd>
 *   <dt>(n1..n2]</dt>
 *   <dd>The columns in order for the second calculation configuration.</dd>
 * </dl>
 * And the pattern continues.
 *
 * @author kirk
 */
public class ViewerColumnModel {
  private final List<CalculationConfigurationRange> _calcConfigRanges = new ArrayList<CalculationConfigurationRange>();
  private int _columnCount;

  public ViewerColumnModel() {
  }

  /**
   * @param viewDefinition  the view definition
   */
  public void init(ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(viewDefinition, "View definition");
    
    // 0th column is for the trade definition.
    int currColumn = 1;
    for (ViewCalculationConfiguration calcConfiguration : viewDefinition.getAllCalculationConfigurations()) {
      CalculationConfigurationRange range = new CalculationConfigurationRange();
      _calcConfigRanges.add(range);
      range.setConfigName(calcConfiguration.getName());
      range.setStartColumn(currColumn);

      for (Pair<String, ValueProperties> requirementName : calcConfiguration.getAllPortfolioRequirements()) {
        range.getColumnRequirementNames().add(requirementName.getFirst());
        currColumn++;
      }
    }
    _columnCount = currColumn;
  }

  private CalculationConfigurationRange getRange(int column) {
    if (column <= 0) {
      return null;
    }
    for (CalculationConfigurationRange range : _calcConfigRanges) {
      if (column >= (range.getStartColumn() + range.getColumnRequirementNames().size())) {
        continue;
      }
      return range;
    }
    return null;
  }

  public String getCalculationConfigurationName(int column) {
    CalculationConfigurationRange range = getRange(column);
    if (range == null) {
      return null;
    }
    return range.getConfigName();
  }

  public String getRequirementName(int column) {
    CalculationConfigurationRange range = getRange(column);
    if (range == null) {
      return null;
    }
    int index = column - range.getStartColumn();
    return range.getColumnRequirementNames().get(index);
  }

  public int getColumnCount() {
    return _columnCount;
  }

  private static class CalculationConfigurationRange {
    private String _configName;
    private int _startColumn;
    private final List<String> _columnRequirementNames = new ArrayList<String>();

    public String getConfigName() {
      return _configName;
    }

    public void setConfigName(final String configName) {
      _configName = configName;
    }

    public int getStartColumn() {
      return _startColumn;
    }

    public void setStartColumn(final int startColumn) {
      _startColumn = startColumn;
    }

    public List<String> getColumnRequirementNames() {
      return _columnRequirementNames;
    }
  }

}
