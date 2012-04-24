package net.cherrytools.language;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.server.InvalidPlugin;

public class Launcher {
    private static ArrayList a = new ArrayList();
    private static Interpreter gamemode;
    public static void loadGamemode(File file, boolean b) {
    	if(null == null) {
    		Interpreter interpreter = new Interpreter();
    		try {
			interpreter.eval(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
                if(b) {
                    gamemode = interpreter;
                }
    	}
		return;
    }
    
       public static void loadGamemode(File file) { 
           loadGamemode(file, true);
       }
           public static void loadPlugin(File file, boolean b) throws InvalidPlugin {
    	if(null == null) {
    		Interpreter interpreter = new Interpreter();
                try {
    		interpreter.checkFunctionExists("onEnable", new SourcePosition(0, 0));
                } catch(Exception e) {
                    throw new InvalidPlugin("onEnable is not defined!", file.getName());
                }
                try {
                interpreter.getVariable("onEnable", new SourcePosition(0, 0)).toBoolean(new SourcePosition(0, 0)).booleanValue();
                } catch(Exception e) {
                    throw new InvalidPlugin("onEnable is not boolean!", file.getName());
                }
                if(b) {
                    a.add(interpreter);
                }
    	}
		return;
    }
    
       public static void loadPlugin(File file) throws InvalidPlugin { 
           loadPlugin(file, true);
       }
}
