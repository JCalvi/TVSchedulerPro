var autoDel = null;
var keepFor = 0;
var postTaskList = null;
var filePatternList = null;

var startBuffer = 0;
var endBuffer = 0;
var captype = 0;
var capPath = 0;
var postTask = 0;
var patternIndex = 0;

var capTypeNames = null;
var capTypeValues = null;

var capturePathNames = null;
var capturePathValues = null;

var inputValues = null;
var inputNames = null;
var inputSelected = 0;

function getCapPathList()
{
   capturePathNames = new Array();
   capturePathValues = new Array();

   var item = document.getElementById("capturePaths");
   if(item == null)
      return;

   var index = 0;
   var name = item.getAttribute("option_" + index);
   var value = item.getAttribute("value_" + index);
   while(name != null && name != "" && value != null && value != "")
   {
      capturePathNames[index] = name;
      capturePathValues[index] = value;

      index++;
      name = item.getAttribute("option_" + index);
      value = item.getAttribute("value_" + index);
   }
}

function getCapturePathValue()
{
   var item = document.getElementById("capturePaths");
   if(item == null) return;
   var value = item.getAttribute("value");

   if(value == null && value == "")
      return;

   for(var index = 0; index < capturePathValues.length; index++)
   {
      if(capturePathValues[index] == value)
         return index;
   }
}

function getCapTypeValue()
{
   var item = document.getElementById("captype");
   if(item == null) return;
   var value = item.getAttribute("value");

   if(value == null && value == "")
      return;

   for(var index = 0; index < capTypeValues.length; index++)
   {
      if(capTypeValues[index] == value)
         return index;
   }
}

function getAutoDelValue()
{
   var item = document.getElementById("autoDel");
   if(item == null) return;
   var value = item.getAttribute("value");
   if(value != null && value != "") autoDel = value;
}

function getkeepForValue()
{
   var item = document.getElementById("keepfor");
   if(item == null) return;
   var value = item.getAttribute("value");
   if(value != null && value != "") keepFor = value;
}

function getStartBufferValue()
{
   var item = document.getElementById("startBuffer");
   if(item == null) return;
   var value = item.getAttribute("value");
   if(value != null && value != "") startBuffer = value;
}

function getEndBufferValue()
{
   var item = document.getElementById("endBuffer");
   if(item == null) return;
   var value = item.getAttribute("value");
   if(value != null && value != "") endBuffer = value;
}

function getPostTaskArray()
{
   postTaskList = new Array();

   var item = document.getElementById("postTask");
   if(item == null)
      return;

   var index = 0;
   var option = item.getAttribute("option_" + index);
   while(option != null && option != "")
   {
      postTaskList[index] = option;
      index++;
      option = item.getAttribute("option_" + index);
   }
}

function getCapTypeList()
{
   capTypeNames = new Array();
   capTypeValues = new Array();

   var item = document.getElementById("captype");
   if(item == null)
      return;

   var index = 0;
   var name = item.getAttribute("option_" + index);
   var value = item.getAttribute("value_" + index);
   while(name != null && name != "" && value != null && value != "")
   {
      capTypeNames[index] = name;
      capTypeValues[index] = value;

      index++;
      name = item.getAttribute("option_" + index);
      value = item.getAttribute("value_" + index);
   }
}

