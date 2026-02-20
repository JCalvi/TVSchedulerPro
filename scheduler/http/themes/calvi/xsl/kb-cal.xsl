<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
   
  <html>
  <head>
  <title>Timer Control</title>
  <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css"/>
  <script src="/javascript/jumpBack.js"></script>
  <script language="JavaScript">
    var dateSelected = <xsl:value-of select="//@selectedDay" />;
    var nextMonth = <xsl:value-of select="//@nextMonth" />;
    var nextYear = <xsl:value-of select="//@nextYear" />;
    var pervMonth = <xsl:value-of select="//@prevMonth" />;
    var prevYear = <xsl:value-of select="//@prevYear" />;
    var id = escape("<xsl:value-of select="//@itemID" />");
    var index = escape("<xsl:value-of select="//@index" />");
    var backURL = escape("<xsl:value-of select="//backURL" />");
    var destination = "";   
  
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
      setActiveDate();
      document.title = "page ready";
    }   
    
    function onKeyDownEvent(nKeyCode)
    {
      var keyCode = nKeyCode.keyCode;
      //alert(keyCode);
    
      if(keyCode == 39) //right
      {
         addDays(1);
      }
      else if	(keyCode == 37) //left
      {
         addDays(-1);
      }
      else if	(keyCode == 38) //up
      {
         addDays(-7);
      }
      else if	(keyCode == 40) //down
      {
         addDays(7);
      }
      else if (keyCode == 13)
      {
         document.location.href = destination;
      }
      else if (keyCode == 36 || keyCode == 27)
      {
         document.location.href= "javascript:jumpBack();";
      }
      else if(keyCode == 33)
      {
         var url = "/servlet/KBScheduleDataRes?action=01&amp;day=" + dateSelected + "&amp;month=" + pervMonth + "&amp;year=" + prevYear;
         if(id.length > 0)
            url += "&amp;id=" + id;
         if(index.length > 0)
            url += "&amp;index=" + index;
         if(backURL.length > 0)
            url += "&amp;url=" + backURL;
                        
         document.location.href = url;
      }
      else if(keyCode == 34)
      {
         var url = "/servlet/KBScheduleDataRes?action=01&amp;day=" + dateSelected + "&amp;month=" + nextMonth + "&amp;year=" + nextYear;
         if(id.length > 0)
            url += "&amp;id=" + id;
         if(index.length > 0)
            url += "&amp;index=" + index;  
         if(backURL.length > 0)
            url += "&amp;url=" + backURL;
                                  
         document.location.href = url;
      }
      return false;
    }
    
    function addDays(amount)
    {
      var cell = document.getElementById("date_" + dateSelected);
      if(cell)
      {
         cell.className = "calendarday";
      }
    
      dateSelected += amount;
      if(dateSelected &lt; 1) dateSelected = dateSelected -= amount;;
    
      cell = document.getElementById("date_" + dateSelected);
      if(cell)
      {
         destination = cell.getAttribute("value");
         cell.className = "calendarSelected";
      }
      else
      {
         dateSelected -= amount;
         cell = document.getElementById("date_" + dateSelected);
         if(cell)
         {
            destination = cell.getAttribute("value");
            cell.className = "calendarSelected";
         }
      }
    }   
    
    function setActiveDate()
    {
    
      var cell = document.getElementById("date_" + dateSelected);
      
      if(cell)
      {
         destination = cell.getAttribute("value");
         cell.className = "calendarSelected";
      }
    }
  </script>   
  
  </head>
  <body onload="Initialize();">
  
  <table class='KBButtonTable'>
    <tr><td align="center" valign="middle">
      <table class='calendarTable' >
        <tr>
          <td class='calendarTitle' colspan='7'><xsl:value-of select="//@description" /></td>
        </tr>
        <tr>
          <xsl:apply-templates select="//dayNames" mode="cells" />
        </tr>
        <tr>
          <xsl:apply-templates select="//day[@week=1]" mode="cells" />
        </tr>
        <tr>
          <xsl:apply-templates select="//day[@week=2]" mode="cells" />
        </tr>
        <tr>
          <xsl:apply-templates select="//day[@week=3]" mode="cells" />
        </tr>
        <tr>
          <xsl:apply-templates select="//day[@week=4]" mode="cells" />
        </tr>
        <tr>
          <xsl:apply-templates select="//day[@week=5]" mode="cells" />
        </tr>   
        <tr>
          <xsl:apply-templates select="//day[@week=6]" mode="cells" />
        </tr>    
        <tr>
          <td class='calendarPrev' colspan='3'>(PageUP) Prev</td> 
          <td colspan='1'></td>
          <td class='calendarNext' colspan='3'>Next (PageDown)</td>
        </tr>
      </table>
    </td></tr>
  </table>
  
  </body>
  </html>   
   
	</xsl:template>

	<xsl:template match="dayNames" mode="cells">
    <td class='calendarweek'>
      <xsl:value-of select="name" />
    </td>
	</xsl:template>   
   
	<xsl:template match="day" mode="cells">
  <td class="calendarday">
    <xsl:if test="@date != 0">
      <xsl:attribute name="id">date_<xsl:value-of select="@date" /></xsl:attribute>
      <xsl:attribute name="value"><xsl:value-of select="url" /></xsl:attribute>
    </xsl:if>
    <span class="solidText">
      <xsl:if test="@date != 0">
        <xsl:value-of select="@date" />
      </xsl:if>
      <xsl:if test="@date = 0">
        &#160;
      </xsl:if>
    </span>
  </td>
	</xsl:template>
   
</xsl:stylesheet>

