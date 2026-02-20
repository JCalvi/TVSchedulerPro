

var xmlhttp = false;
var lastChange = "";
var firstRun = true;

setInterval("makeRequester()", 10000);

function makeRequester()
{
	xmlhttp = false;
	
	if (window.XMLHttpRequest) // Mozilla, Safari,...
	{ 
		xmlhttp = new XMLHttpRequest();
		if (xmlhttp.overrideMimeType)
		{
            xmlhttp.overrideMimeType('text/html');
		}
	}
	else if (window.ActiveXObject)  // IE
	{
		try
		{
			xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
		}
		catch (e)
		{
			try
			{
				xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch (e){}
         }
	}

	if (!xmlhttp)
	{
		alert('Cannot create XMLHTTP instance');
		return false;
	}
	
	xmlhttp.onreadystatechange = triggered;
	xmlhttp.open("GET", "/servlet/SystemDataRes?action=01" + "&dummy=" + new Date().getTime());	
	xmlhttp.send(null);
		
	//alert("Request Sent");	
}
	
function triggered()
{	
	if (xmlhttp != false && xmlhttp.readyState == 4 && xmlhttp.status == 200)
	{
		//alert(xmlhttp.responseText + "  " + lastChange);
		if(firstRun == true)
		{
			firstRun = false;
			lastChange = xmlhttp.responseText;
		}
		else
		{
			if(lastChange != xmlhttp.responseText)
			{
			    lastChange = xmlhttp.responseText
    			//alert(xmlhttp.responseText);
    			location.reload(true);
    		}
    	}
    }
}


      
      
      
      