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
    const messages = ["Push the other players off the ice!", "Try not to fall off!"];
    const start = height / 4;
    con.fillStyle = '#FFFFFF';
    con.styokeStyle = '#000000';
    con.font = '18px Arial';
    let w = con.measureText(messages[0]).width;

    con.fillText(messages[0], (width - w) / 2, start);

    w = con.measureText(messages[1]).width;
    con.fillText(messages[1], (width - w) / 2, start + 20);
}

function drawTextBox(ctx) {
    this.selected = (selectedBox === this);
    ctx.fillStyle = this.selected ? selectedCol : deselectedCol;
    ctx.fillRect(this.x, this.y, this.w, this.h);

    if (this.value) {
        ctx.fillStyle = '#FFFFFF';
        ctx.font = '18px Arial';
        ctx.fillText(this.value, this.x + 3, this.y + 17);
    }
}

function createTextBox(x, y, w, h, value) {
    const txtBox = {};
    txtBox.x = x;
    txtBox.y = y;
    txtBox.w = w;
    txtBox.h = h;
    txtBox.value = value ? value : '';
    txtBox.drawComponent = drawTextBox;
    txtBox.containsPoint = containsPoint;
    txtBox.append = function (c) {
        if (c === 8 && this.value.length > 0) {
                this.value = this.value.substring(0, this.value.length - 1);
        } else if (this.value.length < maxLength) {
            this.value += c;
        }
    };
    return txtBox;
}

function drawButton(ctx) {
    if (!ctx) {
        return;
    }

    let light, dark;
    if (this.selected) {
        ctx.fillStyle = '#C0C0C0';
        light = '#FFFFFF';
        dark = '#868686';
    } else {
        ctx.fillStyle = '#808080';
        light = '#A6A6A6';
        dark = '#595959';
    }

    ctx.fillRect(this.x, this.y, this.w, this.h);

    ctx.strokeStyle = light;
    ctx.beginPath();
    ctx.moveTo(this.x + this.w, this.y);
    ctx.lineTo(this.x, this.y);
    ctx.lineTo(this.x, this.y + this.h);
    ctx.stroke();

    ctx.strokeStyle = dark;
    ctx.beginPath();
    ctx.moveTo(this.x, this.y + this.h);
    ctx.lineTo(this.x + this.w, this.y + this.h);
    ctx.lineTo(this.x + this.w, this.y);
    ctx.stroke();

    ctx.fillStyle = '#FFFFFF';
    ctx.font = '18px Arial';

    if (this.text) {
        const textLength = ctx.measureText(this.text).width;
        ctx.fillText(this.text, this.x + (this.w - textLength) / 2, this.y + this.h - 6);

    }
}

function newButton(x, y, w, h, text) {
    const btn = {};
    btn.x = x;
    btn.y = y;
    btn.w = w;
    btn.h = h;
    btn.text = text;
    btn.drawButton = drawButton;
    btn.containsPoint = containsPoint;
    btn.selected = false;
    return btn;
}

function initUI(ct, img, width, height) {
    const startX = (width - (2 * 100 + 4)) / 2;
    const startY = height * 3 / 4;

    ct.drawImage(img, 0, 0);

    ctx = ct;

    serverTextBox = createTextBox(width / 2 - (170 / 2), height / 2, 170, 20, 'Server');
    ct.fillStyle = "#FFFFFF";
    ct.font = '18px Arial';
    let t1 = 'Server:';
    ct.fillText(t1, (width - 170) / 2 - (13 + ct.measureText(t1).width), height / 2 + 17);
    t1 = 'Username:';
    ct.fillText(t1, (width - 170) / 2 - (13 + ct.measureText(t1).width), height / 2 + 17 + 20 + 12);

    selectedBox = usernameTextBox = createTextBox(width / 2 - (170 / 2), height / 2 + 20 + 12, 170, 20, '');

    loginButton = newButton(startX, startY, 100, 25, 'Login');
    helpButton = newButton(startX + 104, startY, 100, 25, 'Help');
    backButton = newButton((width - 100) / 2, startY, 100, 25, 'Back');
    logoutButton = newButton(width - 103, 3, 100, 25, 'Logout');
    loginButton.drawButton(ct);
    helpButton.drawButton(ct);

    serverTextBox.drawComponent(ct);
    usernameTextBox.drawComponent(ct);
    drawMessages(ct);
}

function renderClient(ctx, img) {
    ctx.drawImage(img, 0, 0);
    if (state === WELCOME) {
        ctx.fillStyle = "#FFFFFF";
        ctx.font = '18px Arial';
        let t1 = 'Server:';
        ctx.fillText(t1, (width - 170) / 2 - (13 + ctx.measureText(t1).width), height / 2 + 17);
        t1 = 'Username:';
        ctx.fillText(t1, (width - 170) / 2 - (13 + ctx.measureText(t1).width), height / 2 + 17 + 20 + 12);

        loginButton.drawButton(ctx);
        helpButton.drawButton(ctx);

        serverTextBox.drawComponent(ctx);
        usernameTextBox.drawComponent(ctx);
        drawMessages(ctx);
    } else if (state === HELP) {
        showHelpPage();
    }
}

function showHelpPage(ctx) {
    ctx.drawImage(img, 0, 0);
    const msg = ["Arrow keys - move", "PgUp/PgDn - zoom camera", "Q - logout", "2 - 2D view", "3 - 3D view", "C - chat"];
    const msgHeight = 20;
    const startY = (height - (msg.length * 2 * msgHeight)) / 2;
    for (let i = 0; i < msg.length; i++) {
        ctx.fillText(msg[i], width / 2 - ctx.measureText(msg[i]).width / 2, startY + i * 2 * msgHeight);
    }
}
