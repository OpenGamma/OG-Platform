<#escape x as x?html>
{
    "template_data": {
        "name": "${portfolio.name}",
        "portfolio_name": "${portfolio.name}",
        "object_id": "${portfolio.uniqueId.objectId}",
        "version_id": "${portfolio.uniqueId.version}",
        <#if deleted>
        "deleted": "${portfolioDoc.versionToInstant}",
        </#if>
        "node" : "${rootNode.uniqueId.objectId}",
        "path" : [{"name": "${portfolio.name}", "node": "${portfolio.uniqueId.objectId}", "version_id": "${portfolio.uniqueId.version}"}]
    },
    "portfolios": [
        <#list childNodes as item>{
            "name": "${item.name}",
            "id": "${item.uniqueId.objectId}"
        }<#if item_has_next>,</#if></#list>
    ],
    "positions": [
        <#list positions as item>{
            "name": "${item.name}",
            "quantity": "${item.quantity}",
            "id": "${item.uniqueId.objectId}"
        }<#if item_has_next>,</#if></#list>
    ]
}
</#escape>