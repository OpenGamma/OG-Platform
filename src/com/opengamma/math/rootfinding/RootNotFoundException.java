package com.opengamma.math.rootfinding;

/**
 * 
 * @author emcleod
 */
public class RootNotFoundException extends RuntimeException {

  public RootNotFoundException() {
    super();
  }

  public RootNotFoundException(String s) {
    super(s);
  }

  public RootNotFoundException(String s, Throwable cause) {
    super(s, cause);
  }

  public RootNotFoundException(Throwable cause) {
    super(cause);
  }
}
