package model.script.compiler;

import model.script.compiler.ScriptOpcode;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import model.script.BufferUtility;
import model.script.Script;
import model.script.ScriptDataType;
import model.script.compiler.ScriptHeader;

/**
 *
 * @author Francis
 */
public class CompiledScript implements Script {
	
	private static final byte VERSION = 1;
	
	public static CompiledScript construct (int id, String name, ScriptOpcode[] instructions, Map<Integer, Object> constants, 
			List<ScriptDataType> params, List<ScriptDataType>responses, int intLocalCount, int objLocalCount) {
		CompiledScript script = new CompiledScript(id);
		script.name = name;
		script.instructions = instructions;
		script.constants = constants;
		script.params = params;
		script.responses = responses;
		script.intLocalCount = intLocalCount;
		script.objLocalCount = objLocalCount;
		return script;
	}
	
	public static CompiledScript construct (ScriptHeader header, ScriptOpcode[] instructions, Map<Integer, Object> constants, 
			int intLocalCount, int objLocalCount) {
		CompiledScript script = new CompiledScript(header.getId());
		script.name = header.getName();
		script.instructions = instructions;
		script.constants = constants;
		script.params = header.getParams();
		script.responses = header.getReturns();
		script.intLocalCount = intLocalCount;
		script.objLocalCount = objLocalCount;
		return script;
	}
	
	protected ScriptOpcode[] instructions;
	protected List<ScriptDataType> params;
	protected List<ScriptDataType> responses;
	
	protected Map<Integer, Object> constants = new HashMap<>();
	
	protected int intLocalCount;
	protected int objLocalCount;
	
	private final int id;
	private String name;
	
	public CompiledScript (int id) {
		this.id = id;
	}
	
	public int getId () {
		return id;
	}
	
	public String getName () {
		return name;
	}
    
    @Override
    public int getIntLocalCount () {
        return intLocalCount;
    }
    
    @Override
    public int getObjLocalCount () {
        return objLocalCount;
    }
	
	public int getEncodedSize () {
		int size = name.length()+params.size()+responses.size()+(instructions.length*2)+10;
		for (int pos=0;pos<instructions.length;pos++) {
			if (instructions[pos] == ScriptOpcode.LOAD_STR_CONST) {
				size += ((String) constants.get(pos)).length()+1;
			} else if (instructions[pos].hasIntConst()) {
				size += 4;
			}
		}
		return size;
	}
    
    @Override
    public ScriptOpcode getInstruction (int pos) {
        return instructions[pos];
    }
    
    @Override
    public int getInstructionCount () {
        return instructions.length;
    }
    
    @Override
    public Object getConstant (int pos) {
        return constants.get(pos);
    }
    
    public List<ScriptDataType> getParams () {
        return Collections.unmodifiableList(params);
    }
	
	public void encode (ByteBuffer buffer) throws IOException {
		buffer.put(VERSION);
		BufferUtility.writeString(buffer, name);
		buffer.put((byte) params.size());
		for (ScriptDataType param : params) {
			buffer.put((byte) param.getID());
		}
		buffer.put((byte) responses.size());
		for (ScriptDataType response : responses) {
			buffer.put((byte) response.getID());
		}
		buffer.putShort((short) intLocalCount);
		buffer.putShort((short) objLocalCount);
		
		buffer.putShort((short) instructions.length);
		for (ScriptOpcode instr : instructions) {
			buffer.putShort((short) instr.getOpcode());
		}
		
		for (int pos=0;pos<instructions.length;pos++) {
			if (instructions[pos] == ScriptOpcode.LOAD_STR_CONST) {
				BufferUtility.writeString(buffer, (String) constants.get(pos));
			} else if (instructions[pos].hasIntConst()) {
				buffer.putInt((int) constants.get(pos));
			}
		}
	}
	
	public void decode (ByteBuffer buffer) throws IOException {
		int version = buffer.get();
		if (version != 1) {
			throw new RuntimeException("Unsupported version number: "+version);
		}
		name = BufferUtility.readString(buffer);
		int count = buffer.get() & 0xff;
		params = new ArrayList<>(count);
		for (int pos=0;pos<count;pos++) {
			params.add(ScriptDataType.forID(buffer.get() & 0xff));
		}
		
		count = buffer.get() & 0xff;
		responses = new ArrayList<>(count);
		for (int pos=0;pos<count;pos++) {
			responses.add(ScriptDataType.forID(buffer.get() & 0xff));
		}
		intLocalCount = buffer.getShort();
		objLocalCount = buffer.getShort();
		
		count = buffer.getShort();
		instructions = new ScriptOpcode[count];
		for (int pos=0;pos<count;pos++) {
			instructions[pos] = ScriptOpcode.forOpcode(buffer.getShort() & 0xffff);
		}
		
		for (int pos=0;pos<instructions.length;pos++) {
			if (instructions[pos] == ScriptOpcode.LOAD_STR_CONST) {
				constants.put(pos, BufferUtility.readString(buffer));
			} else if (instructions[pos].hasIntConst()) {
				constants.put(pos, buffer.getInt());
			}
		}
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + Arrays.deepHashCode(this.instructions);
		hash = 89 * hash + Objects.hashCode(this.params);
		hash = 89 * hash + Objects.hashCode(this.responses);
		hash = 89 * hash + Objects.hashCode(this.constants);
		hash = 89 * hash + this.intLocalCount;
		hash = 89 * hash + this.objLocalCount;
		hash = 89 * hash + Objects.hashCode(this.name);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CompiledScript other = (CompiledScript) obj;
		if (!Arrays.deepEquals(this.instructions, other.instructions)) {
			return false;
		}
		if (!Objects.equals(this.params, other.params)) {
			return false;
		}
		if (!Objects.equals(this.responses, other.responses)) {
			return false;
		}
		if (!Objects.equals(this.constants, other.constants)) {
			return false;
		}
		if (this.intLocalCount != other.intLocalCount) {
			return false;
		}
		if (this.objLocalCount != other.objLocalCount) {
			return false;
		}
		return Objects.equals(this.name, other.name);
	}
	
}
