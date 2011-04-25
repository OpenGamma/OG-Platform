<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>OpenGamma Risk Viewer</title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    
    <script language="JavaScript" src="${pageContext.request.contextPath}/lib/firebugx.js"></script>
    
    <%-- script type="text/javascript" src="${pageContext.request.contextPath}/jquery/jquery-1.3.2.js"></script--%>
	<script language="JavaScript" src="${pageContext.request.contextPath}/lib/jquery-1.4.2.min.js"></script>
	<script language="JavaScript" src="${pageContext.request.contextPath}/lib/jquery-ui-1.7.2.custom.min.js"></script>
	<script language="JavaScript" src="${pageContext.request.contextPath}/lib/jquery.rule-1.0.1.1-min.js"></script>
	<script language="JavaScript" src="${pageContext.request.contextPath}/lib/jquery.event.drag.custom.js"></script>
    <script language="JavaScript" src="${pageContext.request.contextPath}/jquery/jquery.json-2.2.js"></script>
    <script language="JavaScript" src="${pageContext.request.contextPath}/org/cometd.js"></script>
    <script language="JavaScript" src="${pageContext.request.contextPath}/jquery/jquery.cometd.js"></script>
    <script language="JavaScript" src="${pageContext.request.contextPath}/jquery/jquery.sparkline.js"></script>

	<script language="JavaScript" src="${pageContext.request.contextPath}/slick.editors.js"></script>
	<script language="JavaScript" src="${pageContext.request.contextPath}/slick.grid.js"></script>
	<script language="JavaScript" src="${pageContext.request.contextPath}/slick.model.js"></script>		
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/slick.grid.css" type="text/css" media="screen" charset="utf-8" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery-ui-1.7.2.custom.css" type="text/css" media="screen" charset="utf-8" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/examples.css" type="text/css" media="screen" charset="utf-8" />
	
	<style>
		.cell-title {
			font-weight: bold;
		}

		.cell-effort-driven {
			text-align: center;
		}

		.toggle {
			height: 9px;
			width: 9px;
			display: inline-block;
		}

		.toggle.expand {
			background: url(images/expand.gif) no-repeat center center;
		}

		.toggle.collapse {
			background: url(images/collapse.gif) no-repeat center center;
		}
		
		.tickindicator {
			height: 16px;
			width: 16px;
			display: inline-block;
		}
		
		.tickindicator.up {
		    background: url(images/up-tick.png) no-repeat center center;
		}
		 
		.tickindicator.down {
	        background: url(images/down-tick.png) no-repeat center center;
		}
		
		.tickindicator.same {
		    background: url(images/same-tick.png) no-repeat center center;
		}
		
		.header {
		    width: 289px;
		    height: 51px;
		    display: inline-block;
		    background: url(images/opengamma.png) no-repeat center center;
		    margin-left: 1em;
		    margin-bottom: 1em;
		    margin-top: 1em;
		}
		 
		.viewsel {
		    float: right;
		    margin-right:2em;
		    margin-top:3em;
		}
		
		#grid { position:absolute; top:8em; left:0; right:0; bottom:0; }
			
	</style>
    
    <script type="text/javascript" src="application.js"></script>
    <%--
      The reason to use a JSP is that it is very easy to obtain server-side configuration
      information (such as the contextPath) and pass it to the JavaScript environment on the client.
    --%>
    <script type="text/javascript">
        var config = {
            contextPath: '${pageContext.request.contextPath}'
        };
        
    </script>
</head>
<body>
    <div class="viewsel">
        <select id="views">
        </select>
        <button id="go">Select View</button>
    </div>
	<div class="header">
	</div>


    <div id="status"></div>
    <table id="table">
    </table>
    <div id="grid"></div>
</body>
</html>
