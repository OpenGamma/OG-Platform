==================
Index CDS security
==================

A CDS index, represented by ``com.opengamma.financial.security.credit.IndexCDSSecurity``,
is a contract struck on an underlying index definition ``com.opengamma.financial.security.credit.IndexCDSDefinitionSecurity``

Trade date
==========

The date this trade occurred. Commonly referred to as T.

Buy Protection
==============

Does this contract represent the purchase of protection?

Underlying CDS Index
====================

Reference to the underlying index definition this contract is based on. e.g. ``MARKIT_RED_CODE~2I666VBB0``.

Index Tenor
===========

The tenor of the index (must be one of the listed tenors in the index definition) the contract refers to.

Notional
========

The notional size and currency of the contract.
