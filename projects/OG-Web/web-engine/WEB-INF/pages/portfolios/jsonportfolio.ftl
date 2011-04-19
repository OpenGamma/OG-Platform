<#escape x as x?html>
{
    "templateData": {
            "name": "${portfolio.name}",
            "id": "${portfolio.uniqueId.objectId}",
            "node" : "${rootNode.uniqueId.objectId}"
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