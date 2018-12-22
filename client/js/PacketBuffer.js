const PING = -37;

function PacketBuffer(ws) {
	var p = {};
	p.inBuf = new Uint8Array(0);
	p.outBuf = new Uint8Array(5000);
	p.pktStart = 0;
	p.writePtr = 0;
	p.readPtr = 0;
	p.dataEnd = 0;
	p.sock = ws;
	p.tail = p.head = {};

	p.writeByte = function(b) {
		this.outBuf[this.writePtr++] = (b & 0xff);
	};

	p.writeShort = function(s) {
		this.outBuf[this.writePtr++] = (s & 0xff);
		this.outBuf[this.writePtr++] = ((s >> 8) & 0xff);
	};

	p.writeInt = function(i) {
		this.outBuf[this.writePtr++] = (i & 0xff);
		this.outBuf[this.writePtr++] = ((i >> 8) & 0xff);
		this.outBuf[this.writePtr++] = ((i >> 16) & 0xff);
		this.outBuf[this.writePtr++] = ((i >> 24) & 0xff);
	};

	p.writeString = function(s) {
		this.writeShort(s.length);
		var i = 0;
		while (i != s.length) {
			this.outBuf[this.writePtr++] = s.charCodeAt(i);
			i++;
		}
	};

	p.readByte = function() {
		return this.inBuf[this.readPtr++];
	};

	p.readShort = function() {
		var s = (0xff & (this.inBuf[this.readPtr])) + (this.inBuf[this.readPtr + 1] << 8);
		this.readPtr += 2;
		return s;
	};

	p.readInt = function() {
		var i = (0xff & (this.inBuf[readPtr])) + ((0xff & (this.inBuf[this.readPtr + 1])) << 8)
				+ ((0xff & (this.inBuf[readPtr + 2])) << 16)
				+ ((0xff & (this.inBuf[readPtr + 3])) << 24);
		this.readPtr += 4;
		return i;
	};

	p.readString = function() {
		//console.log('available = ' + this.available());
		var len = this.readShort();
		//console.log('len = ' + len);
		var chars = new Uint8Array(this.inBuf.buffer.slice(this.readPtr, this.readPtr + len));
		//console.log(chars);
		//console.log(chars.length);
		return String.fromCharCode.apply(null, chars);
	};

	p.beginPacket = function(op) {
		this.pktStart = this.writePtr;
		this.writePtr += 2;
		this.writeByte(op);
	};

	p.closePacket = function() {
		this.readPtr = this.pktEnd;
	};

	p.endPacket = function() {
		var saveWritePtr = this.writePtr;
		this.writePtr = this.pktStart;
		this.writeShort(saveWritePtr - this.pktStart);	// Write size to the reserved space: size = curent - start.
		this.writePtr = saveWritePtr;
	};

	p.openPacket = function() {
		if ((this.readPtr + 3) > this.dataEnd) {
			return -1; // Smallest valid packet size is 3
		}
		var size = this.readShort();
		if(size < 0) return -1;			// someone is trying to hack the server ..
		this.readPtr -= 2; // Reset the read ptr in case the packet is incomplete
		this.pktEnd = this.readPtr + size; // end = current + size;
		if (this.pktEnd > this.dataEnd) {
			return -1;
		}
		this.readPtr += 2; // Skip the length bytes
		var op = this.readByte();
		if(op == PING) return -1;
		return op;
	};

	p.synch = function() {
		var sendBuf = new Uint8Array(this.outBuf.buffer.slice(0, this.writePtr));
		//console.log(sendBuf);
		this.sock.send(sendBuf);
		this.writePtr = 0;

		var in1 = this.Pull();
		//console.log("Before readPtr = " + this.readPtr + " byteLength = " + this.inBuf.byteLength)
		this.inBuf = new Uint8Array(this.inBuf.buffer.slice(this.readPtr));//new Uint8Array(this.inBuf, this.readPtr, this.inBuf.byteLength - this.readPtr);
		//console.log("Afterg readPtr = " + this.readPtr + " byteLength = " + this.inBuf.byteLength)
		while(in1) {
			var tmp = new Uint8Array(in1.byteLength + this.inBuf.byteLength);
  			tmp.set(new Uint8Array(this.inBuf), 0);
  			tmp.set(new Uint8Array(in1), this.inBuf.byteLength);
  			this.inBuf = tmp;//.buffer;
  			in1 = this.Pull();
  		}
		this.dataEnd = this.inBuf.byteLength;
  		this.readPtr = 0;			
	};

	ws.packetBuffer = p;

	p.Pull = function() {
		//console.log('pull invoked');
		var tt = this.tail.prev;
		if(tt === undefined) {
			return;
		}
		this.tail = tt;
		//console.log('pull returned something');
		return tt.item;
	};

	p.Push = function(t) {
		var nh = {};
		nh.item = t;
		this.head.prev = nh;
		this.head = nh;
	};

	p.available = function() {
		return p.dataEnd - p.readPtr;
	}

	ws.onmessage = function(msg) {
		//console.log('Message received');
		var fr = new FileReader();
		fr.packetBuffer = this.packetBuffer;
		fr.onloadend = function() {
			//console.log('Message content:');
			//console.log(fr.result);
			var u8 = new Uint8Array(fr.result);
			//console.log('Adding buffer');
			//console.log(u8);
			this.packetBuffer.Push(u8);
			/*var i = fr.result.byteLength - 1;
			while(i >= 0) {
				console.log('Byte ' + i + ': ' + u8[i]);
				i--;
			}*/
		};
		fr.readAsArrayBuffer(msg.data);
	};
		
	return p;
}
