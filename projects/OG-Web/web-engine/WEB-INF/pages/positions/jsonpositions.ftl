<#escape x as x?html>
{
	"header": {
    	"type": "Positions",
    	<#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
	    </#if>
	    "dataFields": ["id", "name", "quantity", "trades"]
    },
	"data": [<#if searchResult??>
	 <#list searchResult.documents as item>
	   "${item.position.uniqueId.objectId}|${item.position.name}|${item.position.quantity}|${item.position.trades?size}"<#if item_has_next>,</#if>
	 </#list> </#if>]
}
</#escape>