function getNamePatterArray()
{
   filePatternList = new Array();

   var item = document.getElementById("filenamePatterns");
   if(item == null)
      return;

   var index = 0;
   var option = item.getAttribute("option_" + index);
   while(option != null && option != "")
   {
      filePatternList[index] = option;
      index++;
      option = item.getAttribute("option_" + index);
   }
}

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
   getkeepForValue();
   getStartBufferValue();
   getEndBufferValue();
   getAutoDelValue();

   getPostTaskArray();
   postTask = getPostTaskIndex();

   getNamePatterArray();
   patternIndex = getPatternIndex();

   getCapTypeList();
   captype = getCapTypeValue();

   getCapPathList();
   capPath = getCapturePathValue();

   inputValues = new Array(parseInt(startBuffer), parseInt(endBuffer), parseInt(captype), autoDel, parseInt(keepFor), parseInt(postTask), parseInt(patternIndex), parseInt(capPath));
   inputNames = new Array("startBuffer", "endBuffer",  "captype", "autoDel", "keepfor", "postTask", "filenamePatterns", "capturePaths");

   addEvent(document, "keydown", onKeyDownEvent);

   inputSelected = 0;
     updateAmount(0, 0, 59);
   inputSelected = 1;
     updateAmount(0, 0, 400);
   inputSelected = 2;
     updateCapType(0);

   inputSelected = 4;
     updatekeepFor(0);
   inputSelected = 5;
     updatePostTask(0);

   inputSelected = 6;
     updateNamePattern(0);

   inputSelected = 7;
     updateCapPath(0);

   inputSelected = 0;
   selectInput();
}

function onKeyDownEvent(evt)
{
  if(evt.keyCode == 40) // down
  {
    unSelectInput();
    inputSelected++;
    if(inputSelected >= inputNames.length) inputSelected = 0;
    selectInput();
  }
  else if(evt.keyCode == 38)
  {
    unSelectInput();
    inputSelected--;
    if(inputSelected < 0)  inputSelected = inputNames.length-1;
    selectInput();
  }
  else if(evt.keyCode == 39) // right
  {
    if(inputSelected == 0)
      updateAmount(1, 0, 59);
    else if(inputSelected == 1)
      updateAmount(5, 0, 400);
    else if(inputSelected == 2)
      updateCapType(1);
    else if(inputSelected == 3)
      updateAutoDel();
    else if(inputSelected == 4)
      updatekeepFor(1);
    else if(inputSelected == 5)
      updatePostTask(1);
    else if(inputSelected == 6)
      updateNamePattern(1);
    else if(inputSelected == 7)
      updateCapPath(1);
  }
  else if(evt.keyCode == 37) // left
  {
    if(inputSelected == 0)
      updateAmount(-1, 0, 59);
    else if(inputSelected == 1)
      updateAmount(-5, 0, 400);
    else if(inputSelected == 2)
      updateCapType(-1);
    else if(inputSelected == 3)
      updateAutoDel();
    else if(inputSelected == 4)
      updatekeepFor(-1);
    else if(inputSelected == 5)
      updatePostTask(-1);
    else if(inputSelected == 6)
      updateNamePattern(-1);
    else if(inputSelected == 7)
      updateCapPath(-1);
  }
  else if(evt.keyCode == 13)
  {
      document.data.startbuffer.value = inputValues[0];
      document.data.endbuffer.value = inputValues[1];
      document.data.captype.value = capTypeValues[inputValues[2]];
      document.data.autoDel.value = inputValues[3];
      document.data.keepFor.value = inputValues[4];
      document.data.task.value = postTaskList[inputValues[5]];
      document.data.filenamePatterns.value = filePatternList[inputValues[6]];
      document.data.capPath.value = capturePathValues[inputValues[7]];
      document.data.index.value = index;
      document.data.start.value = start;
      document.data.show.value = show;

      if(referrer.length > 0)
         document.data.url.value = referrer;

      document.data.submit();

    }
    else if (evt.keyCode == 36 || evt.keyCode == 27) // home and esc
      document.location.href= referrer;

}

function unSelectInput()
{
	var cell = document.getElementById(inputNames[inputSelected]);
	if(cell)
	{
		cell.className = "FormInput";
	}
}

function selectInput()
{
	var cell = document.getElementById(inputNames[inputSelected]);
	if(cell)
	{
		cell.className = "FormInputSelected";
	}
}

