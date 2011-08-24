<#escape x as x?html>
{
	"header": {
    	"type": "Positions",
    	<#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "page": ${"${paging.pageNumber}"?replace(',','')},
        "pageSize": ${"${paging.pagingSize}"?replace(',','')},
	    </#if>
	    "dataFields": ["id", "name", "quantity", "trades"]
    },
	"data": [<#if searchResult??>
	 <#list searchResult.documents as item>
	   "${item.position.uniqueId.objectId}|${item.position.name}|${item.position.quantity}|${item.position.trades?size}"<#if item_has_next>,</#if>
	 </#list> </#if>]
}
</#escape>