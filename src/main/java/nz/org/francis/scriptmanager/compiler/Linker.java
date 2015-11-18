package nz.org.francis.scriptmanager.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import nz.org.francis.scriptmanager.ScriptDataType;
import nz.org.francis.scriptmanager.ApiFunctionType;
import nz.org.francis.scriptmanager.ApiFunction;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Francis
 */
public class Linker {
	
	private final Map<String, ScriptHeader> scriptLookup = new HashMap<>();
	
	public Linker () {
		
	}
	
	public void registerScript (String name, ScriptHeader header) {
		scriptLookup.put(name, header);
	}
	
	public void writeLookupTable (File lookupTable) {
		try (FileChannel channel = new FileOutputStream(lookupTable).getChannel()) {
			for (ScriptHeader header : scriptLookup.values()) {
				ByteBuffer buffer = ByteBuffer.allocate(header.encodedSize());
				header.encode(buffer);
				channel.write(buffer);
			}
		} catch (IOException ex) {
			Logger.getLogger(Linker.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public ScriptHeader lookup (String name) {
		return scriptLookup.get(name);
	}
	
	public ScriptDataType getGlobalVarType (String identifier) {
		return ScriptDataType.INT;
	}
	
	public int getGlobalVarId (String identifier) {
		return -1;
	}
	
	public ApiFunction getApiFunction (String identifier) {
		return ApiFunctionType.forName(identifier);
	}
	
}
