<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
   
  <html>
  <head>
    <title>Timer Control</title>
    <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css"/>
    <script>
      var url = "<xsl:value-of select="//@url" />";
      
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
      	
        if(keyCode == 13)      
           document.location.href= url;

      	return true;
      }
    </script>
  </head>
  <body onload="Initialize();">
  
  <table style='KBButtonList'>
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
    <xsl:if test="@type = 1">
      <div width="100%" style="border-top: 2px solid #FDEF11;border-bottom: 2px solid #FDEF11;">
        <xsl:value-of select="line" />
      </div>
    </xsl:if>
    <xsl:if test="@type = 0">
      <div width="100%" style="padding-left: 10px;">
        <xsl:value-of select="line" />
      </div>
    </xsl:if>
	</xsl:template>
   
</xsl:stylesheet>

