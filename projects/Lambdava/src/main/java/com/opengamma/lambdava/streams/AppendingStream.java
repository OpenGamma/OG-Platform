/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

class AppendingStream<T> extends AbstractStream<T> {

  private StreamI<T> _prefix;
  private StreamI<T> _suffix;

  protected AppendingStream(StreamI<T> prefix, StreamI<T> suffix) {
    _prefix = prefix;
    _suffix = suffix;
  }

  @Override
  public T head() {
    if (_prefix.isEmpty())
      return _suffix.head();
    else
      return _prefix.head();
  }

  @Override
  public StreamI<T> rest() {
    if (_prefix.isEmpty()) {
      return _suffix.rest();
    } else {
//      StreamI<T> rest = _prefix.rest();
      return new AppendingStream<T>(_prefix.rest(), _suffix);
//      if(rest.isEmpty()){        
//        return _suffix;
//      }else{
//        return new AppendingStream<T>(_prefix.rest(), _suffix);
//      }
    }
  }

  @Override
  public boolean isEmpty() {
    return _prefix.isEmpty() && _suffix.isEmpty();
  }

  @Override
  public int count() {
    return _prefix.count() + _suffix.count();
  }
  
  

  @Override
  public String toString() {
    return '[' +
      _prefix.toString().replaceFirst("^\\[", "").replaceFirst("\\]$", "") +
      ", " + 
      _suffix.toString().replaceFirst("^\\[", "").replaceFirst("\\]$", "") +
      ']';
  }
}
