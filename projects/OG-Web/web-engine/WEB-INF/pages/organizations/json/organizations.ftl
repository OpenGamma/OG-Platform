<#escape x as x?html>
{
	"header": {
    	"type": "Organizations",
    	<#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
	    </#if>
	    "dataFields": ["id", "obligorShortName", "ObligorRedCode", "ObligorTicker"]
    },
	"data": [<#if searchResult??>
	 <#list searchResult.organizations as organization>
	   "${organization.uniqueId.objectId}|${organization.obligor.obligorShortName}|${organization.obligor.obligorREDCode}|${organization.obligor.obligorTicker}"<#if organization_has_next>,</#if>
	 </#list> </#if>]
}
</#escape>