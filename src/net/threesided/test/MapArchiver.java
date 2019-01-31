package net.threesided.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;
import net.threesided.shared.FileBuffer;

public class MapArchiver {
    public static void main(String[] args) throws Exception {
        ArrayList<String> names = new ArrayList<String>();
        Scanner s = new Scanner(System.in);
        String in;
        while (!(in = s.nextLine()).equals("end")) names.add(in);

        File[] files = new File[names.size()];
        int size = 0;
        for (int k = 0; k < files.length; k++) {
            files[k] = new File(names.get(k));
            size += (int) files[k].length();
        }
        //										files	file size ints		numfiles
        FileBuffer fb = new FileBuffer(new byte[size + (files.length * 4) + 1]);
        fb.writeByte(files.length);
        for (int k = 0; k < files.length; k++) {
            fb.writeInt((int) files[k].length());
            FileInputStream fin = new FileInputStream(files[k]);
            byte[] b = new byte[1024];
            int len;
            while ((len = fin.read(b, 0, 1024)) != -1) fb.writeBytes(b, 0, len);
            fin.close();
        }

        fb.save("maps");
    }
}
