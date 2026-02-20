<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
   
  <html>
  <head>
    <title>Timer Control</title>
    <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css"/>
    <script>
      var id = "<xsl:value-of select="//@id" />";
      var url = escape("<xsl:value-of select="//@url" />");
      var filter = "<xsl:value-of select="//@filter" />";
      
      function addEvent(objObject, strEventName, fnHandler)
      {
         if (objObject.addEventListener) // DOM-compliant way to add an event listener
         {
            objObject.addEventListener(strEventName, fnHandler, false);
         }
         else if (objObject.attachEvent) // IE/windows way to add an event listener
         {
            objObject.attachEvent("on" + strEventName, fnHandler);
         }
      }
      
      function Initialize()
      {
         addEvent(document, "keydown", onKeyDownEvent);
      	window.focus();
      }
      
      function onKeyDownEvent(nKeyCode)
      {
      	var keyCode = nKeyCode.keyCode;
      	//alert(keyCode);
      	
        if(keyCode == 13)      
           document.location.href= "/servlet/KBScheduleDataRes?action=04&amp;id=" + id;
      	return true;
      }
    
    </script>
  </head>
  <body onload="Initialize();">
  <table class='KBButtonTable'>
    <tr><td align="center" valign="middle">
      <table border="0" cellspacing="20">
        <tr>
          <td class="ItemLog">
            <xsl:apply-templates select="//logitem" mode="rows" />
          </td>
        </tr>
      </table>
    </td></tr>
  </table>
  
  </body>
  </html>   
   
	</xsl:template>
   
	<xsl:template match="logitem" mode="rows">
    <xsl:value-of select="line" /><br/>
	</xsl:template>
   
</xsl:stylesheet>