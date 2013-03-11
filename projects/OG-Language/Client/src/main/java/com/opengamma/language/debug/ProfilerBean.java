/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.debug;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.language.connector.Conditional;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.Profiler;

/**
 * Spring bean for configuring the debug profiler.
 */
public final class ProfilerBean implements InitializingBean {

  private long _outputPeriod = 60000L;
  private Conditional _condition = Conditional.isFalse();

  public void setOutputPeriod(final long outputPeriod) {
    ArgumentChecker.notNegativeOrZero(getOutputPeriod(), "outputPeriod");
    _outputPeriod = outputPeriod;
  }

  public long getOutputPeriod() {
    return _outputPeriod;
  }

  public void setCondition(final Conditional condition) {
    ArgumentChecker.notNull(condition, "condition");
    _condition = condition;
  }

  public Conditional getCondition() {
    return _condition;
  }

  public void setEnabled(final boolean enabled) {
    setCondition(Conditional.booleanConstant(enabled));
  }

  public boolean isEnabled() {
    return Conditional.holds(getCondition());
  }

  @Override
  public void afterPropertiesSet() {
    if (isEnabled()) {
      Profiler.enable(getOutputPeriod());
    }
  }

}
