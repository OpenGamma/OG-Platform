/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.livedata.UserPrincipal;

/**
 * Target for functions which can create and manipulate a view client to perform one or more evaluations.
 */
public class ViewEvaluationTarget extends TempTarget {

  private final ViewDefinition _viewDefinition;
  private String _firstValuationDate = "";
  private boolean _includeFirstValuationDate = true;
  private String _lastValuationDate = "";
  private boolean _includeLastValuationDate = true;

  /**
   * Creates a new target with an empty/default view definition.
   *
   * @param user the market data user, for example taken from the parent/containing view definition, not null
   */
  public ViewEvaluationTarget(final UserPrincipal user) {
    super();
    _viewDefinition = new ViewDefinition("Temp", user);
  }

  private ViewEvaluationTarget(final ViewDefinition viewDefinition) {
    super();
    _viewDefinition = viewDefinition;
  }

  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  public String getFirstValuationDate() {
    return _firstValuationDate;
  }

  public void setFirstValuationDate(final String firstValuationDate) {
    _firstValuationDate = firstValuationDate;
  }

  public boolean isIncludeFirstValuationDate() {
    return _includeFirstValuationDate;
  }

  public void setIncludeFirstValuationDate(final boolean includeFirstValuationDate) {
    _includeFirstValuationDate = includeFirstValuationDate;
  }

  public String getLastValuationDate() {
    return _lastValuationDate;
  }

  public void setLastValuationDate(final String lastValuationDate) {
    _lastValuationDate = lastValuationDate;
  }

  public boolean isIncludeLastValuationDate() {
    return _includeLastValuationDate;
  }

  public void setIncludeLastValuationDate(final boolean includeLastValuationDate) {
    _includeLastValuationDate = includeLastValuationDate;
  }

  /**
   * Creates a target which is the union of this and another. The other target must have compatible valuation parameters.
   *
   * @param other the other target, not null
   * @return null if the other target is not compatible, otherwise a new instance containing a view definition that is the union of the two participant view definitions
   */
  public ViewEvaluationTarget union(final ViewEvaluationTarget other) {
    // Check the valuation parameters are compatible
    if (!getFirstValuationDate().equals(other.getFirstValuationDate())
        || (isIncludeFirstValuationDate() != other.isIncludeFirstValuationDate())
        || !getLastValuationDate().equals(other.getLastValuationDate())
        || (isIncludeLastValuationDate() != other.isIncludeLastValuationDate())) {
      return null;
    }
    // Check the basic view definitions are compatible
    final ViewDefinition myView = getViewDefinition();
    final ViewDefinition otherView = other.getViewDefinition();
    if (!ObjectUtils.equals(myView.getDefaultCurrency(), otherView.getDefaultCurrency())
        || !ObjectUtils.equals(myView.getMarketDataUser(), otherView.getMarketDataUser())
        || !ObjectUtils.equals(myView.getMaxDeltaCalculationPeriod(), otherView.getMaxDeltaCalculationPeriod())
        || !ObjectUtils.equals(myView.getMaxFullCalculationPeriod(), otherView.getMaxFullCalculationPeriod())
        || !ObjectUtils.equals(myView.getMinDeltaCalculationPeriod(), otherView.getMinDeltaCalculationPeriod())
        || !ObjectUtils.equals(myView.getMinFullCalculationPeriod(), otherView.getMinFullCalculationPeriod())
        || !ObjectUtils.equals(myView.getName(), otherView.getName())
        || !ObjectUtils.equals(myView.getPortfolioId(), otherView.getPortfolioId())
        || !ObjectUtils.equals(myView.getResultModelDefinition(), otherView.getResultModelDefinition())) {
      return null;
    }
    // Check the calc configs are compatible
    for (final ViewCalculationConfiguration myConfig : myView.getAllCalculationConfigurations()) {
      final ViewCalculationConfiguration otherConfig = otherView.getCalculationConfiguration(myConfig.getName());
      if (!ObjectUtils.equals(myConfig.getDefaultProperties(), otherConfig.getDefaultProperties())
          || !ObjectUtils.equals(myConfig.getDeltaDefinition(), otherConfig.getDeltaDefinition())
          || !ObjectUtils.equals(myConfig.getResolutionRuleTransform(), otherConfig.getResolutionRuleTransform())) {
        // Configs aren't compatible
        return null;
      }
    }
    // Create a new view definition that is the union of all calc configs
    final ViewDefinition newView = myView.copyWith(myView.getName(), myView.getPortfolioId(), myView.getMarketDataUser());
    for (final ViewCalculationConfiguration otherConfig : myView.getAllCalculationConfigurations()) {
      final ViewCalculationConfiguration newConfig = newView.getCalculationConfiguration(otherConfig.getName());
      if (newConfig == null) {
        myView.addViewCalculationConfiguration(newConfig);
      } else {
        newConfig.addSpecificRequirements(otherConfig.getSpecificRequirements());
      }
    }
    return new ViewEvaluationTarget(newView);
  }

  // ViewEvaluationTarget

  @Override
  protected boolean equalsImpl(final Object o) {
    final ViewEvaluationTarget other = (ViewEvaluationTarget) o;
    return getViewDefinition().equals(other.getViewDefinition())
        && getFirstValuationDate().equals(other.getFirstValuationDate())
        && (isIncludeFirstValuationDate() == other.isIncludeFirstValuationDate())
        && getLastValuationDate().equals(other.getLastValuationDate())
        && (isIncludeLastValuationDate() == other.isIncludeLastValuationDate());
  }

  @Override
  protected int hashCodeImpl() {
    int hc = 1;
    hc += (hc << 4) + getViewDefinition().hashCode();
    hc += (hc << 4) + getFirstValuationDate().hashCode();
    hc += (hc << 4) + (isIncludeFirstValuationDate() ? -1 : 0);
    hc += (hc << 4) + getLastValuationDate().hashCode();
    hc += (hc << 4) + (isIncludeLastValuationDate() ? -1 : 0);
    return hc;
  }

}
