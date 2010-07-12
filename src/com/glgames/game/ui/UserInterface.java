package com.glgames.game.ui;

import java.awt.Color;

import com.glgames.shared.FileBuffer;

public class UserInterface {
	public static UserInterface[] interfaces;
	
	int x;
	int y;
	short parentID = -1;
	UserInterface parent;

	public UserInterface() { }
	
	public static void load() {
		FileBuffer cr = new FileBuffer("interfaces", false);
		short numInter = cr.readShort();
		interfaces = new UserInterface[numInter];
		for(int k = 0; k < numInter; k++) {
			short id = cr.readShort();
			short pID = cr.readShort();
			short x = cr.readShort();
			short y = cr.readShort();
			byte type = cr.readByte();
			
			UserInterface i = null;
			if(type == 0)
				i = new UserInterface();
			else if(type == 1)
				i = new TextBox();
			else if(type == 2)
				i = new Label();
			if(i == null)
				continue;
			
			i.parentID = pID;
			i.x = x;
			i.y = y;
			
			if(type == 1) {
				TextBox b = (TextBox) i;
				b.isFocused = cr.readByte() == 1;
				b.caption = cr.readString();
				b.text = cr.readString();
			} else if(type == 2) {
				Label l = (Label) i;
				l.caption = cr.readString();
				l.color = new Color(cr.readInt());
			}
			
			interfaces[id] = i;
		}
		
		for(UserInterface i : interfaces)
			if(i != null && i.parentID != -1)
				i.parent = interfaces[i.parentID];
	}
}
