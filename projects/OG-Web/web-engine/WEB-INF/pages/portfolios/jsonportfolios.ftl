<#escape x as x?html>
{
    "header": {
        "type": "Portfolios",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
	      </#if>
	      "dataFields": ["id", "node", "name", "validFrom"]
    },
    "data": [<#if searchResult??>
      <#list searchResult.documents as item>
	       "${item.portfolio.uniqueId.objectId}|${item.portfolio.rootNode.uniqueId.objectId}|${item.portfolio.name}|${item.versionFromInstant}"<#if item_has_next>,</#if>
	    </#list> </#if>]
}
</#escape>