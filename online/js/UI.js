const selectedCol = '#0040FF';
const deselectedCol = '#001040';

var serverTextBox,
usernameTextBox,
helpButton,
loginButton,
logoutButton,
backButton;

var selectedBox;

var maxLength = 18;

var width, height;

var ctx;

function containsPoint(mx, my) {
	return (mx >= this.x) && (mx <= (this.x + this.w))
	&& (my >= this.y) && (my <= (this.y + this.h));
}

function drawMessages(con) {
	var messages = ["Push the other players off the ice!", "Try not to fall off!"];
	var start = height/4;
	con.fillStyle = '#FFFFFF';
	con.styokeStyle = '#000000';
	con.font = '18px Arial';
	var w = con.measureText(messages[0]).width;
	
	con.fillText(messages[0], (width - w)/2, start);
	
	w = con.measureText(messages[1]).width;
	con.fillText(messages[1], (width - w)/2, start + 20);	
}

function drawTextBox(con) {
	this.selected = (selectedBox === this);
	if(this.selected) {
		con.fillStyle = selectedCol;
	} else {
		con.fillStyle = deselectedCol;
	}

	con.fillRect(this.x, this.y, this.w, this.h);
	
	if(this.value) {
		con.fillStyle = '#FFFFFF';
		con.font = '18px Arial';
		con.fillText(this.value,this.x + 3, this.y + 17);
	}	
}

function createTextBox(x, y, w, h, value) {
	var t =  {};
	t.x = x;
	t.y = y;
	t.w = w;
	t.h = h;
	t.value = "";
	
	if(value) {
		t.value = value;
	}
	
	t.drawComponent = drawTextBox;
	t.containsPoint = containsPoint;

	t.append = function(c) {
		/*if (!focused)
			return;*/
		if (c === 8) {
			if (this.value.length > 0) {
				this.value = this.value.substring(0, this.value.length - 1);
			}
		} else if (this.value.length < maxLength) {
			this.value += c;
		}
	};
	
	return t;
}

function drawButton(con) {
	if(!con) return;

	var light, dark;
	
	if(this.selected) {
		con.fillStyle = '#C0C0C0';
		light = '#FFFFFF';
		dark = '#868686';
	} else {
		con.fillStyle = '#808080';
		light = '#A6A6A6';
		dark = '#595959';
	}

	// If not highlighted: light edge = 182, dark edge = 89
	// if highlighted: light edge = 255, dark edge = 134

	con.fillRect(this.x, this.y, this.w, this.h);

	con.strokeStyle = light;
	con.beginPath();
	con.moveTo(this.x + this.w, this.y);
	con.lineTo(this.x, this.y);
	con.lineTo(this.x, this.y + this.h);
	con.stroke();
	con.strokeStyle = dark;
	
	con.beginPath();
	con.moveTo(this.x, this.y + this.h);
	con.lineTo(this.x + this.w, this.y + this.h);
	con.lineTo(this.x + this.w, this.y);
	
	
	con.stroke();	
	
	con.fillStyle = '#FFFFFF';

	con.font = '18px Arial';

	if(this.text) {

		var len = con.measureText(this.text).width;
		con.fillText(this.text, this.x + (this.w - len)/2, this.y + this.h - 6);

	}
}

function newButton(x, y, w, h, text) {
	var b = {};
	b.x = x;
	b.y = y;
	b.w = w;
	b.h = h;
	b.text = text;
	b.drawButton = drawButton;
	b.containsPoint = containsPoint;
	b.selected = false;
	return b;
}

function initUI(ct, img, width, height) {

	var startx = (width - (2*100 + 4))/2;
	var starty = height*3/4;

	ct.drawImage(img, 0, 0);

	ctx = ct;
	
	serverTextBox = createTextBox(width/2 - (170/2), height/2, 170, 20, 'Server');
	ct.fillStyle = "#FFFFFF";
	ct.font = '18px Arial';
	var t1 = 'Server:';
	ct.fillText(t1, (width - 170)/2 - (13 + ct.measureText(t1).width), height/2 + 17);
	t1 = 'Username:';
	ct.fillText(t1, (width - 170)/2 - (13 + ct.measureText(t1).width), height/2 + 17 + 20 + 12);
	
	selectedBox = usernameTextBox = createTextBox(width/2 - (170/2), height/2 + 20 + 12, 170, 20, '');

	loginButton = newButton(startx, starty, 100, 25, 'Login');
	helpButton = newButton(startx + 104, starty, 100, 25, 'Help');

	backButton = newButton((width - 100)/2, starty, 100, 25, 'Back');

	logoutButton = newButton(width - 103, 3, 100, 25, 'Logout');
	
	loginButton.drawButton(ct);
	helpButton.drawButton(ct);
	//logoutButton.drawButton(ct);
	
	serverTextBox.drawComponent(ct);
	usernameTextBox.drawComponent(ct);
	drawMessages(ct);

}

function renderClient(ct, img) {
	ct.drawImage(img, 0, 0);
	if(state === WELCOME) {
		ct.fillStyle = "#FFFFFF";
		ct.font = '18px Arial';
		var t1 = 'Server:';
		ct.fillText(t1, (width - 170)/2 - (13 + ct.measureText(t1).width), height/2 + 17);
		t1 = 'Username:';
		ct.fillText(t1, (width - 170)/2 - (13 + ct.measureText(t1).width), height/2 + 17 + 20 + 12);

		loginButton.drawButton(ct);
		helpButton.drawButton(ct);
		//logoutButton.drawButton(ct);
	
		serverTextBox.drawComponent(ct);
		usernameTextBox.drawComponent(ct);
		drawMessages(ct);
	} else if(state === HELP) {
		showHelpPage();
	}
}

function showHelpPage(ct) {
	ct.drawImage(img, 0, 0);
	var msg = ["Arrow keys - move", "PgUp/PgDn - zoom camera", "Q - logout", "2 - 2D view", "3 - 3D view", "C - chat"];

	var msgH = 20;
	var startY = (height - (msg.length * 2 *msgH))/2;
	var i = 0;
	while(i != msg.length) {
		ctx.fillText(msg[i], width/2 - ctx.measureText(msg[i]).width/2, startY + i*2*msgH);
		i++;
	}

	//helpButton.drawButton(ct);
}
