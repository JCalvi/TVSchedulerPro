<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
     
  <html>
  <head>
    <title>Timer Control</title>
    <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css" />
    <script language="JavaScript">

      var currentSelection = 0;
      var destination = "";
      var maxItem = 0;
      
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
      	setActiveButton(0);
      	maxItem = findMaxItem();
      	document.title = "page ready";
      }
      
      function onKeyDownEvent(nKeyCode)
      {
      	var keyCode = nKeyCode.keyCode;
      
      	if	(keyCode == 40) // Down
      	{
            setActiveButton(1);
      	}
      	else if	(keyCode == 38) // Up
      	{
            setActiveButton(-1);
      	}
      	else if (keyCode == 13) // enter
      	{
      	   if(destination.length > 0)
      	      document.location.href = destination;
      	   else
      	      document.location.href = "/servlet/ApplyTransformRes?xml=root&amp;xsl=kb-buttons";
      	   //alert(destination);
      	}
      	else if (keyCode == 36 || keyCode == 27) // back
      	{
      	   document.location.href = "/servlet/ApplyTransformRes?xml=root&amp;xsl=kb-buttons";
      	}
      	return false;
      }
      
      function setActiveButton(amount)
      {
         var item = document.getElementById("item_" + currentSelection);
         if(item)
         {
            item.className = item.getAttribute("defaultClass");
         }
      
         currentSelection += amount;
         if(currentSelection &lt; 0) currentSelection = maxItem;
      
         item = document.getElementById("item_" + currentSelection);
         if(item)
         {
            item.className = item.getAttribute("activeClass");
            destination = item.getAttribute("value");
            setItemDetails(item);
         }
         else
         {
            currentSelection = 0;
            item = document.getElementById("item_" + currentSelection);
            if(item)
            {
               item.className = item.getAttribute("activeClass");
               destination = item.getAttribute("value");
               setItemDetails(item);
            }
         }
      }
      
      function findMaxItem()
      {
         for(var x = 0; x &lt; 1000; x++)
         {
            var item = document.getElementById("item_" + x);
            if(!item)
               return x-1;   
         } 
      }
      
      function setItemDetails(item)
      {
         field = document.getElementById("startbuffer");
         field.innerHTML = item.getAttribute("startbuffer");
      
         field = document.getElementById("endbuffer");
         field.innerHTML = item.getAttribute("endbuffer");
      
         field = document.getElementById("capturetype");
         field.innerHTML = item.getAttribute("capturetype");
      }
    </script>
  </head>
  <body onload="Initialize();">
  
  <table class='KBListTable'>
    <tr class='Row' > 
      <td class='itemdata' id='headingtitle'><span class='solidText'>EPG Auto Add Table</span></td>
      <td class='itemdataNoWrap' id='startbuffer' style="text-align:right;width:10%"><span class='solidText'>Start Buffer</span></td>
    </tr>
    <tr class='Row'> 
      <td class='itemdata' id='capturetype'><span class='solidText'>Capture Type</span></td>
      <td class='itemdataNoWrap' id='endbuffer' style="text-align:right"><span class='solidText'>End Buffer</span></td>
    </tr>
    <tr height='10'></tr>
  </table>
  
  <table class='KBListTable'>
    <xsl:if test="number(//@start) = 0">
      <tr>
        <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2'>
          <xsl:attribute name="id">item_0</xsl:attribute>
          <xsl:attribute name="capturetype">Return to Main Menu</xsl:attribute>
          <xsl:attribute name="startbuffer"><xsl:value-of select="//@start + 1" /> to <xsl:value-of select="//@total" /></xsl:attribute>
          <xsl:if test="number(//@start) + number(//@show) &lt; count(//aaitem)">
           <xsl:attribute name="startbuffer"><xsl:value-of select="//@start + 1" /> to <xsl:value-of select="number(//@start) + number(//@show)" /></xsl:attribute>
          </xsl:if>
          <xsl:attribute name="endbuffer">of <xsl:value-of select="//@total" /></xsl:attribute>
          <xsl:attribute name="value">/servlet/ApplyTransformRes?xml=root&amp;xsl=kb-buttons</xsl:attribute>
          <span class='solidText'>Main Menu</span>
        </td>
      </tr>
    </xsl:if>  
    <xsl:if test="number(//@start) > 0">
      <tr>
        <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2'>
          <xsl:attribute name="id">item_0</xsl:attribute>
          <xsl:if test="number(//@start) - number(//@show) > -1">
            <xsl:attribute name="value"><xsl:value-of select="//mainurl" />start=<xsl:value-of select="number(//@start) - number(//@show)" />&amp;show=<xsl:value-of select="//@show" /></xsl:attribute>
          </xsl:if>
          <xsl:if test="number(//@start) - number(//@show) &lt; 0">
            <xsl:attribute name="value"><xsl:value-of select="//mainurl" />start=0&amp;show=<xsl:value-of select="//@show" /></xsl:attribute>
          </xsl:if>           
          <xsl:attribute name="capturetype">Return to Previous Page</xsl:attribute>
          <xsl:attribute name="startbuffer"><xsl:value-of select="//@start + 1" /> to <xsl:value-of select="//@total" /></xsl:attribute>
          <xsl:if test="number(//@start) + number(//@show) &lt; count(//aaitem)">
            <xsl:attribute name="startbuffer"><xsl:value-of select="//@start + 1" /> to <xsl:value-of select="number(//@start) + number(//@show)" /></xsl:attribute>
          </xsl:if>
          <xsl:attribute name="endbuffer">of <xsl:value-of select="//@total" /></xsl:attribute>
          <span class='solidText'>Previous Page</span>
        </td>
      </tr>
    </xsl:if>
  
    <xsl:apply-templates select="//aaitem" mode="rows">
      <xsl:sort select="title"/>
      <xsl:with-param name="start" select="//@start" />
      <xsl:with-param name="show" select="//@show" />
    </xsl:apply-templates> 
    
    <xsl:if test="number(//@start) + number(//@show) &lt; count(//aaitem)">
      <tr>
        <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2'>
          <xsl:attribute name="id">item_<xsl:value-of select="//@show + 1" /></xsl:attribute>
          <xsl:attribute name="value"><xsl:value-of select="//mainurl" />start=<xsl:value-of select="//@start + //@show" />&amp;show=<xsl:value-of select="//@show" /></xsl:attribute>
          <xsl:attribute name="capturetype">Advance to Next Page</xsl:attribute>
          <xsl:attribute name="startbuffer"><xsl:value-of select="//@start + 1" /> to <xsl:value-of select="//@total" /></xsl:attribute>
          <xsl:if test="number(//@start) + number(//@show) &lt; count(//aaitem)">
           <xsl:attribute name="startbuffer"><xsl:value-of select="//@start + 1" /> to <xsl:value-of select="number(//@start) + number(//@show)" /></xsl:attribute>
          </xsl:if>
          <xsl:attribute name="endbuffer">of <xsl:value-of select="//@total" /></xsl:attribute>
          <span class='solidText'>Next Page</span>
        </td>
      </tr>
    </xsl:if>          
  </table>
  
  </body>
  </html>     
     
  </xsl:template>

  <xsl:template match="aaitem" mode="rows">
    <xsl:param name="start"/>
    <xsl:param name="show"/>
    <xsl:if test="position() &gt; number($start)">
      <xsl:if test="position() &lt; number($start) + number($show) + 1">
        <tr>
          <td style="padding-left:10px;" activeClass='ActiveRow' defaultClass='Row' class='Row'>
            <xsl:attribute name="id">item_<xsl:value-of select="position() - number($start)" /></xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of select="title" /></xsl:attribute>
            <xsl:attribute name="startbuffer">Start: <xsl:value-of select="startbuffer" /></xsl:attribute>
            <xsl:attribute name="endbuffer">End: <xsl:value-of select="endbuffer" /></xsl:attribute>
            <xsl:if test="capturetype = '-1'">
              <xsl:attribute name="capturetype">Capture Type : AutoSelect</xsl:attribute>
            </xsl:if>  
            <xsl:if test="capturetype = '0'">
              <xsl:attribute name="capturetype">Capture Type : TS Full</xsl:attribute>
            </xsl:if>
            <xsl:if test="capturetype = '1'">
              <xsl:attribute name="capturetype">Capture Type : DVR-MS</xsl:attribute>
            </xsl:if>
            <xsl:if test="capturetype = '2'">
              <xsl:attribute name="capturetype">Capture Type : TS Mux</xsl:attribute>
            </xsl:if>
            <xsl:if test="capturetype = '3'">
              <xsl:attribute name="capturetype">Capture Type : MPG</xsl:attribute>
            </xsl:if>
            <xsl:attribute name="value"><xsl:value-of select="action" /></xsl:attribute>
            <span class='solidText'><xsl:value-of select="title" /></span>
          </td>
          <td class='Row' style="text-align:right;width:10%">
            <xsl:if test="enabled = 'true'">
              Enabled
            </xsl:if>
            <xsl:if test="enabled = 'false'">
              Disabled
            </xsl:if>      
          </td>
        </tr> 
      </xsl:if>
    </xsl:if>      
  </xsl:template>         
   
</xsl:stylesheet>