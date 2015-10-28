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
	document.getElementById('thymio').innerHTML = 'Connecting...';
	document.getElementById('thymio').style.color = 'orange';
	
	thymio.sendRequest('http://localhost:3000/nodes', function(e)
	{
		if(e.status == 200) {
			var obj = JSON.parse(e.responseText);
			
			if(Array.isArray(obj)) {
				for(var i = 0; i < obj.length; i++) {
					if(obj[i].name == 'thymio-II') {
						console.log('Connected to Thymio!');
						thymio.robot = true;
						document.getElementById('thymio').innerHTML = 'Connected';
						document.getElementById('thymio').style.color = 'green';
						document.getElementById('status').innerHTML = 'Ready';
						document.getElementById('status').style.color = 'green';
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
	document.getElementById('thymio').innerHTML = 'Connecting...';
	document.getElementById('thymio').style.color = 'orange';
	document.getElementById('status').innerHTML = 'Thymio disconnected';
	document.getElementById('status').style.color = 'red';
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
	document.getElementById('aseba').innerHTML = 'Connecting...';
	document.getElementById('aseba').style.color = 'orange';
	document.getElementById('thymio').innerHTML = '';
	document.getElementById('thymio').style.color = 'red';

	thymio.source = new EventSource('http://localhost:3000/events');
	
	thymio.source.addEventListener('open', function(e)
	{
		console.log('Event source connected');
		
		thymio.aseba = true;
		document.getElementById('aseba').innerHTML = 'Connected';
		document.getElementById('aseba').style.color = 'green';
		
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
	
	document.getElementById('status').innerHTML = 'Execution error: ' + reason;
	document.getElementById('status').style.color = 'orange';
}

thymio.disconnect = function(reason)
{
	thymio.aseba = false;
	thymio.robot = false;
	thymio.executing = false;
	
	document.getElementById('aseba').innerHTML = 'Disconnected';
	document.getElementById('aseba').style.color = 'red';
	document.getElementById('thymio').innerHTML = 'Disconnected';
	document.getElementById('thymio').style.color = 'red';
	document.getElementById('status').innerHTML = 'Error (' + reason + ')';
	document.getElementById('status').style.color = 'red';

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
	
	var payload = 'file=' + aesl;

	thymio.sendRequest('http://localhost:3000/nodes/thymio-II', function(e)
	{
		if(e.status == 200) {
			document.getElementById('status').innerHTML = 'Executing program';
			document.getElementById('status').style.color = 'green';
			thymio.executing = true;
		} else {
			document.getElementById('status').innerHTML = 'Compilation error: ' + e.responseText;
			document.getElementById('status').style.color = 'orange';
			thymio.executing = false;
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
