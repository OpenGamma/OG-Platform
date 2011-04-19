<#escape x as x?html>
{
    "header": {
        "type": "Regions",
        "dataFields": ["id", "name", "validFrom"],
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
	      "count": ${"${paging.pagingSize}"?replace(',','')}
	      </#if>
	},
    "data": [<#if searchResult??>
     <#list searchResult.documents as item>
    	"${item.uniqueId.objectId}|${item.region.name}|${item.versionFromInstant}"<#if item_has_next>,</#if>
	   </#list> </#if>]
}
</#escape>