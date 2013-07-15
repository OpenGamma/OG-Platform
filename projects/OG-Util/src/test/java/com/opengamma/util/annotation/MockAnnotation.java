/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

/**
 * 
 */
@MockType
/*package*/class MockAnnotation {
  
  @MockField
  private String _name;
  
  @MockField
  private String _type;
  
  @MockConstructor
  public MockAnnotation() {
  }

  /**
   * Gets the name.
   * @return the name
   */
  @MockMethod
  public String getName() {
    return _name;
  }

  /**
   * Sets the name.
   * @param name  the name
   */
  public void setName(@MockParameter String name) {
    _name = name;
  }

  /**
   * Gets the type.
   * @return the type
   */
  @MockMethod
  public String getType() {
    return _type;
  }

  /**
   * Sets the type.
   * @param type  the type
   */
  @MockMethod
  public void setType(String type) {
    _type = type;
  }
  
}
