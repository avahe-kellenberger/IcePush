package com.glgames.test;

import com.glgames.shared.FileBuffer;

public class InterfaceMaker {
	public static void main(String[] args) throws Exception {
		FileBuffer fb = new FileBuffer(new byte[1024]);
		fb.writeShort(3); // number of interfaces
		
		fb.writeShort(0); // interface 0 id
		fb.writeShort(-1); // parent id
		fb.writeShort(315); // x
		fb.writeShort(275); // y
		fb.writeByte(0); // no subclass
		
		
		fb.writeShort(1); // interface 1 id
		fb.writeShort(0); // parent id
		fb.writeShort(0); // interface 1 X
		fb.writeShort(0); // interface 1 Y
		fb.writeByte(1); // interface 1 Type
		fb.writeByte(0); // TextBox not focused
		fb.writeString("Server: ");
		fb.writeString("strictfp.com");
		
		fb.writeShort(2); // interface 2 id
		fb.writeShort(0); // parent id
		fb.writeShort(0); // interface 2 X
		fb.writeShort(25); // interface 2 Y
		fb.writeByte(1); // interface 2 Type
		fb.writeByte(1); // TextBox is focused
		fb.writeString("Username: ");
		fb.writeString("");
		
		fb.save("interfaces");
		
		System.out.println("Interface file written");
	}
}
