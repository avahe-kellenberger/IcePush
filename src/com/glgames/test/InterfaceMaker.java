package com.glgames.test;

import java.awt.Color;

import com.glgames.game.IcePush;
import com.glgames.shared.FileBuffer;

public class InterfaceMaker {
	public static void main(String[] args) throws Exception {
		FileBuffer fb = new FileBuffer(new byte[1024]);
		fb.writeShort(7); // number of interfaces
		
		fb.writeShort(0); // interface 0 id
		fb.writeShort(-1); // parent id
		fb.writeShort(315); // x
		fb.writeShort(275); // y
		fb.writeByte(IcePush.WELCOME); // Visible during this state
		fb.writeByte(0); // no subclass
		
		fb.writeShort(1); // interface 1 id
		fb.writeShort(0); // parent id
		fb.writeShort(0); // interface 1 X
		fb.writeShort(0); // interface 1 Y
		fb.writeByte(IcePush.WELCOME); // Visible during this state
		fb.writeByte(1); // interface 1 Type
		fb.writeByte(0); // TextBox not focused
		fb.writeString("Server: ");
		fb.writeString("strictfp.com");
		
		fb.writeShort(2); // interface 2 id
		fb.writeShort(0); // parent id
		fb.writeShort(0); // interface 2 X
		fb.writeShort(25); // interface 2 Y
		fb.writeByte(IcePush.WELCOME); // Visible during this state
		fb.writeByte(1); // interface 2 Type
		fb.writeByte(1); // TextBox is focused
		fb.writeString("Username: ");
		fb.writeString("");
		
		fb.writeShort(3); // interface 3 id
		fb.writeShort(-1);
		fb.writeShort(290);
		fb.writeShort(330);
		fb.writeByte(IcePush.WELCOME); // Visible during this state
		fb.writeByte(3); // button type
		fb.writeShort(100);
		fb.writeShort(25);
		fb.writeByte(0);
		fb.writeString("Login");
		fb.writeColor(Color.gray);
		fb.writeColor(Color.white);
		
		fb.writeShort(4); // interface 4 id
		fb.writeShort(-1);
		fb.writeShort(400);
		fb.writeShort(330);
		fb.writeByte(IcePush.WELCOME); // Visible during this state
		fb.writeByte(3); // button type
		fb.writeShort(100);
		fb.writeShort(25);
		fb.writeByte(1); // Action Id Index Into The Actions Array[]
		fb.writeString("Help");
		fb.writeColor(Color.gray);
		fb.writeColor(Color.white);
		
		fb.writeShort(5); // interface 5 id
		fb.writeShort(-1);
		fb.writeShort(350);
		fb.writeShort(330);
		fb.writeByte(IcePush.HELP); // Visible during this state
		fb.writeByte(3); // button type
		fb.writeShort(100);
		fb.writeShort(25);
		fb.writeByte(2);
		fb.writeString("Back");
		fb.writeColor(Color.gray);
		fb.writeColor(Color.white);
		
		fb.writeShort(6); // interface 6 id
		fb.writeShort(-1);
		fb.writeShort(350);
		fb.writeShort(170);
		fb.writeByte(IcePush.WELCOME); // Visible during this state
		fb.writeByte(5); // ServerListType
		fb.writeByte(3);
		
		fb.save("interfaces");
		
		System.out.println("Interface file written");
	}
}
