package model.script;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 
 * @author Francis
 */
public class BufferUtility {
	
	public static void writeString(ByteBuffer buf, String value) {
		if (value.contains("\0")) {
			throw new IllegalArgumentException("Null characters are not allowed in null-terminated strings.");
		}
		int len = value.length();
        for (int i = 0 ; i < len ; i++) {
            buf.put((byte)value.charAt(i));
        }
		buf.put((byte) 0);
    }
	
	public static String readString(ByteBuffer buf) {
		StringBuilder bldr = new StringBuilder();
		char c = (char) buf.get();
        while (c != '\0') {
			bldr.append(c);
			c = (char) buf.get();
        }
		return bldr.toString();
    }

    public static void writeString(DataOutputStream buf, String value) throws IOException {
		if (value.contains("\0")) {
			throw new IllegalArgumentException("Null characters are not allowed in null-terminated strings.");
		}
        buf.writeBytes(value);
		buf.writeByte((byte) 0);
    }
}
