package model.script.compiler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import model.script.ScriptDataType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import model.script.BufferUtility;

/**
 *
 * @author Francis
 */
public class ScriptHeader {	
	
	public static ScriptHeader decode (ByteBuffer buffer) {
		int version = buffer.get();
		if (version != 1) {
			throw new RuntimeException("Unsupported version number: "+version);
		}
		int id = buffer.getShort();
		String name = BufferUtility.readString(buffer);
		int count = buffer.get() & 0xff;
		List<ScriptDataType> params = new ArrayList<>(count);
		for (int pos=0;pos<count;pos++) {
			params.add(ScriptDataType.forID(buffer.get() & 0xff));
		}
		
		count = buffer.get() & 0xff;
		List<ScriptDataType> responses = new ArrayList<>(count);
		for (int pos=0;pos<count;pos++) {
			responses.add(ScriptDataType.forID(buffer.get() & 0xff));
		}
		return new ScriptHeader(id, name, params, responses);
	}
	
	private static final byte VERSION = 1;
	
	private final List<ScriptDataType> paramSignature;
	
	private final List<ScriptDataType> returnSignature;
	
	private final String name;
	
	private final int id;
	
	public ScriptHeader (int id, String name, List<ScriptDataType> paramSignature, List<ScriptDataType> returnSignature) {
		this.id = id;
		this.name = name;
		this.paramSignature = paramSignature;
		this.returnSignature = returnSignature;
	}
	
	public int encodedSize () {
		return name.length()+paramSignature.size()+returnSignature.size()+6;
	}
	
	public void encode (ByteBuffer buffer) {
		buffer.put(VERSION);
		buffer.putShort((short) id);
		BufferUtility.writeString(buffer, name);
		buffer.put((byte) paramSignature.size());
		for (ScriptDataType param : paramSignature) {
			buffer.put((byte) param.getID());
		}
		buffer.put((byte) returnSignature.size());
		for (ScriptDataType response : returnSignature) {
			buffer.put((byte) response.getID());
		}
	}
	
	public String getName () {
		return name;
	}
	
	public int getId () {
		return id;
	}
	
	public List<ScriptDataType> getParams () {
		return Collections.unmodifiableList(paramSignature);
	}
	
	public List<ScriptDataType> getReturns () {
		return Collections.unmodifiableList(returnSignature);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 83 * hash + Objects.hashCode(this.paramSignature);
		hash = 83 * hash + Objects.hashCode(this.returnSignature);
		hash = 83 * hash + Objects.hashCode(this.name);
		hash = 83 * hash + this.id;
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
		final ScriptHeader other = (ScriptHeader) obj;
		if (!Objects.equals(this.paramSignature, other.paramSignature)) {
			return false;
		}
		if (!Objects.equals(this.returnSignature, other.returnSignature)) {
			return false;
		}
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		return this.id == other.id;
	}
}
