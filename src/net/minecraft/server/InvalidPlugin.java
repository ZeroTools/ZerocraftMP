package net.minecraft.server;

  /**
  * (C)2012 ZeroTools InvalidPlugin - is licensed.
  * @author Buddy251
  **/

  public class InvalidPlugin extends Exception {
      public InvalidPlugin(String reason, String pluginname) {
          super("[" + pluginname + "]: " + reason);
      }
  }
