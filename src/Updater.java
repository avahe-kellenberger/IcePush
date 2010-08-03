import java.io.*;
import java.net.Socket;

public class Updater {

	private static final int UNCHANGED = 1;
	private static final int UPDATED = 2;
	private static final int DELETED = 3;
	private static final int ADDED = 4;

	private static Entry serverIndex[];
	private static Entry localIndex[];

	public static void main(String args[]) {
		File file = new File("index");
		if(file.exists())
			try {
				FileInputStream fin = new FileInputStream(file);
				localIndex = loadIndex(fin);
				fin.close();
			} catch (IOException ioe) {
				System.out.println("Error loading local index!");
				ioe.printStackTrace();
				return;
			}

		Socket sock;
		InputStream in;
		OutputStream out;

		try {
			sock = new Socket("icepush.strictfp.com", 2345);
			in = sock.getInputStream();
			out = sock.getOutputStream();
			out.write(3);
			out.flush();
			sock.setSoTimeout(5000);
			System.out.println("Connected to server, loading server index");
			serverIndex = loadIndex(in);
		} catch (IOException ioe) {
			System.out.println("Error loading server index!");
			ioe.printStackTrace();
			return;
		}

		if(localIndex != null) updateIndex();
		else System.out.println("Local index is null, skipping update");

		for(Entry s : serverIndex) {
			if(s.status == 0 || s.status == UPDATED) {			// status == 0 means this file was not found in the local index, so it's new
				System.out.println("File " + s.name + " has been " + (s.status == 0 ? "added." : "updated."));
				try {
					System.out.println("Downloading file: " + s.name);
					loadFile(s, in, out);
				} catch(IOException ioe) {
					System.out.println("Error loading file: " + s.name);
					ioe.printStackTrace();
				}
			}
		}

		try {
			sock.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}


		if(localIndex != null) 
			for(Entry e : localIndex) {
				if(e.status == DELETED) {
					System.out.println("Deleting entry: " + e.name);
					new File(e.name).delete();
				}
			}

		try {
			FileOutputStream fout = new FileOutputStream(file);
			storeIndex(serverIndex, fout);
			fout.close();
			System.out.println("Saved updated index");
		} catch (IOException ioe) {
			System.out.println("Error saving local index!");
			ioe.printStackTrace();
		}
	}

	private static void updateIndex() {
		for(Entry e : localIndex) {
			for(Entry s : serverIndex) {
				if(e.name == s.name) {					// CAN USE == HERE BECAUSE OF String.intern() -- SEE BELOW
					if(e.modified == s.modified && e.crc == s.crc) {
						e.status = s.status = UNCHANGED;	// Change of status in server index is used locally to sieve for added files
					} else {
						e.status = s.status = UPDATED;
					}
				}
			}

			if(e.status == 0) e.status = DELETED;
		}
	}

	private static void loadFile(Entry e, InputStream in, OutputStream out) throws IOException {
		out.write(e.fileId);
		int len = 0;
		int read = 0;
		byte buf[] = new byte[4];

		in.read(buf);

		len = ((buf[0] & 0xff) << 24) + ((buf[1] & 0xff) << 16) + ((buf[2] & 0xff) << 8) + (buf[3] & 0xff);
		
		System.out.println("Length of " + e.name + " = " + len);
		buf = new byte[len];

		for(int i = 0; read < len; i++) {
			int count = in.read(buf, read, len - read);
			if(count < 0) throw new IOException("End of stream!!!");
			if(i > 400) throw new IOException("File download is taking too fucking long!!!!!!!");
			read += count;
		}
		System.out.println("Platform dependanting name: " + e.name);
		String namePD = e.name.replace('/', File.separatorChar);
		int last = namePD.lastIndexOf(File.separatorChar);
		if(last != -1) {
			new File(namePD.substring(0, last)).mkdirs();
		}

		FileOutputStream fos = new FileOutputStream(namePD);
		fos.write(buf);
		fos.close();
	}

	private static Entry[] loadIndex(InputStream ins) throws IOException {

		DataInputStream in = new DataInputStream(ins);
		Entry index[] = new Entry[in.readInt()];

		for(int i = 0; i < index.length; i++) {
			Entry e = new Entry();
			e.name = in.readUTF().intern();
			e.modified = in.readLong();
			e.crc = in.readInt();
			e.fileId = i;
			index[i] = e;
			System.out.println("Loaded entry: " + e.name);
		}

		return index;

	}

	private static void storeIndex(Entry[] index, OutputStream outs) throws IOException {

		DataOutputStream out = new DataOutputStream(outs);

		out.writeInt(index.length);
		for(Entry e : index) {
			out.writeUTF(e.name);
			out.writeLong(e.modified);
			out.writeInt(e.crc);
		}

	}

	private static class Entry {
		String name;	// Name of this file
		long modified;	// Time of last modification
		int crc;		// CRC computed by server
		int fileId;
		int status;		// Whether or not this file was updated (transient)
	}
}