<#escape x as x?html>
{
    "template_data": {
        "portfolio_name": "${portfolio.name}",
        "object_id": "${portfolio.uniqueId.objectId}",
        "version_id": "${portfolio.uniqueId.version}",
        <#if parentNode?has_content>
        "parent_node": "${parentNode.name}",
        "parent_node_id": "${parentNode.uniqueId.objectId}",
        <#else>
        "parent_node": "Root",
        </#if>
        "name": "${node.name}",
        "node": "${node.uniqueId.objectId}",
        "path" : [
            <#list pathNodes as item>
               {"name": "${item.second}", "node": "${item.first.objectId}", "version_id": "${item.first.version}"}<#if item_has_next>,</#if>
	    </#list>
        ]
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
