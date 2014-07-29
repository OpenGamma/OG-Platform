 Bond futures
============

This documentation described the bond futures used in the main currencies (USD, EUR, GBP, JPY).

Security
--------

The **BondFuturesSecurityDefinition** describes the fungible instruments.

There are two constructors, one from all the details (in particular dates) and a shorter version where some dates are computed from the details of the bond.

In the first version, the inputs are the notice dates (start and end), the delivery dates (start and end), the basket, the conversion factors and the notional.

In the second version, the delivery dates are computed from the notice dates by adding the standard settlement lag of the bond in the basket. This standard approach does not work all all currencies. In particular the standard settlement lag for German government bond is 3 days while the lag between the notice date and the delivery is 2 days for EUR bond futures.

Some examples of actual bond futures can be found in the class **BondFuturesDataSets**: Bobl futures (June 2014) and 5-Year U.S. Treasury Note Futures (FVU1 - September 2011).


Transaction
-----------

The **BondFuturesTransactionDefinition** describes a specific trade or transaction done on the fungible instrument described in the previous section.

The transaction is constructed from the underlying and the quantity, the trade date and the trade price details.