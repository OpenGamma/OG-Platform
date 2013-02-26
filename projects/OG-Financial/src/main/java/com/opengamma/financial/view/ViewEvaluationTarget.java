/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.secondary.ThreeTenInstantFieldType;
import org.fudgemsg.types.secondary.ThreeTenLocalTimeFieldType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Target for functions which can create and manipulate a view client to perform one or more evaluations.
 */
public class ViewEvaluationTarget extends TempTarget {

  private final ViewDefinition _viewDefinition;
  private String _firstValuationDate = "";
  private boolean _includeFirstValuationDate = true;
  private String _lastValuationDate = "";
  private boolean _includeLastValuationDate = true;
  private ZoneId _timeZone = ZoneOffset.UTC;
  private LocalTime _valuationTime = LocalTime.NOON;
  private Instant _correction;

  /**
   * Creates a new target with an empty/default view definition.
   *
   * @param user the market data user, for example taken from the parent/containing view definition, not null
   */
  public ViewEvaluationTarget(final UserPrincipal user) {
    super();
    _viewDefinition = new ViewDefinition("Temp", user);
  }

  protected ViewEvaluationTarget(final FudgeDeserializer deserializer, final FudgeMsg message) {
    super(deserializer, message);
    _viewDefinition = deserializer.fieldValueToObject(ViewDefinition.class, message.getByName("viewDefinition"));
    _firstValuationDate = message.getString("firstValuationDate");
    _includeFirstValuationDate = message.getBoolean("includeFirstValuationDate");
    _lastValuationDate = message.getString("lastValuationDate");
    _includeLastValuationDate = message.getBoolean("includeLastValuationDate");
    _timeZone = ZoneId.of(message.getString("timeZone"));
    _valuationTime = message.getValue(LocalTime.class, "valuationTime");
    _correction = message.getValue(Instant.class, "correction");
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
    ArgumentChecker.notNull(firstValuationDate, "firstValuationDate");
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
    ArgumentChecker.notNull(lastValuationDate, "lastValuationDate");
    _lastValuationDate = lastValuationDate;
  }

  public boolean isIncludeLastValuationDate() {
    return _includeLastValuationDate;
  }

  public void setIncludeLastValuationDate(final boolean includeLastValuationDate) {
    _includeLastValuationDate = includeLastValuationDate;
  }

  public ZoneId getTimeZone() {
    return _timeZone;
  }

  public void setTimeZone(final ZoneId timeZone) {
    ArgumentChecker.notNull(timeZone, "timeZone");
    _timeZone = timeZone;
  }

  public LocalTime getValuationTime() {
    return _valuationTime;
  }

  public void setValuationTime(final LocalTime valuationTime) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    _valuationTime = valuationTime;
  }

  public Instant getCorrection() {
    return _correction;
  }

  public void setCorrection(final Instant correction) {
    _correction = correction;
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
        || (isIncludeLastValuationDate() != other.isIncludeLastValuationDate())
        || !getTimeZone().equals(other.getTimeZone())
        || !getValuationTime().equals(other.getValuationTime())
        || !ObjectUtils.equals(getCorrection(), other.getCorrection())) {
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
    for (final ViewCalculationConfiguration otherConfig : otherView.getAllCalculationConfigurations()) {
      final ViewCalculationConfiguration newConfig = newView.getCalculationConfiguration(otherConfig.getName());
      if (newConfig == null) {
        myView.addViewCalculationConfiguration(newConfig);
      } else {
        newConfig.addSpecificRequirements(otherConfig.getSpecificRequirements());
      }
    }
    final ViewEvaluationTarget union = new ViewEvaluationTarget(newView);
    union.setCorrection(getCorrection());
    union.setFirstValuationDate(getFirstValuationDate());
    union.setIncludeFirstValuationDate(isIncludeFirstValuationDate());
    union.setIncludeLastValuationDate(isIncludeLastValuationDate());
    union.setLastValuationDate(getLastValuationDate());
    union.setTimeZone(getTimeZone());
    union.setValuationTime(getValuationTime());
    return union;
  }

  // TempTarget

  @Override
  protected boolean equalsImpl(final Object o) {
    final ViewEvaluationTarget other = (ViewEvaluationTarget) o;
    return getFirstValuationDate().equals(other.getFirstValuationDate())
        && (isIncludeFirstValuationDate() == other.isIncludeFirstValuationDate())
        && getLastValuationDate().equals(other.getLastValuationDate())
        && (isIncludeLastValuationDate() == other.isIncludeLastValuationDate())
        && getTimeZone().equals(other.getTimeZone())
        && getValuationTime().equals(other.getValuationTime())
        && getViewDefinition().equals(other.getViewDefinition())
        && ObjectUtils.equals(getCorrection(), other.getCorrection());
  }

  @Override
  protected int hashCodeImpl() {
    int hc = 1;
    hc += (hc << 4) + getViewDefinition().hashCode();
    hc += (hc << 4) + getFirstValuationDate().hashCode();
    hc += (hc << 4) + (isIncludeFirstValuationDate() ? -1 : 0);
    hc += (hc << 4) + getLastValuationDate().hashCode();
    hc += (hc << 4) + (isIncludeLastValuationDate() ? -1 : 0);
    hc += (hc << 4) + getTimeZone().hashCode();
    hc += (hc << 4) + getValuationTime().hashCode();
    hc += (hc << 4) + ObjectUtils.hashCode(getCorrection());
    return hc;
  }

  @Override
  protected void toFudgeMsgImpl(final FudgeSerializer serializer, final MutableFudgeMsg message) {
    super.toFudgeMsgImpl(serializer, message);
    serializer.addToMessageWithClassHeaders(message, "viewDefinition", null, getViewDefinition(), ViewDefinition.class);
    message.add("firstValuationDate", null, FudgeWireType.STRING, getFirstValuationDate());
    message.add("includeFirstValuationDate", null, FudgeWireType.BOOLEAN, isIncludeFirstValuationDate());
    message.add("lastValuationDate", null, FudgeWireType.STRING, getLastValuationDate());
    message.add("includeLastValuationDate", null, FudgeWireType.BOOLEAN, isIncludeLastValuationDate());
    message.add("timeZone", null, FudgeWireType.STRING, getTimeZone().getId());
    message.add("valuationTime", null, ThreeTenLocalTimeFieldType.INSTANCE, getValuationTime());
    if (getCorrection() != null) {
      message.add("correction", null, ThreeTenInstantFieldType.INSTANCE, getCorrection());
    }
  }

  public static ViewEvaluationTarget fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return new ViewEvaluationTarget(deserializer, message);
  }

}
