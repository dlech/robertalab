thymio = [];
thymio.source = null;
thymio.aseba = false;
thymio.robot = false;
thymio.executing = false;

thymio.sendRequest = function(url, callback, method, payload)
{
	if(typeof method === "undefined") {
		method = "GET";
	}
	
	if(typeof payload === "undefined") {
		payload = "";
	}
	
	var req = new XMLHttpRequest();
	if(!req) {
		return;
	}
	req.open(method, url, true);
	req.setRequestHeader('Content-type','application/x-www-form-urlencoded');
	req.onreadystatechange = function () {
		if(req.readyState != 4) {
			return;
		}
		callback(req);
	}
	if(req.readyState == 4) {
		return;
	}
	req.send(payload);
}

thymio.reconnectThymio = function()
{
	thymio.robot = false;
	thymio.executing = false;
	document.getElementById('robotThymio').innerHTML = 'Connecting to Thymio...';
	document.getElementById('robotThymio').style.color = 'orange';
	$('#iconDisplayRobotState').removeClass('error');
    $('#iconDisplayRobotState').removeClass('wait');
    $('#iconDisplayRobotState').removeClass('busy');
    $('#menuRunProg').parent().addClass('disabled');
    Blockly.getMainWorkspace().startButton.disable();
	
	thymio.sendRequest('http://localhost:3000/nodes', function(e)
	{
		if(e.status == 200) {
			var obj = JSON.parse(e.responseText);
			
			if(Array.isArray(obj)) {
				for(var i = 0; i < obj.length; i++) {
					if(obj[i].name == 'thymio-II') {
						console.log('Connected to Thymio!');
						thymio.robot = true;
						document.getElementById('robotThymio').innerHTML = 'Connected to Thymio';
						document.getElementById('robotThymio').style.color = 'green';
						document.getElementById('robotStatus').innerHTML = 'Ready';
						document.getElementById('robotStatus').style.color = 'green';
						$('#iconDisplayRobotState').removeClass('error');
					    $('#iconDisplayRobotState').addClass('wait');
					    $('#menuRunProg').parent().removeClass('disabled');
					    Blockly.getMainWorkspace().startButton.enable();
					}
				}
			}
		}
	});
}

thymio.disconnectThymio = function()
{
	thymio.robot = false;
	thymio.executing = false;
	
	console.log('Disconnected from Thymio!');
	document.getElementById('robotThymio').innerHTML = 'Connecting to Thymio...';
	document.getElementById('robotThymio').style.color = 'orange';
	document.getElementById('robotStatus').innerHTML = 'Thymio disconnected';
	document.getElementById('robotStatus').style.color = 'red';
	$('#iconDisplayRobotState').removeClass('error');
    $('#iconDisplayRobotState').removeClass('wait');
    $('#iconDisplayRobotState').removeClass('busy');
    $('#menuRunProg').parent().addClass('disabled');
    Blockly.getMainWorkspace().startButton.disable();
}

