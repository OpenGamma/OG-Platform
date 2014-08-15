===========================================
How to add/amend/delete trades with the API
===========================================


Introduction
============

This document outlines the basic code required to load trades into an
OpenGamma instance where the source of trades is a feed from another
system. As the feed is likely to send a variety of trades (new, amended
or cancelled), this document outlines possible approaches for each type.
In order to be generally applicable to any sort of feed this document
makes no assumptions about the structures of the trades sent. Instead a
generic type is used – it should be easily adaptable to any input types.

This document only considers the loading of trades into a flat portfolio
structure. The principles for a hierarchical structure are the same but
would require additional navigation of the hierarchy.

In OpenGamma, portfolios contain positions. Each position contains a
link to a security and optionally a link to one or more trades. Each
trade then contains a link to the (same) security. In order to load
trades access is required to the PortfolioMaster, the SecurityMaster and
the PositionMaster (which holds both positions and trades).


Listed vs OTC Securities
========================

OpenGamma’s security model does not distinguish between listed and OTC
securities. This means that every trade and position needs to reference
a security from the security master. For listed securities, it is
assumed that appropriate security definitions have already been loaded
into the security master with an appropriate set of external id bundles.
This means that when loading associated trades or positions there is no
need to create additional securities.


Work with the API
=================

Connecting to the OpenGamma instance
------------------------------------

It is possible to connect to a remote OpenGamma instance and get access
to the masters using the following code:


.. code:: java

     	String serverLocation = "http://hostname:port"; // e.g localhost:8080
 	ToolContext context = ToolContextUtils.getToolContext(serverLocation,
 	ToolContext.class); PortfolioMaster portfolioMaster =
 	context.getPortfolioMaster();
 	SecurityMaster securityMaster = context.getSecurityMaster();
 	PositionMaster positionMaster = context.getPositionMaster();


Loading New Trades
------------------
Trades that the feed mechanism has determined are new can be handled as follows (for a generic trade type T):

.. code:: java

    public void insertTrade(T trade) {

        String portfolioName =  _tradeAdapter.determinePortfolioForTrade(trade);
        ManageablePortfolio portfolio = createOrRetrievePortfolio(portfolioName);
        ExternalIdBundle idBundle = createOrRetrieveSecurityForTrade(trade);

        ManageablePosition position = createOrRetrievePositionForTrade(portfolio, idBundle); 
        ManageableTrade manageableTrade =  _tradeAdapter.buildManageableTrade(trade);
        position.addTrade(manageableTrade);

        boolean isNewPosition = position.getUniqueId() == null;

        // Do position management as required
        BigDecimal tradeQuantity = manageableTrade.getQuantity(); 
        BigDecimal quantity = position.getQuantity().add(tradeQuantity);
        position.setQuantity(quantity);

        //This will persist both the position and the associated trades
         _positionMaster.add(new PositionDocument(position));

        //If we didn't have the position before we need to update the portfolio so it is now aware of it
        if (isNewPosition) {
            //By adding the position to the master, it gets assigned an id
            //so we know it will be non-null now
            portfolio.getRootNode().addPosition(position.getUniqueId());
            addPositionToPortfolio(position, portfolio);
        }

    }

    private void addPositionToPortfolio(

        ManageablePosition position, ManageablePortfolio portfolio) {
        
        portfolio.getRootNode().addPosition(position.getUniqueId());
        _portfolioMaster.add(new PortfolioDocument(portfolio));

    }

    private ManageablePortfolio createOrRetrievePortfolio(String portfolioName) {
        
        ManageablePortfolio existingPortfolio = findCurrentPortfolio(portfolioName); 
        return existingPortfolio != null ?
            existingPortfolio :
            new ManageablePortfolio(portfolioName);

    }

    private ManageablePortfolio findCurrentPortfolio(String portfolioName) {

        PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
        searchRequest.setName(portfolioName);
        PortfolioSearchResult result =  _portfolioMaster.search(searchRequest);

        List<ManageablePortfolio> portfolios = result.getPortfolios();

        int size = portfolios.size();

        return size == 0 ? null : portfolios.get(0);

    }

    private ExternalIdBundle createOrRetrieveSecurityForTrade(T trade) {

        if ( _tradeAdapter.isTradeUsingListedProduct(trade)) {
    
            //We don't need to insert a security, just reference it 
            return _tradeAdapter.determineSecurityIdForTrade(trade);
        
        } else {
    
            //Note that to allow editing an id should be created from the
            //trade (perhaps the trade id), such that the security can be looked
            //up or altered if the trade is amended

            ManageableSecurity security = _tradeAdapter.buildSecurityForTrade(trade);  
            _securityMaster.add(new SecurityDocument(security));
        
            return security.getExternalIdBundle();
        }
    }

    private ManageablePosition createOrRetrievePositionForTrade(ManageablePortfolio portfolio, ExternalIdBundle idBundle) {

        // If we knew this was an OTC trade then we could skip the search
        PositionSearchRequest request = new PositionSearchRequest();
        request.setPositionObjectIds(portfolio.getRootNode().getPositionIds());
        request.setSecurityIdSearch(ExternalIdSearch.of(idBundle.getExternalIds()));
        PositionSearchResult result =  _positionMaster.search(request);
        ManageablePosition found = result.getFirstPosition();

        return found != null ? found : createNewPosition(idBundle);

    }

    private ManageablePosition createNewPosition(ExternalIdBundle idBundle)
    {

        ManageablePosition position = new ManageablePosition();
        position.setSecurityLink(new ManageableSecurityLink(idBundle));
        position.setQuantity(BigDecimal.ZERO);

        return position;

    }


