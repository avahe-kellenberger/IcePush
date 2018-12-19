var pack;
var run = true;
var state = 0;
var count = 80;

var SUCCESS = 2;
var FAILURE = 1;

var NEW_PLAYER = 5;
var PLAYER_MOVED = 6;
var KEEP_ALIVE = 7;
var PLAYER_LOGGED_OUT = 11;
var PLAYER_DIED = 12;
var PROJECTILE_REQUEST = 15;
var NEW_CHAT_MESSAGE = 17;
var UPDATE = 19;
var UPDATE_TIME = 18;

var chats = [];

var players = [];

var addProjectile = false;
var projectileX = 0, projectileY = 0;

var keyArray = [];
var arrowArray = {};

var KEY_LEFT = 37;
var KEY_UP = 38;
var KEY_RIGHT = 39;
var KEY_DOWN = 40;

var oldAngle = -1;

var KEY_ENTER = 13;

var in_chat = false;

var curChat = '';

var dbox;

var runLocal = false;
if(location.protocol === 'file:') {
	runLocal = true;
}

function drawDeathsBox(g) {
        var x = 210, y = 280;
        g.drawImage(dbox, x, y);
        g.fillStyle = '#FFFFFF';
        g.font = '16px chatFont';
        y += 35;
        var k;
        for (k = 0; k < players.length; k++) {
            if (!players[k]) continue;
            if(!players[k].type === 3) continue;		// Projectile
            var plr = players[k];
            g.fillText(plr.username + " - " + plr.deaths, x + 25, y += 15);
        }
}

function checkKeys() {

	var angle = -1;
	var len = keyArray.length;
	var i = 0;

	var ver = -1;
	var lat = -1;

	if(arrowArray[KEY_LEFT]) {
		lat = 192;
	}
	if(arrowArray[KEY_RIGHT]) {
		lat = 64;
	}
	if(arrowArray[KEY_LEFT] && arrowArray[KEY_RIGHT]) {
		lat = -1;
	}

	if(arrowArray[KEY_UP]) {
		ver = 128;
	}
	if(arrowArray[KEY_DOWN]) {
		ver = 0;
	}
	
	if(arrowArray[KEY_LEFT] && arrowArray[KEY_RIGHT]) {
		ver = -1;
	}

	if(lat !== -1) angle = lat;
	if(ver !== -1) angle = ver;

	if(lat !== -1 && ver !== -1) {
		angle = (lat + ver)/2;
	}

	if(lat === 192 && ver === 0) { // Special case: down and left average together and point the wrong way
		angle = 224;
	}
	
	if(angle !== oldAngle) {
		console.log('angle = ' + angle + ' oldAngle: ' + oldAngle);
		if(angle !== -1) {
			if(loggedIn) {
				pack.beginPacket(MOVE_REQUEST);
				pack.writeByte(angle);
				pack.endPacket();
			}
		} else {
			pack.beginPacket(END_MOVE);
			pack.endPacket();
		}
	}
	oldAngle = angle;

	if(keyArray[KEY_ENTER]) {
		//in_chat = !in_chat;
		keyArray[KEY_ENTER] = false;
		pack.beginPacket(CHAT_REQUEST);
		pack.writeString(curChat);
		pack.endPacket();
		curChat = '';
		keyArray[KEY_ENTER] = false;
	}

	var i = 0;
	var end = 256;

	while(i != end) {
		if(i === 8) i = 9;
		if(keyArray[i]) {
			curChat += String.fromCharCode(i);
			keyArray[i] = false;
		}
		i++;
	}

	if(keyArray[8]) {
		keyArray[8] = false;
		
		if (curChat.length > 0) {
			curChat = curChat.substring(0, curChat.length - 1);
		}
	}
}

var sprites = [document.createElement('img'), document.createElement('img'), document.createElement('img')];
var imageBase = runLocal ? "../images/" : "/images/";
sprites[0].src = imageBase + 'tree.png';
sprites[1].src = imageBase + 'snowman.png';
sprites[2].src = imageBase + 'present.png';

dbox = document.createElement('img');
dbox.src = imageBase + 'dbox.png';

var loggedIn = false;
var response = -1;

var cc = document.getElementById('clientCanvas');
var ct; //= cc.getContext('2d');

var doRender = false;
var myId = -1;

function render() {

	/*cc = document.getElementById('clientCanvas');
	if(!cc) return;

	ct = cc.getContext('2d');
	ct.translate(0.5, 0.5);*/

	ct.font = '14px Arial';
	
	ct.drawImage(img, 0, 0);

	var l = players.length;
	var i = 0;
	ct.font = '14px Arial';
	ct.fillStyle = '#FF0000';
	// Draw usernames above players

	while(i != l) {
		if(players[i]) {
			ct.drawImage(sprites[players[i].type], players[i].x, players[i].y);
			if(!players[i].namew) {
				players[i].namew = ct.measureText(players[i].username).width;
			}
			ct.fillText(players[i].username, players[i].x + (sprites[players[i].type].width - players[i].namew)/2, players[i].y);
		}
		i++;
	}

	drawDeathsBox(ct);

	var ga = ct.globalAlpha;
	ct.globalAlpha = 0.5859375;
	ct.fillStyle = '#000000';
	ct.fillRect(50, 0, 700, 179);// 50, 50);
	ct.globalAlpha = ga;

	ct.strokeStyle = '#FFFFFF';
	ct.beginPath();
	ct.moveTo(50, 155);
	ct.lineTo(750, 155);
	ct.stroke();

	ct.font = '16px chatFont';
	ct.fillStyle = '#FFFFFF';
	//ct.fillText(in_chat ? 'in chat: yes' : 'in chat: no', 70, 172);
	ct.fillText(curChat, 70, 172);

	 for (var k = 0; k < chats.length; k++) {
            var chat = chats[k];
            ct.fillText(chat, 70, 160 - (chats.length - k) * 15);
        }

	logoutButton.drawButton(ct);
}