function updateAmount(amount, min, max)
{
  var cell = document.getElementById(inputNames[inputSelected]);
  if(cell)
  {
    inputValues[inputSelected] += amount;
    if(inputValues[inputSelected] < min) inputValues[inputSelected] = min;
    if(inputValues[inputSelected] > max) inputValues[inputSelected] = max;

    cell.innerHTML = "<span class='solidText'>" + intToStr(inputValues[inputSelected]) + "</span>";
  }
}

function updateCapType(amount)
{
  var cell = document.getElementById(inputNames[inputSelected]);
  if(cell)
  {
    inputValues[inputSelected] += amount;
    if(inputValues[inputSelected] < 0) inputValues[inputSelected] = capTypeNames.length-1;
    if(inputValues[inputSelected] >= capTypeNames.length) inputValues[inputSelected] = 0;

    cell.innerHTML = "<span class='solidText'>" + capTypeNames[inputValues[inputSelected]] + "</span>";
  }
}

function updateCapPath(amount)
{
  var cell = document.getElementById(inputNames[inputSelected]);
  if(cell)
  {
    inputValues[inputSelected] += amount;
    if(inputValues[inputSelected] < 0) inputValues[inputSelected] = capturePathNames.length-1;
    if(inputValues[inputSelected] >= capturePathNames.length) inputValues[inputSelected] = 0;

    cell.innerHTML = "<span class='solidText'>" + capturePathNames[inputValues[inputSelected]] + "</span>";
  }
}

function updateAutoDel()
{
  var cell = document.getElementById(inputNames[inputSelected]);

  if(cell)
  {
    if(inputValues[inputSelected] == "True")
      inputValues[inputSelected] = "False";
    else
      inputValues[inputSelected] = "True";
    cell.innerHTML = "<span class='solidText'>" + inputValues[inputSelected] + "</span>";
  }

}

function updatekeepFor(amount)
{
  var cell = document.getElementById(inputNames[inputSelected]);
  if(cell)
  {
    inputValues[inputSelected] += amount;
    if(inputValues[inputSelected] < 0) inputValues[inputSelected] = 0;
    if(inputValues[inputSelected] > 60) inputValues[inputSelected] = 60;

    cell.innerHTML = "<span class='solidText'>" + inputValues[inputSelected] + " days</span>";
  }
}

function updatePostTask(amount)
{
  var cell = document.getElementById(inputNames[inputSelected]);
  if(cell)
  {
    inputValues[inputSelected] += amount;
    if(inputValues[inputSelected] < 0) inputValues[inputSelected] = postTaskList.length-1;
    if(inputValues[inputSelected] >= postTaskList.length) inputValues[inputSelected] = 0;

    cell.innerHTML = "<span class='solidText'>" + postTaskList[inputValues[inputSelected]] + "</span>";
  }
}

function updateNamePattern(amount)
{
  var cell = document.getElementById(inputNames[inputSelected]);
  if(cell)
  {
    inputValues[inputSelected] += amount;
    if(inputValues[inputSelected] < 0) inputValues[inputSelected] = filePatternList.length-1;
    if(inputValues[inputSelected] >= filePatternList.length) inputValues[inputSelected] = 0;

    cell.innerHTML = "<span class='solidText'>" + filePatternList[inputValues[inputSelected]] + "</span>";
  }
}

function getPostTaskIndex()
{
   var item = document.getElementById("postTask");
   if(item == null) return;

   var value = item.getAttribute("value");

   for(var x = 0; x < postTaskList.length; x++)
   {
      if(postTaskList[x] == value)
         return x;
   }
   return 0;
}

function getPatternIndex()
{
   var item = document.getElementById("filenamePatterns");
   if(item == null) return;

   var value = item.getAttribute("value");

   for(var x = 0; x < filePatternList.length; x++)
   {
      if(filePatternList[x] == value)
         return x;
   }
   return 0;
}

function intToStr(number)
{
   if(number < 10)
      return "0" + number;
   else
      return number;
}
