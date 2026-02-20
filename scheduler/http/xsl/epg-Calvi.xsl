<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template match="/">
		<html>
			<head>
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
	table-layout:fixed;
	width: 100%
}

th.epgTimes
{
	padding: 0px 15px 0px 15px;

	border-top: 1px solid #5261f4;
    border-left: 1px solid #5261f4;
    border-right: 1px solid #5261f4;
}

a.epgTimes 
{
	color: White;
}

td.epgChannel
{
	padding: 5px 5px 5px 5px;
	BACKGROUND-COLOR: #1221B4;
}

td.epgBlank
{
	BACKGROUND-COLOR: #D5D6FF;
	border-left: 1px solid #0018a6;
    border-top: 1px solid #0018a6;
	overflow: hidden;
}

td.epgProgram
{  
	text-align: left;
	vertical-align: top;
	overflow: hidden;
    cursor: pointer; cursor: hand;
    padding: 0px 0px 0px 0px;
}

span.programDesc
{
   color: #8FB2FF;
}

span.programTitle
{
   font-weight: bold;
   font-size: 14px;
}

td.epgClear
{
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
	border-top: 2px solid #FFFFFF;
	border-bottom: 2px solid #FFFFFF;
	border-left: 0px solid #FFFFFF;
	border-right: 0px solid #FFFFFF;		
}
td.dayCell
{
	vertical-align: baseline;
}
td.epgProgramDefault
{
	background-color: #3068FF;
}
td.epgProgramNoCategory
{
	background-color: #3068FF;
}
td.epgProgramEmpty
{
	background-color: #3068FF;
}

/* Force text to wrap inside the table so it doesn't push the width out */
td.epgProgram, td.epgChannel, span.programTitle, span.programDesc {
    white-space: normal !important; /* Allow line breaks */
    word-wrap: break-word;         /* Break long words */
    word-break: break-all;         /* Aggressive breaking for very long strings */
}

</style>
<link rel="stylesheet" href="/css/category.css" type="text/css" />


