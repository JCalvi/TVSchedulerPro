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
      
         // var field = document.getElementById("schName");
         // field.innerHTML = item.getAttribute("schName");
         
         var field = document.getElementById("schSubName");
         field.innerHTML = item.getAttribute("schSubName");
         
         field = document.getElementById("schDur");
         field.innerHTML = item.getAttribute("schDur");
         
         field = document.getElementById("schChannel");
         field.innerHTML = item.getAttribute("schChannel");
         
         field = document.getElementById("schType");
         field.innerHTML = item.getAttribute("schType");
      }
      
      </script>
      </head>
      <body onload="Initialize();">
      
      <table class='KBListTable'>
        <tr class='Row' > 
          <td class='itemdata' id='schChannel' ><span class='solidText'>Channel</span></td>
          <td class='itemdataNoWrap' id='schDur' style="text-align:right;width:10%"><span class='solidText'>Duration</span></td>
        </tr>
        <tr class='Row'> 
          <td class='itemdata' id='schSubName' ><span class='solidText'>Sub Title</span></td>
          <td class='itemdataNoWrap' id='schType' style="text-align:right"><span class='solidText'>Type</span></td>
        </tr>
        <tr height='10'></tr>
       </table>
       
       <table class='KBListTable'>
         <xsl:if test="//@start = 0">
            <tr>
              <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2' >
                <xsl:attribute name="id">item_0</xsl:attribute>
                <xsl:attribute name="schDur"><xsl:value-of select="//@start + 1" /> to <xsl:value-of select="//@end" /></xsl:attribute>
                <xsl:attribute name="schType"> of <xsl:value-of select="//@total" /></xsl:attribute>
                <xsl:attribute name="schStatus">...</xsl:attribute>
                <xsl:attribute name="schName">...</xsl:attribute>
                <xsl:attribute name="schChannel">Schedule List</xsl:attribute>            
                <xsl:attribute name="schSubName">Return to Main Menu</xsl:attribute>
                <xsl:attribute name="value">/servlet/ApplyTransformRes?xml=root&amp;xsl=kb-buttons</xsl:attribute>
                <span class='solidText'>Main Menu</span>
              </td>
            </tr>
            <tr>
              <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2' >
                <xsl:attribute name="id">item_1</xsl:attribute>
                <xsl:attribute name="schDur"><xsl:value-of select="//@start + 1" /> to <xsl:value-of select="//@end" /></xsl:attribute>
                <xsl:attribute name="schType"> of <xsl:value-of select="//@total" /></xsl:attribute>
                <xsl:attribute name="schStatus">...</xsl:attribute>
                <xsl:attribute name="schName">...</xsl:attribute>
                <xsl:attribute name="schChannel">Schedule List</xsl:attribute>
                <xsl:if test="//@filter = 0">
                   <xsl:attribute name="schSubName">Show Past Schedules</xsl:attribute>
                   <xsl:attribute name="value">/servlet/KBScheduleDataRes?start=0&amp;show=<xsl:value-of select="//@show" />&amp;filter=1</xsl:attribute>
                   <span class='solidText'>Past Schedules</span>
                </xsl:if>
                <xsl:if test="//@filter = 1">
                   <xsl:attribute name="schSubName">Show Pending Schedules</xsl:attribute>
                   <xsl:attribute name="value">/servlet/KBScheduleDataRes?start=0&amp;show=<xsl:value-of select="//@show" />&amp;filter=0</xsl:attribute>
                   <span class='solidText'>Pending Schedules</span>
                </xsl:if>
              </td>
            </tr>
        </xsl:if>  
         
        <xsl:if test="//@start > 0">
          <tr>
           <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2' >
             <xsl:attribute name="id">item_0</xsl:attribute>
             <xsl:attribute name="schDur"><xsl:value-of select="//@start + 1" /> to <xsl:value-of select="//@end" /></xsl:attribute>
             <xsl:attribute name="schType"> of <xsl:value-of select="//@total" /></xsl:attribute>
             <xsl:attribute name="schStatus">...</xsl:attribute>
             <xsl:attribute name="schName">...</xsl:attribute>
             <xsl:attribute name="schChannel">Schedule List</xsl:attribute>
             <xsl:attribute name="schSubName">Return to Previous Page</xsl:attribute>
             <xsl:attribute name="value">/servlet/KBScheduleDataRes?start=<xsl:value-of select="//@start - //@show" />&amp;show=<xsl:value-of select="//@show" />&amp;filter=<xsl:value-of select="//@filter" /></xsl:attribute>
             <span class='solidText'>Previous Page</span>
           </td>
          </tr>
        </xsl:if>
        
        <xsl:apply-templates select="//schedule" mode="rows" />
        <xsl:if test="//@end != //@total">
          <tr>
            <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2' >
              <xsl:if test="//@start = 0">
                <xsl:attribute name="id">item_<xsl:value-of select="//@show + 2" /></xsl:attribute>
              </xsl:if>
              <xsl:if test="//@start > 0">
                <xsl:attribute name="id">item_<xsl:value-of select="//@show + 1" /></xsl:attribute>
              </xsl:if>
              <xsl:attribute name="schDur"><xsl:value-of select="//@start + 1" /> to <xsl:value-of select="//@end" /></xsl:attribute>
              <xsl:attribute name="schType"> of <xsl:value-of select="//@total" /></xsl:attribute>
              <xsl:attribute name="schStatus">...</xsl:attribute>
              <xsl:attribute name="schName">...</xsl:attribute>
              <xsl:attribute name="schChannel">Schedule List</xsl:attribute>
              <xsl:attribute name="schSubName">Advance to Next Page</xsl:attribute>
              <xsl:attribute name="value">/servlet/KBScheduleDataRes?start=<xsl:value-of select="//@end" />&amp;show=<xsl:value-of select="//@show" />&amp;filter=<xsl:value-of select="//@filter" /></xsl:attribute>
              <span class='solidText'>Next Page</span>
            </td>
          </tr>
        </xsl:if>          
      </table>
      
      </body>
      </html>     
      </xsl:template>
   
      <xsl:template match="schedule" mode="rows">
        <tr>
          <td activeClass='ActiveRow' defaultclass='Row' class='Row' style="padding-left:10px;" >
          <xsl:if test="itemState = 1">
            <xsl:attribute name="activeClass">epgScheduleSkippedSelected</xsl:attribute>
            <xsl:attribute name="defaultclass">epgScheduleSkipped</xsl:attribute>
            <xsl:attribute name="class">epgScheduleSkipped</xsl:attribute>
          </xsl:if> 
          <xsl:if test="itemState = 2">
            <xsl:attribute name="activeClass">epgScheduleRunningSelected</xsl:attribute>
            <xsl:attribute name="defaultclass">epgScheduleRunning</xsl:attribute>
            <xsl:attribute name="class">epgScheduleRunning</xsl:attribute>
          </xsl:if> 
          <xsl:if test="itemState = 3">
            <xsl:attribute name="activeClass">epgScheduleFinishedSelected</xsl:attribute>
            <xsl:attribute name="defaultclass">epgScheduleFinished</xsl:attribute>
            <xsl:attribute name="class">epgScheduleFinished</xsl:attribute>
          </xsl:if>
          <xsl:if test="itemState &gt; 3">
            <xsl:attribute name="activeClass">epgScheduleErrorSelected</xsl:attribute>
            <xsl:attribute name="defaultclass">epgScheduleError</xsl:attribute>
            <xsl:attribute name="class">epgScheduleError</xsl:attribute>
          </xsl:if>
          <xsl:if test="//@start = 0">
            <xsl:attribute name="id">item_<xsl:value-of select="position()+1" /></xsl:attribute>
          </xsl:if>
          <xsl:if test="//@start > 0">
            <xsl:attribute name="id">item_<xsl:value-of select="position()" /></xsl:attribute>
          </xsl:if>
            <xsl:attribute name="schType"><xsl:value-of select="schType" /></xsl:attribute>
            <xsl:attribute name="itemState"><xsl:value-of select="itemState" /></xsl:attribute>      
            <xsl:attribute name="schStatus"><xsl:value-of select="schStatus" /></xsl:attribute>
            <xsl:attribute name="schDur"><xsl:value-of select="schDur" />min</xsl:attribute>
            <xsl:attribute name="schName"><xsl:value-of select="schName" /></xsl:attribute>
            <xsl:attribute name="schChannel"><xsl:value-of select="schChannel" /></xsl:attribute>
            <xsl:attribute name="schSubName"><xsl:value-of select="schSubName" /></xsl:attribute>
            <xsl:attribute name="value"><xsl:value-of select="action" /></xsl:attribute>
          <xsl:value-of select="schName" /> 
          </td>
          <td class='itemdataNoWrap' style="width:10%">
            <xsl:choose>
              <xsl:when test="schStatus = 'Waiting' or //@filter = 1">
                <xsl:value-of select="@start" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="schStatus" />
              </xsl:otherwise>
            </xsl:choose> 
          </td>
        </tr> 
      </xsl:template>
   
</xsl:stylesheet>