The trade adapter contains the methods that are specific to the trade
format being used. The interface is:

.. code:: java

    public interface TradeAdapter<T> {

    /**
     * Uses data from the trade object to determine what
     * portfolio it should be inserted into.
     *
     * @param trade the trade being inserted
     * @return the name of the portfolio to insert into, not null
     */
    String determinePortfolioForTrade(T trade);

    /**
     * Is the trade an OTC or using a listed security.
     * @param trade the trade being inserted
     * @return true if the trade is using a listed security
     */
    boolean isTradeUsingListedProduct(T trade);

    /**
     * Determine the external id for this trade. Only used for
     * trades using listed securities.
     *
     * @param trade the trade being inserted
     * @return the required security id bundle, not null
     */
    ExternalIdBundle determineSecurityIdForTrade(T trade);

    /**
     * Build a security object for the OTC trade. This method should
     * ensure that the security id is determined from some unique
     * characteristics of the trade (e.g. trade id) such that it can be
     * located if the trade is updated.
     *
     * @param trade the trade being inserted
     * @return the security for the OTC trade, not null
     */
    ManageableSecurity buildSecurityForTrade(T trade);


    /**
     * Build a ManageableTrade object from the passed trade. Implementations
     * should ensure that they call {@link ManageableTrade#setProviderId(ExternalId)}
     * with the trade id produced by {@link #getExternalId(Object)} so that it can
     * be located if amended.
     * 
     * @param trade the trade being inserted
     * @return a ManageableTrade object, not null
     */
    ManageableTrade buildManageableTrade(T trade);

    /**
     * Get the identifier for this trade. This should be constant
     * across different versions of the trade as it will be used
     * to track amendments.
     * 
     * @param trade the trade being inserted
     * @return an external id for the trade, not null
     */
    ExternalId getExternalId(T trade);

    }

Amending existing trades
------------------------

Trades that the feed mechanism has determined are amendments to existing
ones can be handled as follows (again for a generic trade type T):

