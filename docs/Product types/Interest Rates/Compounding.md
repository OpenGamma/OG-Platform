
# Compounding

Compounding applies when multiple calculation (or accrual) periods apply to a single payment period.

## Supported types
The following compounding methods are available. Other names by which the method may be known e.g. ISDA are listed alongside the OpenGamma terminology. In general the OpenGamma designation is the same as the name used in [FpML CompoundingMethod][1]

### None - OpenGamma/FpML/ISDA.

No compounding.

### Straight - OpenGamma/FpML (ISDA "Compounding").

Compounding includes the spread. For more details see [ISDA - "Alternative compounding methods for over-the-counter derivative transactions"][2] section 3.1.

### Flat - OpenGamma/FpML (ISDA "Flat Compounding", "Index Flat")

The spread is compounded only for the first calculation (accrual) period in a payment period. After the first calculation (accrual) period the spread is excluded. For more details see [ISDA - "Alternative compounding methods for over-the-counter derivative transactions"][2] section 3.2.

### Spread exclusive - OpenGamma/FpML (ISDA "Compounding with simple spread", aka "Compounding with spread excluded")
 
Floating rate interest is treated as compound interest and spread interest is treated as simple interest. For more details see [ISDA - "Alternative compounding methods for over-the-counter derivative transactions"n][2] section 3.3.

## Default behaviour.

Compounding methods may be optional when entering a trade, in that case None compounding is assumed to apply.

## How to Specify compounding on a trade.

 - IRS - See com.opengamma.financial.security.irs.InterestRateSwapLeg#setCompoundingMethod.

## Notes

Note that the above compounding methods (excluding "None") are identical in the absence of a spread.

  
  [1]: http://www.fpml.org/spec/fpml-5-3-5-tr-1/html/confirmation/schemaDocumentation/schemas/fpml-ird-5-3_xsd/complexTypes/Calculation/compoundingMethod.html
  [2]: http://www.isda.org/c_and_a/pdf/ISDA-Compounding-memo.pdf