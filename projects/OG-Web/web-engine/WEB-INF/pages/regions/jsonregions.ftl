<#escape x as x?html>
{
    "header": {
        "type": "Regions",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
	      </#if>
	      "dataFields": ["id", "name", "validFrom"]
	},
    "data": [<#if searchResult??>
     <#list searchResult.documents as item>
    	"${item.uniqueId.objectId}|${item.region.name}|${item.versionFromInstant}|${item.region.getCountry()}"<#if item_has_next>,</#if>
	   </#list> </#if>]
}
</#escape>