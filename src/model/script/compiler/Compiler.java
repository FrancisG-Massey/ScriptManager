package model.script.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.script.executer.CompiledScript;

/**
 *
 * @author Francis
 */
public class Compiler {
	
	private final Linker linker;
	private final Map<Integer, CompiledScript> scripts = new HashMap<>();
	private final File lookupTableFile;
	
	public Compiler (File lookupTable) {
		this.linker = new Linker();
		this.lookupTableFile = lookupTable;
		if (lookupTable.exists()) {
			readLookupTable(lookupTable);
		}
	}
	
	public void compile (File inputDirectory) {
		for (File file : inputDirectory.listFiles()) {
			if (file.isDirectory()) {
				compile(file);
			}
			compileFile(file);
		}
	}
	
	public void saveFiles (File outputFile) {
		linker.writeLookupTable(lookupTableFile);
		
	}
	
	private void compileFile (File script) {
		try (FileChannel channel = new FileInputStream(script).getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
			channel.read(buffer);
			CharBuffer cBuffer = buffer.asCharBuffer();
			SourceScanner scanner = new SourceScanner(cBuffer);
			readHeaders(scanner);
			cBuffer.flip();
			compileScripts(scanner);
		} catch (IOException | ParserException ex) {
			Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, "Failed to compile file "+script, ex);
		}
	}
	
	private void readLookupTable (File lookupTable) {
		try (FileChannel channel = new FileInputStream(lookupTable).getChannel()) {
			ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			while (buffer.hasRemaining()) {
				ScriptHeader header = ScriptHeader.decode(buffer);
				linker.registerScript(header.getName(), header);
			}
		} catch (IOException ex) {
			Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, "Error reading lookup table", ex);
		}
	}
	
	private void readHeaders (SourceScanner scanner) throws ParserException {
		ScriptParser parser = new ScriptParser(scanner, linker);
		while (scanner.getToken() != Token.EOF) {
			parser.parseHeader();
			ScriptHeader header = new ScriptHeader(1, parser.getBinding(), parser.getParamSignature(), parser.getReturnSignature());
			linker.registerScript(parser.getBinding(), header);
			parser.skipBody();
		}
	}
	
	private void compileScripts (SourceScanner scanner) throws ParserException {
		ScriptParser parser = new ScriptParser(scanner, linker);
		while (scanner.getToken() != Token.EOF) {
			parser.parseHeader();
			parser.parseBody();
			ScriptHeader header = linker.lookup(parser.getBinding());
			if (header == null) {
				throw new RuntimeException("Script not found: "+parser.getBinding());
			}
			int intLocalCount = 0, objLocalCount = 0;
			for (ScriptParser.LocalVariable type : parser.localVars.values()) {
				if (type.getDataType().intBase()) {
					intLocalCount++;
				} else {
					objLocalCount++;
				}
			}
			CompiledScript script = CompiledScript.construct(header, parser.instructions.toArray(new ScriptOpcode[0]), parser.constants, intLocalCount, objLocalCount);
			scripts.put(header.getId(), script);
		}
	}
}
