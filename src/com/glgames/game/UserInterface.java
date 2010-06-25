package com.glgames.game;

public class UserInterface {
	static UserInterface[] interfaces;
	
	public int x;
	public int y;
	public UIContainer parent;
	
	public UserInterface() { }
	
	public static void load() {
		CacheReader cr = new CacheReader("interfaces");
		short numInter = cr.readShort();
		interfaces = new UserInterface[numInter];
		while(numInter --> 0) {
			short id = cr.readShort();
			short x = cr.readShort();
			short y = cr.readShort();
			byte type = cr.readByte();
			if(type == 0) {
				// not a subclass
				UserInterface i = new UserInterface();
				i.x = x;
				i.y = y;
				interfaces[id] = i;
			} else if(type == 1) {
				// TextBox
				TextBox b = new TextBox();
				b.x = x;
				b.y = y;
				b.isFocused = cr.readByte() == 1;
				b.caption = cr.readString();
				b.text = cr.readString();
				interfaces[id] = b;
			} else if(type == 2) {
				// UIContainer
				UIContainer c = new UIContainer();
				c.x = x;
				c.y = y;
				c.children = new int[cr.readByte()]; // number of children
				for(int k = 0; k < c.children.length; k++) {
					c.children[k] = cr.readShort(); // child indexes (indices??)
				}
				interfaces[id] = c;
			}
		}
		
		// Assign the parent to 'parent' field of components that do indeed
		// have parent interfaces.
		for (UserInterface i : interfaces)
			if (i != null && i instanceof UIContainer) {
				UIContainer c = (UIContainer) i;
				for (int k = 0; k < c.children.length; k++)
					interfaces[c.children[k]].parent = c;
			}
	}
}
