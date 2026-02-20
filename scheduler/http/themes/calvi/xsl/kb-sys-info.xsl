<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template match="/">
   
  <html>
  <head>
    <title>Timer Control</title>
    <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css"/>
    <script>
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
        document.location.href= "/servlet/ApplyTransformRes?xml=sys-settings&amp;xsl=kb-buttons";
        return true;
      }
    </script>
  
  </head>
  <body onload="Initialize();">
  
  <table class='KBButtonTable'>
    <tr><td align="center" valign="middle">
      <table border="0" cellspacing="20">
        <xsl:apply-templates select="//info" mode="cell" />
      </table>
    </td></tr>
  </table>

  </body>
  </html>   
   
	</xsl:template>

	<xsl:template match="info" mode="cell">
    <tr>
      <td class="SystemInfo">
        <span class="solidText"><xsl:value-of select="@name" /></span>
      </td>
      <td class="SystemInfo">
        <span class="solidText"><xsl:value-of select="@value" />&#160;<xsl:value-of select="@units" /></span>
      </td>
    </tr>
	</xsl:template>
   
</xsl:stylesheet>

