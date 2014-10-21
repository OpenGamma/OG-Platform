Trade Fee Converter
===================

Fees are added as attributes on the Trade object.

Multiple fees are added by grouping them in the following pattern: FEE_{number}_{PART}

Fees attributes are made up of four parts
 * 'DATE' in the format YYYY-MM-DD
 * 'CURRENCY' ISO currency code
 * 'AMOUNT' fee payable
 * 'DIRECTION' either 'PAY' or 'RECEIVE'

If the converter fails to create the InstrumentDefinition, null is returned