<xsl:comment>[if lt IE 7.0]&gt;
&lt;script src="/javascript/iepng.js"&gt;&lt;/script&gt;
&lt;![endif]</xsl:comment>


			<title>WS EPG - (<xsl:value-of select="//@date" />/<xsl:value-of select="//@month" />/<xsl:value-of select="//@year" />)</title>
			<script src="/javascript/refresh.js"></script>
			<script>
			
			
			   var totalColNum = <xsl:value-of select="count(//channel)" />;
				
               function highlightCell(oCell)
               {
                  oCell.style.backgroundColor = '#071D5C';
               }

               function lowlightCell(oCell)
               {
                  oCell.style.backgroundColor = '';
               }

               function highlightScheduleCell(oCell)
               {
                  oCell.style.filter = 'alpha(opacity=100)';
                  oCell.style.opacity = '1';
               }

               function lowlightScheduleCell(oCell)
               {
                  oCell.style.filter = 'alpha(opacity=50)';
                  oCell.style.opacity = '0.5';
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

                  var browserName=navigator.vendor;
                  var mainTable = document.getElementById("mainTable");
                  var mainDiv = document.getElementById("mainDiv");
                  var programTable = document.getElementById("programTable");
                  // iPad Detection
                  if (navigator.vendor == "Apple Computer, Inc.")
                  { 
                    mainDiv.style.height = programTable.clientHeight + 400;
                  }
                  else
                  { 
                    mainDiv.style.height = window.document.body.clientHeight - mainDiv.offsetTop;
                  }
                  mainTable.style.width = programTable.clientWidth;
                  //alert(navigator.vendor) ;
               }
               
               function saveScrollPosition()
               {
                  var scroll = document.body.scrollTop;
                  var item = document.getElementById("mainDiv");
                  scroll = item.scrollTop;

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
                     var item = document.getElementById("mainDiv");
                     item.scrollTop = scrollTo;
                     
                  }
                  else
                  {
                     document.getElementById("time" + scrollTo).scrollIntoView();
                  }
               }

               function scrollToNow()
               {
                  var now = new Date();
                  document.getElementById("time" + now.getHours()).scrollIntoView();
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
					
				</script>
			</head>
			<body scroll="no" onResize="positionSchedules();" onLoad="positionSchedules();loadScrollPosition();" onUnload="saveScrollPosition();">

			<table id="mainTable" cellpadding="0" cellspacing="0" style="FONT-SIZE: 12px; TABLE-LAYOUT: Fixed; Border:0px solid black; border-collapse:collapse;width:100%"><tr><td align="center">
         
  			<table class="epgTable" cellspacing='0' cellpadding='0'>
              <tr>
              <th valign="center">
              
              <div style="position:absolute; left:0px; top:10px; padding:5px;">
              <a href="/">
              <img src="/images/home.png"  align="absmiddle" border="0" width="24" height="24" /> 
              </a></div>
  
              <div style="position:absolute; left:40px; top:10px; padding:5px;">
              <a href="/servlet/EpgDataRes?action=12&amp;scrollto=-1">
              <img src="/images/reload.png" align="absmiddle" border="0" width="24" height="24" /> 
              </a></div>
  
              <div style="position:absolute; left:80px; top:10px; padding:5px;">
              <a href="/servlet/EpgDataRes?action=22">
              <img src="/images/log.png" align="absmiddle" border="0" width="24" height="24" /> 
              </a></div>
              
              <div style="font-size: 20px; padding: 0px; color: #FFFFFF;">
              <table align="center" cellpadding="5">
              <tr>
                 <td class="dayCell">
                    <font size="5 px"><xsl:value-of select="//@day" /> </font> <font size="4 px"> (<xsl:value-of select="//@date" />/<xsl:value-of select="//@month" />)</font> <br/>
                 </td>
                 <xsl:apply-templates select="//days" mode="title" />
              </tr>
              </table>
              </div>
            
  				    </th></tr>
          </table>
        
          <table class="epgTable" cellspacing='0' cellpadding='0'>
  					<colgroup>
  						<col width="75" />
  						<xsl:apply-templates select="//channel" mode="columns" />
  					</colgroup>
  					<tr>
  						<th>
  						</th>
  						<xsl:apply-templates select="//channel" mode="header" />
  					</tr>
  				</table>
  				
  				</td></tr>
				</table>
				
				
				<div id="mainDiv" style="width:100%;height:10px;overflow:auto;position:relative;">

				<table cellpadding="0" cellspacing="0" class='epgTable' id="programTable">
					<colgroup>
						<col width="75" />
						<xsl:apply-templates select="//channel" mode="columns" />
					</colgroup>
					<tr>
						<td valign="top">
							<table class='epgTable' border='0' cellspacing='0' cellpadding='0' name='epgtable'>
								<tr>

									<th class='epgTimes' height="120" id="time6">6 am</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time7">7 am</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time8">8 am</th>

								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time9">9 am</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time10">10 am</th>
								</tr>
								<tr>

									<th class='epgTimes' height="120" id="time11">11 am</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time12">12 pm</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time13">1 pm</th>

								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time14">2 pm</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time15">3 pm</th>
								</tr>
								<tr>

									<th class='epgTimes' height="120" id="time16">4 pm</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time17">5 pm</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time18">6 pm</th>

								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time19">7 pm</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time20">8 pm</th>
								</tr>
								<tr>

									<th class='epgTimes' height="120" id="time21">9 pm</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time22">10 pm</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time23">11 pm</th>

								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time0">12 am</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time1">1 am</th>
								</tr>
								<tr>

									<th class='epgTimes' height="120" id="time2">2 am</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time3">3 am</th>
								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time4">4 am</th>

								</tr>
								<tr>
									<th class='epgTimes' height="120" id="time5">5 am</th>
								</tr>
							</table>
						</td>
						<xsl:apply-templates select="//channel" mode="body" />
                  

					</tr>

				</table>
				<xsl:apply-templates select="//nowLine" />
				</div>
			
			</body>	
		</html>
	</xsl:template>	


    <xsl:template match="nowLine">
        <!--<div id="nowLine" title="Current Time" style="position: absolute; top: 200px; left: 5px; width: 99%; height: 3px; border: #F1FF77 1px solid; overflow: hidden"></div>-->
        <div title="Current Time">
          <xsl:if test="@hour &lt; 6">
              <!--<xsl:attribute name="style">top: <xsl:value-of select="(((@hour + 24 - 6) * 60) + @minute) * 2" />px; position: absolute; left: 5px; width: 99%; height: 3px; border: #F1FF77 1px solid; overflow: hidden</xsl:attribute>-->
              <xsl:attribute name="style">top: <xsl:value-of select="(((@hour + 24 - 6) * 60) + @minute) * 2" />px; position: absolute; left: 5px; width: 99%; height: 5px; background-color: #D5D6FF; border: none; overflow: hidden; filter:alpha(opacity=50);opacity: 0.5;-moz-opacity:0.5;</xsl:attribute>
           </xsl:if>
           <xsl:if test="@hour &gt; 5">
              <!-- <xsl:attribute name="style">top: <xsl:value-of select="(((@hour - 6) * 60 ) + @minute) * 2" />px; position: absolute; left: 5px; width: 99%; height: 3px; border: #F1FF77 1px solid; overflow: hidden</xsl:attribute>-->
              <xsl:attribute name="style">top: <xsl:value-of select="(((@hour - 6) * 60 ) + @minute) * 2" />px; position: absolute; left: 5px; width: 99%; height: 5px; background-color: #D5D6FF; border: none; overflow: hidden; filter:alpha(opacity=50);opacity: 0.5;-moz-opacity:0.5;</xsl:attribute>
           </xsl:if>           
        </div>
    </xsl:template>
    
	<xsl:template match="days" mode="title">
   
      <td class="dayCell">
      <a style="text-decoration: none; color: #FFFFFF; font-size: 15px">
      <xsl:attribute name="href">
         <xsl:value-of select="url" />
      </xsl:attribute>
		<xsl:value-of select="@name" /></a>
		
      <xsl:if test="position() != count(//days)"></xsl:if>
      </td>
      
	</xsl:template>
   
	<xsl:template match="channel" mode="columns">
		<col />
	</xsl:template>
   
	<xsl:template match="channel" mode="header">
		<th style="padding: 2px; background-color: white; color: #18347F; border-left: 1px solid #5261f4;">
		  <xsl:call-template name="output-tokens">
			<xsl:with-param name="list"><xsl:value-of select="display-name" /></xsl:with-param>
		  </xsl:call-template>
		</th>
	</xsl:template>
	
	<xsl:template name="output-tokens">
       <xsl:param name="list" />
       <xsl:variable name="newlist" select="concat(normalize-space($list), ' ')" />
       <xsl:variable name="first" select="substring-before($newlist, ' ')" />
       <xsl:variable name="remaining" select="substring-after($newlist, ' ')" />
		   <span style="font-weight: bold; font-size: {20 -  (1.25 * string-length($first))};">
			   <dnline><xsl:value-of select="$first" /></dnline>
		   </span>
       <xsl:if test="$remaining">
           <xsl:call-template name="output-tokens">
               <xsl:with-param name="list" select="$remaining" />
           </xsl:call-template>
       </xsl:if>
   </xsl:template>
   
	<xsl:template match="channel" mode="body">
      <xsl:variable name="filter" select="display-name"/>
      <xsl:variable name="colNum" select="position()"/>
		<td valign="Top" align="right" style="background-color: #6F92FA;">
		<div style="position: relative; width: 100%;" align="left">
			<xsl:apply-templates select="//programme[@channel=$filter]" />
			<xsl:apply-templates select="//schedule[@channel=$filter]">
			<xsl:with-param name="position" select="$colNum" />
			</xsl:apply-templates>
		</div>	
		</td>
	</xsl:template>
   
   <xsl:template match="programme">
   <div>
    <xsl:attribute name="onClick">showTitle('<xsl:value-of select="detailsUrl" />')</xsl:attribute>
    
    <xsl:attribute name="height">
         <xsl:value-of select="length * 2" />
    </xsl:attribute>
    <xsl:attribute name="title">
         <xsl:value-of select="title" /><xsl:if test='string-length(sub-title) &gt; 1'>(<xsl:value-of select="sub-title" />)</xsl:if> @ <xsl:value-of select="substring(@start,9,2)" />:<xsl:value-of select="substring(@start,11,2)" /> for <xsl:value-of select="length" /> min on <xsl:value-of select="@channel" />
    </xsl:attribute>
    
    <xsl:attribute name="style">OVERFLOW: hidden; position: absolute; top: <xsl:value-of select="(substring(@start,9,2) * 120 + substring(@start,11,2) * 2 + 2160) mod 2880" />px; height:<xsl:value-of select="length * 2" />px;</xsl:attribute>
    <table width="100%" height="100%"  cellpadding="0" cellspacing="0" style="font-size: 11px; border: 1px solid; border-color: #18347F; border-right-width: 0px; border-bottom-width: 0px;">
      <tr>
        <td align="left" valign="top" onMouseOver="highlightCell(this);" onMouseOut="lowlightCell(this);">
        <xsl:attribute name="class">epgProgram epgProgramDefault <xsl:if test='string-length(category ) &gt; 0'><xsl:call-template name="remSpaces"><xsl:with-param name="string"><xsl:value-of select="category" /></xsl:with-param></xsl:call-template></xsl:if><xsl:if test='string-length(category ) = 0'>epgProgramNoCategory</xsl:if></xsl:attribute>
		<xsl:if test="ignored = 1">
		<img src="/images/stop.png" style="position:absolute; opacity:.50; -moz-opacity:.50; filter:alpha(opacity=50); width:75%"/>
		</xsl:if>		
        <span class="programTitle"><xsl:value-of select="title" /></span><br/>
        <span class="programDesc"><xsl:value-of select="substring(@start,9,2)" />:<xsl:value-of select="substring(@start,11,2)" /> (<xsl:value-of select="length" />) </span><xsl:if test="sub-title!=''"><span class="programDesc"> <xsl:value-of select="sub-title" /></span></xsl:if><br/>
        <span class="programDesc"><xsl:value-of select="desc" /></span>
        </td>
      </tr>
    </table>
   </div>
   </xsl:template>   
   
    <xsl:template match="schedule">
        <xsl:param name="position"/>
        <xsl:variable name="filter" select="id"/>

        <table cellpadding="0" cellspacing="0" type="ScheduleItem">
            <xsl:attribute name="style">
                position:absolute;top:<xsl:value-of select="from_top * 2" />;height:<xsl:value-of select="@duration * 2" />;width:50%; right: 0px; filter:alpha(opacity=50);opacity: 0.5;-moz-opacity:0.5;
            </xsl:attribute>
			
            <xsl:attribute name="colNum">
               <xsl:value-of select="$position"/>
            </xsl:attribute>		
				
            <xsl:attribute name="id">
                <xsl:value-of select="id" />
            </xsl:attribute>

            <tr>
              <td width="25%" class="epgScheduleMarkerCell">&nbsp;</td>
              <td align="left" valign="Top" class="epgSchedule" onMouseOver="highlightScheduleCell(this.offsetParent);" onMouseOut="lowlightScheduleCell(this.offsetParent);">
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
              
              <xsl:attribute name="height">
                <xsl:value-of select="@duration * 2" />
              </xsl:attribute>
              
              <xsl:attribute name="title"><xsl:value-of select="substring(@start,9,2)" />:<xsl:value-of select="substring(@start,11,2)" /> (<xsl:value-of select="@duration" />) <xsl:value-of select="@channel" />
              </xsl:attribute>&nbsp;
				      </td>
			     </tr>
		    </table>
		
    		<table cellpadding="0" cellspacing="0" type="ScheduleItemActions">
    		
    			<xsl:attribute name="style">
    				top:<xsl:value-of select="from_top * 2" />;position:absolute;BACKGROUND-COLOR: #3068FF;border:1px solid #FFFFFF;visibility:hidden;right:38px
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

