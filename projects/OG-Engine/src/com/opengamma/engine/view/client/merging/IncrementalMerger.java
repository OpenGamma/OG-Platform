/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

/**
 * An interface for incremental merging of results. Access is synchronised externally, so the implementation does not
 * need to be thread-safe. 
 * 
 * @param <T>  the type of the results
 */
public interface IncrementalMerger<T> {
  
  /**
   * Adds a new result.
   * 
   * @param result  the new result to merge
   */
  void merge(T result);
  
  /**
   * Retrieves a result representing everything that has been merged since this method was last called.
   * 
   * @return  a result representing everything that has been merged since this method was last called
   */
  T consume();
    
}
