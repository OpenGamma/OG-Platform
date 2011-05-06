<#escape x as x?html>
{
    "templateData": {
      "portfolioName": "${portfolio.name}",
      "id": "${portfolio.uniqueId.objectId}",
<#if deleted>
      "deleted":"${portfolioDoc.versionToInstant}",
</#if>
<#if parentNode?has_content>
      "parentNode": "${parentNode.name}",
      "parentNodeId": "${parentNode.uniqueId.objectId}",
<#else>
      "parentNode": "Root",
</#if>
      "name": "${node.name}",
      "node": "${node.uniqueId.objectId}"
    },
    "portfolios": [
    	<#list childNodes as item>
			{"name": "${item.name}", "id": "${item.uniqueId.objectId}"}<#if item_has_next>,</#if>
		</#list>
    ],
    "positions": [
    	<#list positions as item>
			{"name": "${item.name}", "quantity": "${item.quantity}", "id": "${item.uniqueId.objectId}"}<#if item_has_next>,</#if>
		</#list>
    ]
}
</#escape>