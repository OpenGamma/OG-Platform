<#escape x as x?html>
{
    "template_data": {
      "name": "${portfolio.name}",
      "objectId":"${portfolio.uniqueId.objectId}",
      "versionId":"${portfolio.uniqueId.version}",
<#-- deprecated -->
      "id": "${portfolio.uniqueId.objectId}",
<#if deleted>
      "deleted":"${portfolioDoc.versionToInstant}",
</#if>
      "node" : "${rootNode.uniqueId.objectId}"
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