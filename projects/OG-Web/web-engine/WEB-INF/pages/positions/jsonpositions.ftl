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
	 <#list searchResult.positions as position>
	   "${position.uniqueId.objectId}|${position.name}|${position.quantity}|${position.trades?size}"<#if position_has_next>,</#if>
	 </#list> </#if>]
}
</#escape>