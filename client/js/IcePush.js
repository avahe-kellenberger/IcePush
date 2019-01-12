// Event types
const FAILURE = 1;
const SUCCESS = 2;
const NEW_PLAYER = 5;
const PLAYER_MOVED = 6;
const MOVE_REQUEST = 8;
const END_MOVE = 9;
const LOGOUT = 10;
const CHAT_REQUEST = 16;
const PLAYER_LOGGED_OUT = 11;
const PLAYER_DIED = 12;
const PROJECTILE_REQUEST = 15;
const NEW_CHAT_MESSAGE = 17;
const UPDATE_TIME = 18;


// Key codes
const KEY_LEFT = 37;
const KEY_UP = 38;
const KEY_RIGHT = 39;
const KEY_DOWN = 40;
const KEY_ENTER = 13;

const runLocal = location.protocol === 'file:';

const chats = [];
players = [];
const keyArray = [];
const arrowArray = {};

let pack;
let run = true;

let addProjectile = false;
let projectileX = 0, projectileY = 0;

let oldAngle = -1;
let currentChat = '';

var selectedBox;
let img;
let logOut;

const WELCOME = 0, HELP = 1, PLAY = 2;
let state = WELCOME;
let globalContext;

function isArrow(code) {
	return code === KEY_LEFT || code === KEY_UP ||
	code === KEY_RIGHT || code === KEY_DOWN;
}

function oneChar(s) {
	return s.length === 1;
}

