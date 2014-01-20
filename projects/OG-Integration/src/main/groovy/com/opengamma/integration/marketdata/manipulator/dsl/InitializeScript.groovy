/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl

/*
 * Adds methods to java.util.Number:
 *   getBp() - converts from basis points, i.e. returns num / 1e4
 *   getPc() - converts from percentage, i.e. returns num / 1e2
 *   getPercent() - same as getPc()
 * This allows users to specify quantities in DSL scripts like "25.bp" or "10.percent".
 * This works because all the methods in the analytics library expect their arguments to be normalized
 *
 * TODO are these safe?
 * 2 cases?
 *   1) fitted curves that take year fractions - not safe because they don't include day count?
 *   2) raw data. strips have a tenor so these are no use. but if they created a period or tenor they should be ok
 * Also included are date specification methods. The number is converted to its value in years:
 *   getD() - num / 365
 *   getM() - num / 12
 *   getY() - num
 *
 */
Number.metaClass.getBp = { -> delegate / 10000 }
Number.metaClass.getPc = { -> delegate / 100 }
Number.metaClass.getPercent = { -> delegate / 100 }

// TODO these aren't safe in their current form. need to be converted for use in analytics
// also need a different meaning for pre and post fitted curve data. create period or tenor
// use TimeCalculator for fitted curves? but would need to know the start
Number.metaClass.getD = { -> delegate / 365 }
Number.metaClass.getM = { -> delegate / 12 }
Number.metaClass.getY = { -> delegate }
