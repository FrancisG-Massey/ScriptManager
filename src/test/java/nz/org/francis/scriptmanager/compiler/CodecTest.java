package nz.org.francis.scriptmanager.compiler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.org.francis.scriptmanager.ScriptDataType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Francis
 */
public class CodecTest {
	
	public CodecTest() {
		
	}
	
	@Test
	public void testEncodeSize() throws IOException {
		ScriptOpcode[] instructions = { ScriptOpcode.LOAD_INT_CONST, ScriptOpcode.LOAD_STR_CONST, ScriptOpcode.DROP_OBJ, ScriptOpcode.ADD, ScriptOpcode.ASSIGN_LOCAL_INT };
		Map<Integer, Object> constants = new HashMap<>();
		constants.put(0, 132345);
		constants.put(1, "Hello!");
		constants.put(4, 1);
		List<ScriptDataType> params = Arrays.asList(new ScriptDataType[]{ ScriptDataType.INT, ScriptDataType.STRING, ScriptDataType.BOOLEAN });
		List<ScriptDataType> responses = Arrays.asList(new ScriptDataType[]{ ScriptDataType.LONG, ScriptDataType.BOOLEAN, ScriptDataType.STRING });
		CompiledScript script = CompiledScript.construct(1, "Script1", instructions, constants, params, responses, 3, 6);
		int size = script.getEncodedSize();
		ByteBuffer buffer = ByteBuffer.allocate(1000);
		int offset = buffer.position();
		script.encode(buffer);
		assertEquals(size, buffer.position()-offset);
	}

	@Test
	public void testScriptCodec() throws IOException {
		ScriptOpcode[] instructions = { ScriptOpcode.LOAD_INT_CONST, ScriptOpcode.LOAD_STR_CONST, ScriptOpcode.DROP_OBJ, ScriptOpcode.ADD, ScriptOpcode.ASSIGN_LOCAL_INT };
		Map<Integer, Object> constants = new HashMap<>();
		constants.put(0, 132345);
		constants.put(1, "Hello!");
		constants.put(4, 1);
		List<ScriptDataType> params = Arrays.asList(new ScriptDataType[]{ ScriptDataType.INT, ScriptDataType.STRING, ScriptDataType.BOOLEAN });
		List<ScriptDataType> responses = Arrays.asList(new ScriptDataType[]{ ScriptDataType.LONG, ScriptDataType.BOOLEAN, ScriptDataType.STRING });
		CompiledScript script = CompiledScript.construct(1, "Script1", instructions, constants, params, responses, 3, 6);
		ByteBuffer buffer = ByteBuffer.allocate(script.getEncodedSize());
		script.encode(buffer);
		buffer.flip();
		CompiledScript newScript = new CompiledScript(script.getId());
		newScript.decode(buffer);
		assertEquals(script, newScript);
		 
	}
}
