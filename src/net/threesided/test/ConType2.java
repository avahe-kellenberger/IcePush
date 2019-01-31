/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.threesided.test;

import java.net.Socket;

/** @author Elliott */
public class ConType2 {
    public static void main(String args[]) {
        try {
            Socket s = new Socket("127.0.0.1", 2345);
            s.getOutputStream().write(2);
            while (s.isConnected()) {
                System.out.println(s.getInputStream().read());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
