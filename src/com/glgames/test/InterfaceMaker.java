package com.glgames.test;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

public class InterfaceMaker {
	public static void main(String[] args) throws Exception {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(
				"interfaces"));
		dos.writeShort(3); // number of interfaces
		
		dos.writeShort(0); // interface 0 id
		dos.writeShort(-1); // parent id
		dos.writeShort(315); // x
		dos.writeShort(275); // y
		dos.writeByte(0); // no subclass
		
		
		dos.writeShort(1); // interface 1 id
		dos.writeShort(0); // parent id
		dos.writeShort(0); // interface 1 X
		dos.writeShort(0); // interface 1 Y
		dos.writeByte(1); // interface 1 Type
		dos.writeByte(0); // TextBox not focused
		writeString(dos, "Server: ");
		writeString(dos, "strictfp.com");
		
		dos.writeShort(2); // interface 2 id
		dos.writeShort(0); // parent id
		dos.writeShort(0); // interface 2 X
		dos.writeShort(25); // interface 2 Y
		dos.writeByte(1); // interface 2 Type
		dos.writeByte(1); // TextBox is focused
		writeString(dos, "Username: ");
		writeString(dos, "");
		
		dos.flush();
		dos.close();
		
		System.out.println("Interface file written");
	}
	
	private static void writeString(DataOutputStream dos, String s)
			throws Exception {
		dos.writeShort(s.length());
		dos.write(s.getBytes());
		dos.flush();
	}
}
