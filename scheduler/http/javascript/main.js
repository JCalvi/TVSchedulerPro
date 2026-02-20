

function showLayer(layerID)
{
	var layer = document.getElementById(layerID);
	layer.style.visibility = 'visible';
}

function hideLayer(layerID)
{
	var layer = document.getElementById(layerID);
	layer.style.visibility = 'hidden';
}

function setTimes()
{
	var now = new Date();
	//alert("Test " + now.getMinutes());

	document.add.hour.value = now.getHours();
	document.add.min.value = now.getMinutes();
}

function isChannelSelected()
{
   if(document.add.channel.length == null)
   {
      if(document.add.channel.checked == true)
         return true;
      else
      {
         alert("You must select a channel");
         return false;
      }
   }
   
	var selected = false;
	var x = 0;
	for(x = 0; x < document.add.channel.length; x++)
	{
		if(document.add.channel[x].checked == true)
		{
			selected = true;
			break;
		}
	}

	if(selected == true)
		return selected;
	else
	{
		alert("You must select a channel");
		return selected;
	}
}
function openDetails(url)
{
   var w = screen.availWidth;
   var h = screen.availHeight;

   var popW = 500;
   var popH = 450;

   var leftPos = (w - popW) / 2;
   var topPos = (h - popH) / 2;

   var popupWin = window.open(url ,'popup','width=' + popW + ',height=' + popH + ',top=' + topPos + ',left=' + leftPos +',scrollbars=yes,resizable=yes');
   popupWin.focus();
}

function showHelp(name)
{
   var w = screen.availWidth;
   var h = screen.availHeight;

   var popW = 500;
   var popH = 300;

   var leftPos = (w - popW) / 2;
   var topPos = (h - popH) / 2;

   var fileName = "/docs/popup/" + name + ".html"
   var popupWin = window.open(fileName ,'help_popup','width=' + popW + ',height=' + popH + ',top=' + topPos + ',left=' + leftPos +',scrollbars=yes,resizable=yes');
   popupWin.focus();
}

function changeImage(imageOvered, newImage)
{
   imageOvered.src = newImage
}

function scrollToPos(toThis)
{
   var layer = null;
   var framed = false;
   
   if(parent.frames["mainFrame"] != null)
   {
      framed = true;
      layer = parent.frames["mainFrame"].document.getElementById("hour" + toThis);
   }
   else
      layer = document.getElementById("hour" + toThis);
      
   var iW = document.body.clientWidth;
   var iH = document.body.clientHeight;

   if(layer != null)
   {
      var scrolTo = layer.offsetLeft - (iW/2);
      
      if(framed)
         parent.frames["mainFrame"].window.scroll(scrolTo,0);
      else
         window.scroll(scrolTo,0);
         
   }
   else
   {
      alert("Not Found");
   }
   
}