function checkKeys() {

	let angle = -1;
	let ver = -1;
	let lat = -1;

	if (arrowArray[KEY_LEFT]) {
		lat = 192;
	}
	if (arrowArray[KEY_RIGHT]) {
		lat = 64;
	}
	if (arrowArray[KEY_LEFT] && arrowArray[KEY_RIGHT]) {
		lat = -1;
	}

	if (arrowArray[KEY_UP]) {
		ver = 128;
	}
	if (arrowArray[KEY_DOWN]) {
		ver = 0;
	}

	if (arrowArray[KEY_LEFT] && arrowArray[KEY_RIGHT]) {
		ver = -1;
	}

	if (lat !== -1) angle = lat;
	if (ver !== -1) angle = ver;

	if (lat !== -1 && ver !== -1) {
		angle = (lat + ver) / 2;
	}

	if (lat === 192 && ver === 0) { // Special case: down and left average together and point the wrong way
		angle = 224;
	}

	if (angle !== oldAngle) {
		if (angle !== -1) {
			if (loggedIn) {
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

	if (keyArray[KEY_ENTER]) {
		keyArray[KEY_ENTER] = false;
		pack.beginPacket(CHAT_REQUEST);
		pack.writeString(currentChat);
		pack.endPacket();
		currentChat = '';
		keyArray[KEY_ENTER] = false;
	}

	const end = 256;

	for (let i = 0; i < end; i++) {
		if (i === 8) i = 9;
		if (keyArray[i]) {
			currentChat += String.fromCharCode(i);
			keyArray[i] = false;
		}
	}

	if (keyArray[8]) {
		keyArray[8] = false;

		if (currentChat.length > 0) {
			currentChat = currentChat.substring(0, currentChat.length - 1);
		}
	}
}

const sprites = [document.createElement('img'), document.createElement('img'), document.createElement('img')];
const imageBase = "resources/images/";
sprites[0].src = imageBase + 'tree.png';
sprites[1].src = imageBase + 'snowman.png';
sprites[2].src = imageBase + 'present.png';

const deathsBox = document.createElement('img');
deathsBox.src = imageBase + 'dbox.png';

let response = -1;

let cc;
let ct;

let doRender = false;
let myId = -1;

function drawDeathsBox(g) {
	const x = 210;
	let y = 280;
	g.drawImage(deathsBox, x, y);
	g.fillStyle = '#FFFFFF';
	g.font = '16px chatFont';
	y += 35;
	for (let k = 0; k < players.length; k++) {
		if (!players[k] || !players[k].type === 3) {
			continue;
		}
		const player = players[k];
		g.fillText(player.username + " - " + player.deaths, x + 25, y += 15);
	}
}

function render() {
	ct.drawImage(img, 0, 0);
	ct.font = '14px Arial';
	ct.fillStyle = '#FF0000';
	// Draw usernames above players
	for (let i = 0; i < players.length; i++) {
		if (players[i]) {
			ct.drawImage(sprites[players[i].type], players[i].x, players[i].y);
			if (!players[i].namew) {
				players[i].namew = ct.measureText(players[i].username).width;
			}
			ct.fillText(players[i].username, players[i].x + (sprites[players[i].type].width - players[i].namew) / 2, players[i].y);
		}
	}

	drawDeathsBox(ct);

	const oldAlpha = ct.globalAlpha;
	ct.globalAlpha = 0.5859375;
	ct.fillStyle = '#000000';
	ct.fillRect(50, 0, 700, 179);
	ct.globalAlpha = oldAlpha;

	ct.strokeStyle = '#FFFFFF';
	ct.beginPath();
	ct.moveTo(50, 155);
	ct.lineTo(750, 155);
	ct.stroke();

	ct.font = '16px chatFont';
	ct.fillStyle = '#FFFFFF';
	ct.fillText(currentChat, 70, 172);

	for (let k = 0; k < chats.length; k++) {
		ct.fillText(chats[k], 70, 160 - (chats.length - k) * 15);
	}

	logoutButton.drawButton(ct);
}

function checkInput() {
	if (addProjectile) {
		const img = sprites[players[myId].type];
		const dx = projectileX - (players[myId].x + (img.width / 2));
		const dy = projectileY - (players[myId].y + (img.height / 2));
		if (dx === 0 && dy === 0) {
			return;
		}
		addProjectile = false;
	}
}

let loggedIn = false;

function loop() {
	pack.synch();

	if (pack.available() >= 2 && !loggedIn) {
		response = pack.readByte();
		if (response === SUCCESS) {
			const id = pack.readByte();
			loggedIn = true;
			state = PLAY;
			run = true;
			cc = document.getElementById('clientCanvas');
			ct = cc.getContext('2d');
			ct = globalContext;
		}

		if (response === FAILURE) {
			run = false;
		}
	}

	if (!loggedIn) {
		if (run) {
			setTimeout(loop, 10);
		}
		return;
	}

	checkKeys();
	checkInput();
	handlePackets();

	if (doRender) {
		render();
	}

	doRender = !doRender;

	if (logOut) {
		run = false;
		loggedIn = false;
		logOut = false;
		players = [];
		state = WELCOME;
		renderClient(ct, img);
		pack.beginPacket(LOGOUT);
		pack.endPacket();
		pack.synch();
	}

	if (pack.sock.readyState === pack.sock.CLOSED) {
		logOut = true;
	}

	if (run) {
		setTimeout(loop, 10);
	}
}

function Player(type, username) {
	this.type = type;
	this.username = username;
}

function handlePackets() {
	let opcode, playerID, type, x, y, username, plr;
	const pbuf = pack;
	opcode = pbuf.openPacket();

	while (opcode !== -1) {
		switch (opcode) {
			case NEW_PLAYER:
				playerID = pbuf.readShort();
				// Snowman or tree?
				type = pbuf.readByte();
				username = pbuf.readString();
				const deaths = pbuf.readShort();
				plr = new Player(type, username);
				plr.username = username;
				plr.deaths = deaths;
				plr.isDead = true;
				players[playerID] = plr;
				myId = playerID;
				break;
			case PLAYER_MOVED:
				playerID = pbuf.readShort();
				x = pbuf.readShort();
				y = pbuf.readShort();
				players[playerID].x = x;
				players[playerID].y = y;
				break;
			case PLAYER_DIED:
				playerID = pbuf.readShort();
				plr = players[playerID];
				if (!plr)
					break;
				plr.deaths = pbuf.readByte();
				// Death reset; not dead.
				if (plr.deaths !== 0) {
					plr.isDead = true;
				}
				break;
			case PLAYER_LOGGED_OUT:
				playerID = pbuf.readShort();
				delete players[playerID];
				break;
			case NEW_CHAT_MESSAGE:
				const msg = pbuf.readString();
				chats.push(msg);
				break;
		}
		pbuf.closePacket();
		opcode = pbuf.openPacket();
	}
}

let stopButton;

function login(username) {

	if (!stopButton) {
		stopButton = document.createElement('button');
		stopButton.innerHTML = 'Stop';
		stopButton.onclick = function () {
			run = false;
		};
	}

	const address = `ws://${runLocal ? 'localhost' : '98.11.245.205'}:2345`;
	const socket = new WebSocket(address);

	socket.onopen = function () {
		const VERSION = 106;
		pack = PacketBuffer(socket);
		pack.beginPacket(0);
		pack.writeByte(VERSION);
		pack.writeString(username);
		pack.endPacket();
		pack.synch();
		run = true;
		loop();
	};
}

function addHandlers() {

	const canvas = document.getElementById("clientCanvas");

	canvas.onmousemove = function (event) {
		const brect = canvas.getBoundingClientRect();
		const x = event.clientX - Math.floor(brect.left);
		const y = event.clientY - Math.floor(brect.top);

		let select = loginButton.containsPoint(x, y);
		if (select !== loginButton.selected && state === WELCOME) {
			loginButton.selected = select;
			loginButton.drawButton(globalContext);
		}

		select = helpButton.containsPoint(x, y);
		if (select !== helpButton.selected && state === WELCOME) {
			helpButton.selected = select;
			helpButton.drawButton(globalContext);
		}

		if (state === PLAY) {
			select = logoutButton.containsPoint(x, y);
			if (select !== logoutButton.selected) {
				logoutButton.selected = select;
				logoutButton.drawButton(globalContext);
			}
		}

		if (state === HELP) {
			select = backButton.containsPoint(x, y);
			if (select !== backButton.selected) {
				backButton.selected = select;
				backButton.drawButton(globalContext);
			}
		}
	};

	function handleKey(e) {
		if (e) {
			// Stops page from unloading when deleting characters from the text fields.
			e.preventDefault();
		}

		let keyCode = e.keyCode;

		if (isArrow(keyCode) && loggedIn) {
			arrowArray[keyCode] = true;
			return;
		}

		if (keyCode !== 8 && keyCode !== 13 && !oneChar(e.key)) {
			return;
		}

		if (keyCode !== 8 && keyCode !== 13) {
			keyCode = e.key.charCodeAt(0);
		}

		if (!loggedIn) {
			if (keyCode === 8) {
				selectedBox.append(8);
				selectedBox.drawComponent(globalContext);
			} else {
				selectedBox.append(String.fromCharCode(keyCode));
				selectedBox.drawComponent(globalContext);
			}
		} else {
			keyArray[keyCode] = true;
		}
	}

	window.onkeydown = function (e) {
		handleKey(e);
	};

	window.onkeypress = function (e) {
		handleKey(e);
	};

	window.onkeyup = function (e) {
		if (isArrow(e.keyCode)) {
			arrowArray[e.keyCode] = false;
		} else {
			keyArray[e.keyCode] = false;
		}
	};

	canvas.onclick = function (event) {
		var brect = canvas.getBoundingClientRect();
		var x = event.clientX - Math.floor(brect.left);
		var y = event.clientY - Math.floor(brect.top);

		const comps = [usernameTextBox, serverTextBox, helpButton, loginButton];
		if (usernameTextBox.containsPoint(x, y) && (!loggedIn)) {
			selectedBox = usernameTextBox;
			usernameTextBox.drawComponent(globalContext);
			serverTextBox.drawComponent(globalContext);
		}

		if (serverTextBox.containsPoint(x, y) && (!loggedIn)) {
			selectedBox = serverTextBox;
			serverTextBox.drawComponent(globalContext);
			usernameTextBox.drawComponent(globalContext);
		}

		if (helpButton.containsPoint(x, y) && state === WELCOME) {
			ct.drawImage(img, 0, 0);
			state = HELP;
			showHelpPage(ct);
			backButton.drawButton(globalContext);
			return;
		}

		if (loginButton.containsPoint(x, y) && state === WELCOME) {
			if (!loggedIn){
				login(usernameTextBox.value);
			}
			loginButton.selected = false;
		}

		if (logoutButton.containsPoint(x, y)) {
			if (loggedIn){
				logOut = true;
			}
			return;
		}

		if (backButton.containsPoint(x, y) && state === HELP) {
			state = WELCOME;
			canvas.onmousemove(event);
			renderClient(ct, img);
		}

		if (state === PLAY) {
			addProjectile = true;
			projectileX = x;
			projectileY = y;
		}

	};
}