thymio.connect = function()
{
	if(thymio.source) {
		thymio.source.close();
		thymio.source = null;
	}

	thymio.aseba = false;
	thymio.robot = false;
	thymio.executing = false;
	document.getElementById('robotAseba').innerHTML = 'Connecting to Aseba...';
	document.getElementById('robotAseba').style.color = 'orange';
	document.getElementById('robotThymio').innerHTML = '';
	document.getElementById('robotThymio').style.color = 'red';
	$('#iconDisplayRobotState').addClass('error');
    $('#iconDisplayRobotState').removeClass('wait');
    $('#iconDisplayRobotState').removeClass('busy');
    $('#menuRunProg').parent().addClass('disabled');
    Blockly.getMainWorkspace().startButton.disable();

	thymio.source = new EventSource('http://localhost:3000/events');
	
	thymio.source.addEventListener('open', function(e)
	{
		console.log('Event source connected');
		
		thymio.aseba = true;
		document.getElementById('robotAseba').innerHTML = 'Connected to Aseba';
		document.getElementById('robotAseba').style.color = 'green';
		$('#iconDisplayRobotState').removeClass('error');
	    $('#iconDisplayRobotState').removeClass('wait');
		
		thymio.reconnectThymio();
	});
	
	thymio.source.addEventListener('message', function(e)
	{
		console.log('Event: ' + e.data);
		
		var eventData = e.data.split(" ");
		
		if(eventData[0] == 'array_access_out_of_bounds') {
			thymio.stopExecution('Array access out of bounds');
		} else if(eventData[0] == 'division_by_zero') {
			thymio.stopExecution('Division by zero');
		} else if(eventData[0] == 'event_execution_killed') {
			thymio.stopExecution('Event execution killed');
		} else if(eventData[0] == 'node_specific_error') {
			thymio.stopExecution('Node specific error');
		} else if(eventData[0] == 'disconnect') {
			thymio.disconnectThymio();
		} else if(eventData[0] == 'connect') {
			thymio.reconnectThymio();
		}
	});

	thymio.source.addEventListener('error', function(e)
	{
		if(thymio.aseba) {
			thymio.disconnect('Event stream closed');
			thymio.connect();
		}
	});
}

thymio.stopExecution = function(reason)
{
	thymio.executing = false;
	
	document.getElementById('robotStatus').innerHTML = 'Execution error: ' + reason;
	document.getElementById('robotStatus').style.color = 'orange';
	$('#iconDisplayRobotState').addClass('busy');
	
	displayMessage("POPUP_ROBOT_EXECUTION_ERROR", "POPUP", reason);
}

thymio.disconnect = function(reason)
{
	thymio.aseba = false;
	thymio.robot = false;
	thymio.executing = false;
	
	document.getElementById('robotAseba').innerHTML = 'Disconnected from Aseba';
	document.getElementById('robotAseba').style.color = 'red';
	document.getElementById('robotThymio').innerHTML = 'Disconnected from Thymio';
	document.getElementById('robotThymio').style.color = 'red';
	document.getElementById('robotStatus').innerHTML = 'Error (' + reason + ')';
	document.getElementById('robotStatus').style.color = 'red';
	$('#iconDisplayRobotState').addClass('error');
    $('#iconDisplayRobotState').removeClass('wait');
    $('#iconDisplayRobotState').removeClass('busy');
    $('#menuRunProg').parent().addClass('disabled');
    Blockly.getMainWorkspace().startButton.disable();

	if(thymio.source) {
		thymio.source.close();
		thymio.source = null;
	}
}

thymio.run = function(aesl)
{
	if(!thymio.aseba) {
		return;
	}
	
	$('#iconDisplayRobotState').removeClass('busy');
	
	var payload = 'file=' + aesl;

	thymio.sendRequest('http://localhost:3000/nodes/thymio-II', function(e)
	{
		if(e.status == 200) {
			document.getElementById('robotStatus').innerHTML = 'Executing program';
			document.getElementById('robotStatus').style.color = 'green';
			thymio.executing = true;
		} else {
			document.getElementById('robotStatus').innerHTML = 'Compilation error: ' + e.responseText;
			document.getElementById('robotStatus').style.color = 'orange';
			thymio.executing = false;
			
			displayMessage("POPUP_ROBOT_COMPILER_ERROR", "POPUP", e.responseText);
		}
	}, 'PUT', payload);
}

thymio.generateAesl = function(workspace)
{
	var rawCode = Blockly.AESL.workspaceToCode(workspace);
		
	var code = rawCode.replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&apos;');
	
	var aesl = 
		'<!DOCTYPE aesl-source>\n' +
		'<network>\n' +
		'<keywords flag="true"/>\n' +
		'<node name="thymio-II">\n' +
		code +
		'</node>\n' +
		'</network>';
	
	return aesl;
}
