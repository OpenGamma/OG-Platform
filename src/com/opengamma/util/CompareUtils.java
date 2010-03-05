package com.opengamma.util;

import org.apache.commons.lang.ObjectUtils;


/*
 * General static utility class for methods involved in making comparisons and equals operations more efficient.
 */
public class CompareUtils {
  /**
   * An equals check that can automatically handle nulls.  It returns true if both a and b are null
   * @param first value
   * @param second value
   * @return true if values are equal, or if both are null, false otherwise.
   * @deprecated Use {@link ObjectUtils#equals} instead.
   */
  @Deprecated
  public static boolean equalsWithNull(Object a, Object b) {
    return ObjectUtils.equals(a, b);
  }
  
  /**
   * This is a bit stupid because any properly written version of compareTo would deal with this already.
   * This implementation assumes that null should be at the 'bottom' of the natural order (null < any value).
   * @param <E> type of object we're comparing to.
   * @param a item that compareTo is called on.
   * @param b item that is being compared.
   * @return natural ordering as an integer (-ve when a < b, 0 when a == b, +ve when a > b).
   */
  public static <E> int compareWithNull(Comparable<E> a, E b) {
    if (a == null) {
      return b==null ? 0 : -1;
    } else if (b == null) {
      // we know a is not null as well.
      return 1;
    } else {
      return a.compareTo((E) b);
    }    
  }
  
  /**
   * Compare two doubles to see if they're 'closely' equal - this is because rounding errors can mean 
   * the results of double precision computations lead to small differences in results.  The definition
   * in this case of 'close' is that the difference is less than 10^-15 (1E-15).  If a different maximum
   * allowed difference is required, use the other version of this method.
   * @param a
   * @param b
   * @return true, if a and be are equal to within 10^-6, false otherwise
   */
  public static boolean closeEquals(double a, double b) {
    return (Math.abs(a - b) < 1E-15);
  }
  
  /**
   * Compare two doubles to see if they're 'closely' equal - this is because rounding errors can mean 
   * the results of double precision computations lead to small differences in results.  The definition
   * in this case of 'close' is that the absolute difference is less than the maxDifference parameter.  
   * If a different maximum
   * allowed difference is required, use the other version of this method.
   * @param a
   * @param b
   * @return true, if a and be are equal to within 10^-6, false otherwise
   */
  public static boolean closeEquals(double a, double b, double maxDifference) {
    return (Math.abs(a - b) < maxDifference);
  }
}
