package net.threesided.server.net;

import static net.threesided.shared.Constants.*;

import java.lang.reflect.Method;
import java.util.Hashtable;
import net.threesided.shared.PacketBuffer;

public class PacketMapper {

    public static final int STRING = 1;
    public static final int BYTE = 2;
    public static final int SHORT = 3;
    public static final int INTEGER = 4;

    // TODO: Load this from config or directory structure or search or all of the above or anything
    // but this
    private static final String[] classes = new String[] {"net.threesided.server.Player"};

    /* c2s == client to server. s2c == server to client. We should evaluate/standardize usage of this convention at some point */

    private static final String[] c2sPacketNames =
            new String[] {"MOVE_REQUEST", "END_MOVE", "LOGOUT", "PING", "CHAT_REQUEST"};

    private static int[] opcodes = new int[] {MOVE_REQUEST, END_MOVE, LOGOUT, PING, CHAT_REQUEST};

    private static Hashtable<Integer, MethodHandle> methodTable =
            new Hashtable<Integer, MethodHandle>();

    private static boolean[] printed = new boolean[50];

    // TODO: These should be stored in the classes that provide the packet handling orsmoetinhg idk

    public static void load() throws ClassNotFoundException {
        for (String name : classes) {
            Class c = Class.forName(name);
            System.out.println("Found class " + c + " with " + c.getMethods().length + " methods.");
            Method[] methods = c.getMethods();
            for (Method method : methods) {
                for (int i = 0; i < c2sPacketNames.length; i++) {
                    if (c2sPacketNames[i].equals(method.getName())) {
                        System.out.println("Adding method " + method.getName());
                        MethodHandle mh = new MethodHandle();
                        mh.method = method;
                        mh.format = packetFormats[i];
                        methodTable.put(opcodes[i], mh);
                    }
                }
            }
        }
    }

    public static int packetFormats[][] =
            new int[][] {
                {BYTE}, // MOVE_REQUEST
                {}, // END_MOVE
                {}, // LOGOUT
                {}, // PING
                {STRING} // CHAT_REQUEST
            };

    public static void handlePackets(PacketBuffer pbuf, Object target) {
        while (handlePacket(pbuf, target)) ;
    }

    private static boolean handlePacket(
            PacketBuffer pbuf,
            Object target) { // TARGET OBJECT SHOULD BE PROVIDED FROM SOME OTHER INTERFACE
        int opcode = pbuf.openPacket();
        if (opcode == -1) return false;
        MethodHandle handle = methodTable.get(opcode);
        if (handle == null) {
            //	System.out.println("Unknown packet: " + opcode);
            pbuf.closePacket();
            return false;
        }
        // System.out.println(target.getClass().toString());
        /*///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          ALL CODE TO THE END OF THIS METHOD IS STILL VULNERABLE TO EXPLOITATION BECAUSE IT DOES NOT PROPERLY VALIDATE WELL-FORMEDNESS OF INPUTS
          THE POTENTIAL THING HERE IS THAT THE CLIENT CAN SPECIFY STRINGS THAT ARE OUT OF BOUNDS AND CAUSE THIS CODE TO THROW AN EXCEPTION
          IN THE FUTURE THE PROTOCOL WILL PROBABLY ALLOW SENDING OF PRIMITIVE ARRAYS AND STRING ARRAYS SO THAT WILL BE SUBJECT TO THE SAME SANITIZATION REQUREMENTS
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*/

        Object args[] = new Object[handle.format.length];

        // int format[] = handle.format;

        boolean b = !printed[opcode];
        if (b) printed[opcode] = true;

        if (b) {
            System.out.println(java.util.Arrays.toString(handle.format));
        }

        for (int i = 0; i < args.length; i++) {
            try {
                switch (handle.format[i]) {
                    case STRING:
                        if (b) {
                            System.out.println("Reading string");
                        }
                        args[i] = pbuf.readString();
                        break;
                    case BYTE:
                        if (b) {
                            System.out.println("Reading BYTE");
                        }
                        args[i] = pbuf.readByte();
                        break;
                    case SHORT:
                        if (b) {
                            System.out.println("Reading SHORT");
                        }
                        args[i] = pbuf.readShort();
                        break;
                    case INTEGER:
                        if (b) {
                            System.out.println("Reading INTEGER");
                        }
                        args[i] = pbuf.readInt();
                        break;
                }
            } catch (RuntimeException re) {
                System.out.println(
                        "Malformed packet or something like that wrong:"); // THIS BLOCK SHOULD NOT
                                                                           // EXIST
                System.out.println('\t' + getTypeName(handle.format[i]));
                re.printStackTrace();
                return false;
            }
        }

        try {
            handle.method.invoke(target, args);
        } catch (Exception e) {
            System.out.println("Reflection error");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static final String[] dataTypeNames =
            new String[] {"STRING", "BYTE", "SHORT", "INTEGER"};

    public static String getTypeName(int type) {
        return dataTypeNames[type - 1];
    }

    private static class MethodHandle {
        Method method;
        int[] format;
    }
}
