<!DOCTYPE xsl:stylesheet [ 
<!ENTITY nbsp "&#160;">
<!ENTITY space "<xsl:text> </xsl:text>">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template match="/">
		<html>
			<head>

<link rel="stylesheet" href="/css/category.css" type="text/css" />			
<style>
body
{
	font-family: Arial, Helvetica, sans-serif;
	background-color: #6F92FA;
	color: #FFFFFF;
	padding: 0px;
	margin: 0px;
}

table.epgTable
{
	FONT: 11px/15px Tahoma, Verdana, Arial, Helvetica;
	margin: 0px;
	padding: 0px;
	border: 0px none #000000;
	table-layout: fixed;
	width: 100%;
}

div.timeBlock
{
    width: 100%;
    height: 100%;
	border-left: 1px solid #0018a6;
    border-top: 1px solid #0018a6;
	text-align: left;
    padding: 0px 2px 0px 2px;
}

td.epgChannel
{
	padding: 5px 5px 5px 5px;
	BACKGROUND-COLOR: #1221B4;
}

td.epgBlank
{
	BACKGROUND-COLOR: #D5D6FF;
	border-left: 0px solid #0018a6;
    border-top: 0px solid #0018a6;
	overflow: hidden;
}

td.epgProgram
{
	border-left: 0px solid #0018a6;
    border-top: 1px solid #0018a6;
   
	text-align: left;
	vertical-align: top;

	overflow-x: hidden;
	overflow-y: hidden;
	overflow: hidden;

    cursor: pointer; cursor: hand;
    padding: 0px 0px 0px 0px;
}


span.programDesc
{
   font-size: 10px;
   color: #8FB2FF;
}

span.programTitle
{
   font-weight: bold;
   font-size: 14px;
}

span.programTime
{
	font-size: 12px;
	color: #FFFFFF;
}

td.epgClear {
	padding: 2px 2px 2px 2px;
	border: 0px none #000000;
}

table.epgSchedule
{
	border: 1px solid #000000;
}

td.epgSchedule
{
	text-align: center;
	BACKGROUND-COLOR: #FBEC43;
	border-top: 2px solid #FFFFFF;
	border-bottom: 2px solid #FFFFFF;
	border-left: 1px solid #FFFFFF;
	border-right: 1px solid #FFFFFF;		
}

td.epgScheduleFinished
{
	text-align: center;
	BACKGROUND-COLOR: #00FF00;
	border-top: 2px solid #FFFFFF;
	border-bottom: 2px solid #FFFFFF;
	border-left: 1px solid #FFFFFF;
	border-right: 1px solid #FFFFFF;	
}


td.epgScheduleRunning
{
	text-align: center;
	BACKGROUND-COLOR: #FF0000;
	border-top: 2px solid #FFFFFF;
	border-bottom: 2px solid #FFFFFF;
	border-left: 1px solid #FFFFFF;
	border-right: 1px solid #FFFFFF;		
}

td.epgScheduleMarkerCell
{
	text-align: center;
	border-top: 0px solid #FFFFFF;
	border-bottom: 0px solid #FFFFFF;
	border-left: 2px solid #FFFFFF;
	border-right: 2px solid #FFFFFF;		
}			
</style>


<xsl:comment>[if lt IE 7.0]&gt;
&lt;script src="/javascript/iepng.js"&gt;&lt;/script&gt;
&lt;![endif]</xsl:comment>


			<title>WS EPG - (<xsl:value-of select="//@date" />/<xsl:value-of select="//@month" />/<xsl:value-of select="//@year" />)</title>
			<script>
			
			   function notifyOfOverlap()
			   {
			      var doNotify = <xsl:value-of select="//@overlapDetected" />;
			      if(doNotify)
			      {
			         alert("There are overlapped items in this EPG page, this will cause\nthe layout to be offset and some items may not line up with the\ncorrect start and stop times even though they have the\ncorrect start and stop times for the program.\n\nThis error is usually cased by a problem with your EPG source data.\n\nThis warning can be turned off in the EPG Data Source settings page.");
			      }
			   }
				
               function highlightCell(oCell)
               {
                  oCell.style.backgroundColor = 'lightblue';
                  //oCell.style.color = 'black';
               }

               function lowlightCell(oCell)
               {
                  oCell.style.backgroundColor = '';
                  //oCell.style.color = '';
               }

               function toggleScheduleCell(oCell, actionID)
               {
                  var itemIcons = document.getElementById(actionID+"A");
                  
                  if(itemIcons.style.visibility == "hidden")
                  {
                     itemIcons.style.visibility = "visible";
                  }
                  else
                  {
                     itemIcons.style.visibility = "hidden";                  
                  }
               }
               
               function showTitle(urlToShow)
               {
                  var w = screen.availWidth;
                  var h = screen.availHeight;
               
                  var popW = 600;
                  var popH = 450;
               
                  var leftPos = (w - popW) / 2;
                  var topPos = (h - popH) / 2;
               
                  var newWindow = window.open(urlToShow ,'popup','width=' + popW + ',height=' + popH + ',top=' + topPos + ',left=' + leftPos +',scrollbars=yes,resizable=yes');
                  newWindow.focus();
               }
					
               function positionSchedules()
               {
                  var cell = document.getElementsByTagName("table");
                  var x = 0;
                 
                  while(cell[x] != null)
                  {
                     if(cell[x].getAttribute("type") == "ScheduleItem")
                     {
                        cell[x].style.top = cell[x].offsetTop - 30;
                     }
                     else if(cell[x].getAttribute("type") == "ScheduleItemActions")
                     {
                        cell[x].style.top = cell[x].offsetTop - 30;                   
                     }
                     
                     x++;
                  }
               }
               
               function saveScrollPosition()
               {
                  var scroll = getDocLeft();

                  var cookieID = document.location.href;
                  setCookie("EpgScrollTo", scroll, 32);
               }
               
               function loadScrollPosition()
               {
                  var scrollTo = <xsl:value-of select="//@scrollto" />;
                  if(scrollTo == -2)
                  {
                     var cookieID = document.location.href;
                     scrollTo = parseInt(readCookie("EpgScrollTo"));
                     setDocLeft(scrollTo);
                  }
                  else
                  {
                     var timeObject = document.getElementById("time" + scrollTo);
                     var scrollToX = parseInt(timeObject.offsetLeft);
                     scrollToX -= (window.document.body.clientWidth / 2) - (timeObject.clientWidth / 2);
                     setDocLeft(scrollToX);
                  }
               }
               
               function setCookie(cookieName, cookieValue, nDays)
               {
                  var today = new Date();
                  var expire = new Date();
                  
                  if (nDays==null || nDays==0)
                     nDays=1;
                  
                  expire.setTime(today.getTime() + 3600000*24*nDays);
                  
                  document.cookie = cookieName+"=" + escape(cookieValue) + ";expires="+expire.toGMTString();
               }
               
               function readCookie(cookieName)
               {
                  var theCookie=""+document.cookie;
                  var ind=theCookie.indexOf(cookieName);
                  
                  if (ind==-1 || cookieName=="")
                     return ""; 
                     
                  var ind1=theCookie.indexOf(';',ind);
                  
                  if (ind1==-1)
                     ind1=theCookie.length; 
                     
                  return unescape(theCookie.substring(ind+cookieName.length+1,ind1));
               }      
               
               function getDocLeft()
               {
					var x,y;
					if (self.pageYOffset) // all except Explorer
					{
						x = self.pageXOffset;
					}
					else if (document.documentElement &amp;&amp; document.documentElement.scrollTop)
					// Explorer 6 Strict
					{
						x = document.documentElement.scrollLeft;
					}
					else if (document.body) // all other Explorers
					{
						x = document.body.scrollLeft;
					} 
					return x;
			   }
			   
			   function setDocLeft(left)
			   {
					if (self.pageYOffset) // all except Explorer
					{
						self.pageXOffset = left;
					}
					else if (document.documentElement &amp;&amp; document.documentElement.scrollTop)
					// Explorer 6 Strict
					{
						document.documentElement.scrollLeft = left;
					}
					else if (document.body) // all other Explorers
					{
						document.body.scrollLeft = left;
					} 			   
			   }
			                  
               function relocateHeader()
               {
					document.getElementById("navBar").style.left = getDocLeft() + 10;
               }   
               
               window.setInterval("relocateHeader()", 500);
					
			</script>
			</head>
			<body onLoad="positionSchedules();loadScrollPosition();notifyOfOverlap();" onUnload="saveScrollPosition();">
			
            <div id="navBar" style="position: absolute; left:10px; top:5px; padding:5px;">
	            <table cellpadding="5">
		            <tr>
			            <td nowrap="1">
			            <font size="4"><a target="_top" href="/" style="text-decoration: none; color: #FFFFFF;">
			            <img src="/images/home.png" align="absmiddle" border="0" width="24" height="24" /> 
			            <b>Home</b></a> : </font>
			            </td>
			            
		               	<xsl:apply-templates select="//days" mode="title" />
		            </tr>
	            </table>            
			</div>			
			
			<br/><br/><br/>

			<table border='0' cellspacing='0' cellpadding='0' style="margin: 0px; padding: 0px;	border: 0px none #000000; table-layout: fixed; width: 100%">
				<tr>
				<td class='epgTimes' width="100px">&nbsp;</td>
				<td class='epgTimes' width="240" id="time6"><div class="timeBlock">6 am</div></td>
				<td class='epgTimes' width="240" id="time7"><div class="timeBlock">7 am</div></td>
				<td class='epgTimes' width="240" id="time8"><div class="timeBlock">8 am</div></td>
				<td class='epgTimes' width="240" id="time9"><div class="timeBlock">9 am</div></td>
				<td class='epgTimes' width="240" id="time10"><div class="timeBlock">10 am</div></td>
				<td class='epgTimes' width="240" id="time11"><div class="timeBlock">11 am</div></td>
				<td class='epgTimes' width="240" id="time12"><div class="timeBlock">12 pm</div></td>
				<td class='epgTimes' width="240" id="time13"><div class="timeBlock">1 pm</div></td>
				<td class='epgTimes' width="240" id="time14"><div class="timeBlock">2 pm</div></td>
				<td class='epgTimes' width="240" id="time15"><div class="timeBlock">3 pm</div></td>
				<td class='epgTimes' width="240" id="time16"><div class="timeBlock">4 pm</div></td>
				<td class='epgTimes' width="240" id="time17"><div class="timeBlock">5 pm</div></td>
				<td class='epgTimes' width="240" id="time18"><div class="timeBlock">6 pm</div></td>
				<td class='epgTimes' width="240" id="time19"><div class="timeBlock">7 pm</div></td>
				<td class='epgTimes' width="240" id="time20"><div class="timeBlock">8 pm</div></td>
				<td class='epgTimes' width="240" id="time21"><div class="timeBlock">9 pm</div></td>
				<td class='epgTimes' width="240" id="time22"><div class="timeBlock">10 pm</div></td>
				<td class='epgTimes' width="240" id="time23"><div class="timeBlock">11 pm</div></td>
				<td class='epgTimes' width="240" id="time0"><div class="timeBlock">12 am</div></td>
				<td class='epgTimes' width="240" id="time1"><div class="timeBlock">1 am</div></td>
				<td class='epgTimes' width="240" id="time2"><div class="timeBlock">2 am</div></td>
				<td class='epgTimes' width="240" id="time3"><div class="timeBlock">3 am</div></td>
				<td class='epgTimes' width="240" id="time4"><div class="timeBlock">4 am</div></td>
				<td class='epgTimes' width="240" id="time5"><div class="timeBlock">5 am</div></td>
				</tr>
			</table>
					
			<xsl:apply-templates select="//channel" mode="body" />

			</body>	
		
		</html>
	</xsl:template>	

   
	<xsl:template match="days" mode="title">
   
      <td>
      <a style="text-decoration: none; color: #FFFFFF; font-size: 15px">
      <xsl:attribute name="href">
         <xsl:value-of select="url" />
      </xsl:attribute>
		<xsl:value-of select="@name" /></a>
		
      <xsl:if test="position() != count(//days)"></xsl:if>
      </td>
      
	</xsl:template>
    
 	
	<xsl:template match="channel" mode="body">
	
      <xsl:variable name="filter" select="display-name"/>
      <xsl:variable name="colNum" select="position()"/>

		<table border='0' cellspacing='0' cellpadding='0' style="margin: 0px; padding: 0px; border: 0px none #000000; table-layout: fixed; width: 100%">
		    <tr>
		    
		    <td width="100px" valign="top">
            <xsl:attribute name="id">chan_<xsl:value-of select="$colNum" /></xsl:attribute>			    
		    <div style="font-weight: bold; font-size: 16px; overflow: hidden; height:100px; width: 100px">
				<xsl:value-of select="display-name" />
			</div>
		    </td>			    
	    
			<xsl:apply-templates select="//programme[@channel=$filter]" />
			
			</tr>
		</table>
					
		
		<xsl:apply-templates select="//schedule[@channel=$filter]">
			<xsl:with-param name="position" select="$colNum" />
		</xsl:apply-templates>

	</xsl:template>	
	
	
   
   <xsl:template match="programme">
      
         <td valign="Top" class="epgProgram" height="100px" onMouseOver="highlightCell(this);" onMouseOut="lowlightCell(this);" >
         
         <xsl:attribute name="class">epgProgram epgProgramDefault<xsl:apply-templates select="category" mode="class" /></xsl:attribute>
         
         <xsl:attribute name="onClick">showTitle('<xsl:value-of select="detailsUrl" />')</xsl:attribute>
   
            <xsl:attribute name="width">
                   <xsl:value-of select="length * 4" />
            </xsl:attribute>
            
            <xsl:attribute name="title">
                   <xsl:if test='string-length(sub-title) &gt; 0'>(<xsl:value-of select="sub-title" />)&space;</xsl:if> @ <xsl:value-of select="substring(@start,9,2)" />:<xsl:value-of select="substring(@start,11,2)" /> for <xsl:value-of select="length" /> min on <xsl:value-of select="@channel" />
            </xsl:attribute>
            
            <div>
            <xsl:attribute name="style">white-space: nowrap; overflow: hidden; width: 100%; border-left: 1px solid #0018a6;</xsl:attribute>
			<xsl:if test="ignored = 1">
			<img src="/images/stop.png" style="position:absolute; opacity:.50; -moz-opacity:.50; filter:alpha(opacity=50); height:50px"/>
			</xsl:if>           
            <span class="programTitle"><xsl:value-of select="title" /></span>
            </div>
            
            <div>
            <xsl:attribute name="style">white-space: nowrap; overflow: hidden; width: 100%; border-left: 1px solid #0018a6;</xsl:attribute>
            <span class="programTime"><xsl:value-of select="substring(@start,9,2)" />:<xsl:value-of select="substring(@start,11,2)" /> (<xsl:value-of select="length" />)</span><br/>
            </div>            

            <div>
            <xsl:attribute name="style">overflow: hidden; height: 100%; width: 100%; border-left: 1px solid #0018a6;</xsl:attribute>
            <span class="programDesc"><xsl:value-of select="desc" /></span>
            </div>

         </td>

   </xsl:template>   
   

   <xsl:template match="category" mode="class">
      &space;<xsl:if test='string-length(.) &gt; 0'><xsl:call-template name="remSpaces"><xsl:with-param name="string"><xsl:value-of select="." /></xsl:with-param></xsl:call-template></xsl:if><xsl:if test='string-length(.) = 0'>epgProgramNoCategory</xsl:if>
   </xsl:template>

      
    <xsl:template match="schedule">
        <xsl:param name="position"/>
        <xsl:variable name="filter" select="id"/>

        <table height="30" border="0" cellpadding="0" cellspacing="0" type="ScheduleItem">
            <xsl:attribute name="style">
                overflow:hidden; position:absolute;left:<xsl:value-of select="from_top * 4 + 100" />;width:<xsl:value-of select="@duration * 4" />;height:30px
            </xsl:attribute>
				
            <xsl:attribute name="id">
                <xsl:value-of select="id" />
            </xsl:attribute>
            
            <xsl:attribute name="colNum">
                <xsl:value-of select="$position"/>
            </xsl:attribute>          

            <tr>
               <td height="15" class="epgScheduleMarkerCell" style="overflow:hidden;"><div width="1px" height="1px" style="overflow: hidden; width: 1px; height: 1px;">&nbsp;</div></td>
            </tr>
            
            <tr>
                <td height="15" align="left" valign="Top" class="epgSchedule" style="overflow:hidden;">
				   <xsl:attribute name="onClick">
				     toggleScheduleCell(this, '<xsl:value-of select="id" />');
				   </xsl:attribute>	
            
	                <xsl:if test="itemState = 2">
	                    <xsl:attribute name="class">
	                       epgScheduleRunning
	                    </xsl:attribute>
	                </xsl:if> 
               
                <xsl:if test="itemState = 1 or itemState = 3">
                   <xsl:attribute name="class">
                      epgScheduleFinished
                   </xsl:attribute>
                </xsl:if>                
               
                 <xsl:attribute name="title"><xsl:value-of select="substring(@start,9,2)" />:<xsl:value-of select="substring(@start,11,2)" /> (<xsl:value-of select="@duration" />) <xsl:value-of select="@channel" />
                 </xsl:attribute>
                
                <div width="1px" height="1px" style="overflow: hidden; width: 1px; height: 1px;">&nbsp;</div>
                
				</td>
			</tr>
		</table>
		
		<table cellpadding="1" cellspacing="1" type="ScheduleItemActions">
		
			<xsl:attribute name="style">
				left:<xsl:value-of select="from_top * 4 + 100" />;position:absolute;BACKGROUND-COLOR: #3068FF;border:1px solid #FFFFFF;visibility:hidden;
			</xsl:attribute>	
			
			<xsl:attribute name="colNum">
				<xsl:value-of select="$position"/>
			</xsl:attribute>				
			
         <xsl:attribute name="id"><xsl:value-of select="id" />A</xsl:attribute>

			<tr>	
			   <td style="text-align:center;width:30px;padding: 2px 2px 2px 2px;">					
               
               <xsl:attribute name="id"><xsl:value-of select="id" />A</xsl:attribute>
               
               <xsl:if test="itemState = 0">WA</xsl:if>

               <xsl:if test="itemState = 1">SK</xsl:if>

               <xsl:if test="itemState = 2">RN</xsl:if>

               <xsl:if test="itemState = 3">FN</xsl:if>

               <xsl:if test="itemState = 4">AB</xsl:if>

               <xsl:if test="itemState = 5">ER</xsl:if>

               <xsl:apply-templates select="//actionUrl[@item_id=$filter]" />
               
			   </td>
			</tr>
		</table>

		
	</xsl:template>
   
   <xsl:template match="actionUrl">
   
      <a onClick="javascript:window.event.cancelBubble=true">
      <xsl:attribute name="href">
         <xsl:value-of select="url" />
      </xsl:attribute>
      <xsl:if test="@name = 'D'"><img src="/images/delete.png" border="0" align="center" width="24" height="24" /></xsl:if>
      <xsl:if test="@name = 'E'"><img src="/images/edit.png" border="0" align="center" width="24" height="24" /></xsl:if>
      <xsl:if test="@name = '+'"><img src="/images/+5.png" border="0" align="center" width="24" height="24" /></xsl:if>
      <xsl:if test="@name = 'S'"><img src="/images/stop.png" border="0" align="center" width="24" height="24" /></xsl:if>
      <xsl:if test="@name = 'L'"><img src="/images/showchildren.png" border="0" align="center" width="24" height="24" /></xsl:if>
      </a>

   </xsl:template>
   
	<xsl:template name="remSpaces">
		<xsl:param name="string" />
		<xsl:if test="contains($string, ' ')">
		<xsl:value-of select="substring-before($string, ' ')" /><xsl:call-template name="remSpaces">
		<xsl:with-param name="string"><xsl:value-of select="substring-after($string, ' ')" />
		</xsl:with-param>
		</xsl:call-template>
		</xsl:if>
		<xsl:if test="not(contains($string, ' '))"><xsl:value-of select="$string" />
		</xsl:if>
	</xsl:template>    

</xsl:stylesheet>

