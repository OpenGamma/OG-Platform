package com.opengamma.engine.function.dsl;

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
public class TargetSpecificationReference {

    private TargetSpecificationReference(){

    }

    static public TargetSpecificationReference originalTarget(){
      return new TargetSpecificationReference();
    }
}