.. code:: java

    public void amendTrade(T trade) {

        String portfolioName =  _tradeAdapter.determinePortfolioForTrade(trade);

        ManageablePortfolio portfolio = findCurrentPortfolio(portfolioName);

        if (portfolio == null) {

            throw new IllegalStateException("Portfolio not found"); 
            // Or handle portfolio not found some other way

        }

        // Has security for the trade changed
        ExternalIdBundle idBundle = retrieveSecurityForTrade(trade);
        ManageableTrade previousTrade = findTradeByExternalId(trade);
        ManageablePosition previousPosition = findPosition(previousTrade.getParentPositionId());

        ManageableTrade manageableTrade =  _tradeAdapter.buildManageableTrade(trade); 
        boolean isNewPosition = false;

        if ( _tradeAdapter.isTradeUsingListedProduct(trade)) {

            if (previousTrade.getSecurityLink().getExternalId().equals(idBundle)) {

                //We don't need to worry about switching security details,
                //Just create a new version of the trade and add it in
                previousPosition.removeTrade(previousTrade);
                previousPosition.addTrade(manageableTrade);

                //position manage
                BigDecimal newQuantity = previousPosition.getQuantity()
                    .subtract(previousTrade.getQuantity())
                    .add(manageableTrade.getQuantity());
                previousPosition.setQuantity(newQuantity);

            } else {

                //create correct position if needed (and update portfolio positions)
                //create trade in correct position
                //add position quantity
                //remove trade from wrong position
                //add position quantity

                ManageablePosition position = createOrRetrievePositionForTrade(portfolio, idBundle);

                isNewPosition = position.getUniqueId() == null;

                previousPosition.removeTrade(previousTrade);

                BigDecimal oldPosQuantity = previousPosition.getQuantity().subtract(previousTrade.getQuantity());

                previousPosition.setQuantity(oldPosQuantity);

                position.addTrade(manageableTrade); 
                BigDecimal newPosQuantity = position.getQuantity().add(manageableTrade.getQuantity());
                position.setQuantity(newPosQuantity);

                 _positionMaster.add(new PositionDocument(position));
            }

        } else {

            //We will need to add a new version of either the trade
            //or the security or both. If the security needs
            //updating we should probably update the trade as well.
            SecurityMasterUtils.addOrUpdateSecurity( _securityMaster,  _tradeAdapter.buildSecurityForTrade(trade));
            previousPosition.removeTrade(previousTrade);
            previousPosition.addTrade(manageableTrade);
        }
             _positionMaster.add(new PositionDocument(previousPosition));

            if (isNewPosition) { 
                ddPositionToPortfolio(previousPosition, portfolio);
        }
    }

    private ManageablePosition findPosition(UniqueId parentPositionId) {
        PositionSearchRequest request = new PositionSearchRequest();
        request.setPositionObjectIds(ImmutableList.of(parentPositionId)); 
        return _positionMaster.search(request).getSinglePosition();

    }

    private ManageableTrade findTradeByExternalId(T trade) { 
        ExternalId id = _tradeAdapter.getExternalId(trade); 
        PositionSearchRequest request = new PositionSearchRequest(); 
        request.setTradeProviderId(id);

        return Iterables.getOnlyElement( _positionMaster.search(request).getSinglePosition().getTrades());
    }

    private ExternalIdBundle retrieveSecurityForTrade(T trade) {

        if ( _tradeAdapter.isTradeUsingListedProduct(trade)) {
            // We don't need to insert a security, just reference it 
            return _tradeAdapter.determineSecurityIdForTrade(trade);
        } else {
            ManageableSecurity security = _tradeAdapter.buildSecurityForTrade(trade); 
            return security.getExternalIdBundle();
        }
    }

Note than in the above code no attempt is made to remove empty positions, though this would be straightforward to implement if required.


Cancelling Trades
-----------------

Trades that the feed mechanism has determined are cancellations if existing ones can be handled as follows (again for a generic trade type T):

.. code:: java

    public void cancelTrade(T trade) {

        ManageableTrade previousTrade = findTradeByExternalId(trade);
        ManageablePosition previousPosition = findPosition(previousTrade.getParentPositionId());

        if ( _tradeAdapter.isTradeUsingListedProduct(trade)) {

            previousPosition.removeTrade(previousTrade);
            BigDecimal newQuantity = previousPosition.getQuantity()
                .subtract(previousTrade.getQuantity());
            previousPosition.setQuantity(newQuantity);
            _positionMaster.add(new PositionDocument(previousPosition));

        } else {

            // Remove the security
            SecuritySearchRequest request = new SecuritySearchRequest();
            request.setExternalIdSearch(
                ExternalIdSearch.of( _tradeAdapter.determineSecurityIdForTrade(trade)));
            ManageableSecurity security = _securityMaster.search(request).getSingleSecurity();
             _securityMaster.remove(security.getUniqueId());

            // Remove the trade and position
             _positionMaster.remove(previousPosition.getUniqueId());
        }
    }

Again, in the above code, no attempt is made to remove empty positions.
