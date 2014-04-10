<#-- Macro to produce a standard page with html/head/body -->
<#macro page title="OpenGamma" aceXmlEditor=false jquery=false jqueryDate=false>
<html>
  <head>
    <title>${title} - OpenGamma</title>
    <link type="text/css" rel="stylesheet" href="/green/css/og-base.css" />
<#if jqueryDate>
    <link rel="stylesheet" href="/css/jquery/smoothness/jquery-ui-1.8.5.custom.css" />
</#if>

<#if jquery>
    <script src="/prototype/scripts/lib/jquery/jquery-1.8.0.js"></script>
</#if>
 
<#if jqueryDate>    
    <script src="/prototype/scripts/lib/jquery/ui/jquery-ui-1.8.11.custom.min.js"></script>
    <script src="/prototype/scripts/lib/jquery/ui/jquery.ui.datepicker.js"></script>
</#if>  
  
<#if aceXmlEditor>
    <script src="/prototype/scripts/lib/ace/ace.js"></script>
    <script src="/prototype/scripts/lib/ace/theme-textmate.js"></script>
    <script src="/prototype/scripts/lib/ace/mode-xml.js"></script>
</#if>
  </head>
  <body>
    <div id="header">
      <table>
        <tr colcount="2">
          <td id="logo">
            <a href="/jax"><img src="/images/opengamma.png" width="289" height="51" alt="OpenGamma - Financial Analytics and Risk Management" /></a>
          </td>
          <td id="topright">
<#if security.enabled>
 <#if security.loggedIn>
            Logged in as <a href="${security.profileUri}">${security.userName}</a> : <a href="${security.logoutUri}">Logout</a>
 <#else>
            <a href="${security.registerUri}">Register</a> : <a href="${security.loginUri}">Login</a>
 </#if>
</#if>
            <br />
            ${dateFormatter.format(now)}, ${timeFormatter.format(now)} ${offsetFormatter.format(now)} ${timeZone}            
          </td>
        </tr>
      </table>
    </div>
    <div id="body">
<#nested>
    </div>
    <!-- Freemarker template: ${freemarkerTemplateName} -->
    <!-- Freemarker locale: ${freemarkerLocale} -->
    <!-- Freemarker version: ${freemarkerVersion} -->
    <!-- Processing time: ${now} -->
  </body>
</html>
</#macro>

<#-- Macro to produce a standard page section with title -->
<#macro section title="" css="" if=true>
<#if if>
<div class="section ${css}">
<#if title?has_content>
  <h2>${title}</h2>
</#if>
<#nested>
</div>
</#if>
</#macro>

<#-- Macro to produce a standard page subsection with title -->
<#macro subsection title="" if=true>
<#if if>
<div class="subsection">
<#if title?has_content>
  <h3>${title}</h3>
</#if>
<#nested>
</div>
</#if>
</#macro>

<#-- Macro to produce a standard table -->
<#macro table items empty="No data" headers=[] paging=[] loop=true if=true>
<#if if>
<#if items?size == 0>
  <p>${empty}</p>
<#else>
  <table>
<#if headers?size != 0>
    <tr><#rt>
<#list headers as header>
    <th>${header}</th><#t>
</#list>
    </tr><#lt>
</#if>
<#if loop>
<#list items as item>
    <tr>
<#nested item>
    </tr>
</#list>
<#else>
<#nested items>
</#if>
  </table>
<#if paging?has_content>
  <div class="paging">
    <div class="box">
<#if paging.previousPageExists>
      <div class="item page active"><a href="${paging.previousPage.uri}">prev</a></div>
<#else>
      <div class="item prev inactive">prev</div>
</#if>
<#list paging.pagesStandard as page>
<#if page.currentPage>
      <div class="item page current">${page.pageNumber}</div>
<#else>
      <div class="item page active"><a href="${page.uri}">${page.pageNumber}</a></div>
</#if>
</#list>
<#if paging.nextPageExists>
      <div class="item next active"><a href="${paging.nextPage.uri}">next</a></div>
<#else>
      <div class="item next inactive">next</div>
</#if>
    </div>
  </div>
</#if>
</#if>
</#if>
</#macro>

<#-- Macro to produce a standard form handling RESTful tunneling -->
<#macro form action method="GET" id="">
  <form <#if id?has_content>id="${id}"</#if> method="<#if method == "GET">GET<#else>POST</#if>" action="${action}"><#rt>
<#if method == "PUT" || method == "DELETE"><input type="hidden" name="method" value="${method}" /></#if>
<#nested>
  </form>
</#macro>

<#-- Macro to produce a standard input row -->
<#macro rowin id="" label="" if=true>
<#if if>
<div class="row in" <#if id?has_content>id="${id}"</#if>><#if label?has_content><div class="lbl">${label}</div></#if><div class="dat"><#nested></div></div><#rt>
</#if>
</#macro>

<#-- Macro to produce a standard output row -->
<#macro rowout label="" if=true>
<#if if>
<div class="row out"><div class="lbl">${label}</div><div class="dat"><#nested></div></div><#rt>
</#if>
</#macro>

<#-- Macro to produce a space between data -->
<#macro space>
<hr style="border: none; border-top: 1px solid #ccc;" />
</#macro>

<#-- Macro to produce script tag for ace xml editor -->
<#macro xmlEditorScript formId="" inputId="" xmlValue="" readOnly=false>
<script type="text/javascript">
var editor = ace.edit("ace-xml-editor")
editor.getSession().setMode('ace/mode/xml')
editor.getSession().setValue("${xmlValue}")
<#if readOnly>
editor.setReadOnly(true)
</#if>

<#if formId?has_content>
$("#${formId}").submit( function(eventObj) {
  <#if inputId?has_content>
  $("#${inputId}").val(editor.getSession().getValue())
  </#if>
  return true
})
</#if>
</script>
</#macro>
