/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

/**
 * Abstraction to provide a hash and equality test based on the class.
 * 
 * @author Andrew Griffin
 */
/* package */ abstract class StatelessDayCount implements DayCount {
  
  @Override
  public boolean equals (final Object o) {
    if (o == null) return false;
    if (o == this) return true;
    return getClass ().equals (o.getClass ());
  }
  
  @Override
  public int hashCode () {
    return getClass ().hashCode ();
  }
  
}