function checkInput() {
	if(addProjectile) {
		var img = sprites[players[myId].type];
		var dx = projectileX - (players[myId].x + (img.width/2));
		var dy = projectileY - (players[myId].y + (img.height/2));
		//console.log('w='+img.width +', h =' + img.height);
		//console.log('bomb! dx=' + dx + ', dy=' + dy);
		if(dx === 0 && dy === 0) {
			return;
		}

		var angle = Math.floor((128*Math.atan2(dx, dy))/(Math.PI));
		if(angle < 0) {
			angle += 256;
		}

		console.log('a='+angle);
		
		addProjectile = false;
	}
}

function loop() {
	var i = 0;
	var j = 0;
	pack.synch();

//	console.log('Available = ' + pack.available());

	if(pack.available() >= 2 &&!loggedIn) {
		response = pack.readByte();
		console.log("Response = " + response);
		if(response === SUCCESS) {
			var id = pack.readByte();
			console.log('id = ' + id);
			loggedIn = true;
			run = true;
			state = PLAY;
			cc = document.getElementById('clientCanvas');
			ct = cc.getContext('2d');
			//ct.translate(0.5, 0.5);
			ct = globalContext;
		}
		if(response === FAILURE) {
			console.log(pack.readString());
			run = false;
			state = WELCOME;
		}
	}

	if(!loggedIn) {
		//console.log('Incomplete login');
		if(run) setTimeout(loop, 10);
		return;
	}

	checkKeys();
	checkInput();
	handlePackets();
	if(doRender) {
		render();
	}

	doRender =! doRender;
		
	if(logOut) {
		run = false;
		loggedIn = false;
		logOut = false;

		state = WELCOME;
		renderClient(ct, img);
		
		pack.beginPacket(LOGOUT);
		pack.endPacket();
		pack.synch();
	}

	if(pack.sock.readyState === pack.sock.CLOSED) {
		//run = false;
		//loggedIn = false;
		logOut = true;
	}
	
	if(run) setTimeout(loop, 10);
}

function Player(type, username) {
	this.type = type;
	this.username = username;
}

function handlePackets() {
	var opcode, id, type, x, y, username, plr;
	var pbuf = pack;
	opcode = pbuf.openPacket();

	while(opcode != -1) {
		switch(opcode) {
			case NEW_PLAYER:
				id = pbuf.readShort();
				type = pbuf.readByte(); // snowman or tree??
				username = pbuf.readString();
				var deaths = pbuf.readShort();
				plr = new Player(type, username);
				plr.username = username;
				plr.deaths = deaths;
				plr.isDead = true;
				players[id] = plr;
				console.log('New player id = ' + id + ' name = ' + username + ' type = ' + type);
				myId = id;
				break;
			case PLAYER_MOVED:
				id = pbuf.readShort(); // player ID
				x = pbuf.readShort();
				y = pbuf.readShort();
				//console.log('Mover id = ' + id + " x=" + x + ', y=' + y);
				players[id].x = x;
				players[id].y = y;
				break;
			case PLAYER_DIED:
				id = pbuf.readShort();
				plr = players[id];
				if (!plr)
					break;
				plr.deaths = pbuf.readByte();
				if (plr.deaths != 0) // death reset, not dead
					plr.isDead = true;
				console.log('id = ' + id + ' deaths = ' + plr.deaths)
				break;
			case PLAYER_LOGGED_OUT:
				id = pbuf.readShort();
				delete players[id];
				break;
			case NEW_CHAT_MESSAGE:
				var msg = pbuf.readString();
				//console.log('New message: ' + msg);
				chats.push(msg);
				break;			
		}
		//console.log('Opcode: ' + opcode);
		pbuf.closePacket();
		opcode = pbuf.openPacket();
	}
}

var stopButton;

function login(username) {

	if(!stopButton){
		stopButton = document.createElement('button');
		stopButton.innerHTML = 'Stop';
		//document.body.appendChild(stopButton);
		stopButton.onclick = function() {
			run = false;
		};
	}

	var addr = '98.11.245.205';

	if(runLocal) {
		addr = 'localhost';
	}

	addr = 'ws://' + addr + ':2345';

	var sock = new WebSocket(addr);

	var c = 500;
	while(c != 0) {
	//	console.log(modRand.next());
		c--;
	}

	sock.onopen = function() {
		console.log('IcePush: connection established');
		var VERSION = 105;
		var len = username.length;

		//alert('Username = ' + username);

		var buf = new Uint8Array(len + 3);	// Space enough for 0 VERSION and len itself
		buf[0] = 0; // connecting client
		buf[1] = VERSION;
		buf[2] = len;

		var i = 0;
		while(i != len) {
			buf[i + 3] = username.charCodeAt(i);
			i++;
		}

		sock.send(buf);

		pack = PacketBuffer(sock);

		var i = 0;
		
		pack.synch();
		console.log('Running loop');
		run = true;
		loop();
	};
}
