package org.vishia.fpga;

import java.io.IOException;

public abstract class TestSignalRecorder {

  protected final String moduleName;
  
  
  public TestSignalRecorder(String moduleName) {
    this.moduleName = moduleName;
  }

  protected void startLine(StringBuilder ob, String signaleName) {
    ob.setLength(0);
    ob.append("\n").append(this.moduleName).append(signaleName);
    while(ob.length() <19) { ob.append('_'); }
    ob.append(':');
  }
  
  public abstract void clean();
  
  public abstract void addSignals();
  
  public abstract void output(Appendable out) throws IOException;
}
