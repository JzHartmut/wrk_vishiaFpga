package org.vishia.fpga.testutil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**A base for a test signal recorder for horizontal time deterministic presentation of states. 
 * A proper derivation of this class should be implement in any module. 
 * The derivation should define some StringBuilder for output lines.
 * The instantiation of this class should be done in the Test environment, not in the module itself (!).
 * 
 * */
public abstract class TestSignalRecorder {

  /**Registered module name to build the line title. */
  protected final String moduleName;
  
  protected int pos;
  
  private List<StringBuilder> sbs;
  
  /**Each module with several output lines need an instance of this class.
   * @param moduleName as start for the line.
   */
  public TestSignalRecorder(String moduleName) {
    this.moduleName = moduleName;
  }

  /**Helper operation to resets one line.
   * @param ob any of the output buffer
   * @param signaleName the signal name on start of line will be combinded with the module name of the constructor. 
   */
  protected void startLine(StringBuilder ob, String signaleName) {
    ob.setLength(0);
    this.sbs.add(ob);
    ob.append(this.moduleName).append('.').append(signaleName);
    while(ob.length() <18) { ob.append('_'); }
    ob.append(':');
    this.pos = ob.length();
    
  }
  
  /**This operation should be implemented in a kind that all buffers should be reseted
   * using the {@link #startLine(StringBuilder, String)} operation. 
   */
  public void clean() {
    this.sbs = new LinkedList<StringBuilder>();
  }
  
  /**This operation can be overridden if the module should determine whether {@link #addSignals(int)}
   * from all modules should be called, it means somewhat should be added.
   * @param time
   * @return
   */
  public boolean checkAdd(int time) {
    return true;
  }
  
  
  
  /**This operation should be implemented to add all signals to all existing lines. 
   * Each line should have its own StringBuilder buffer.
   * @param time the system time may be used for output
   * @param bAdd true then other TestSignalsRecorders have added an information in this step time before.
   *   This information can be used to decide whether to add.
   *   But it depends of the order of registering in {@link TestSignalRecorderSet#addRecorder(TestSignalRecorder)}.
   *   If you want to use this function, this test generator is subordinate, it accepts the behavior of the recorders before.
   * @return 0 if no signal is added. Elsewhere the length of all the internal buffer.  
   * @throws IOException */
  public abstract int addSignals ( int time, boolean bAdd) throws IOException;
  
  public final int addSignals ( int time) throws IOException { return addSignals(time, true);}
  
  
  
  
  /**This operation should be implemented to add all signals to all existing lines. 
   * Each line should have its own StringBuilder buffer.
   * @throws IOException */
  protected void endSignals ( int pos) throws IOException {
    this.pos = pos;
    for(StringBuilder sb : this.sbs) {
      while(sb.length() < pos) { sb.append(' '); }
    }
  }
  
  protected boolean checkLen ( StringBuilder sb, int currentLength) {
    int zsb = sb.length();
    if(zsb == currentLength && sb.charAt(zsb-1) !='X') {
      sb.append('X');
    }
    return zsb < currentLength;
  }
  
  
  
  /**This operation outputs all lines.
   * Special function: If no lines are registered, an empty line is outputted.
   * This can be used to structure the output. 
   * @param out
   * @throws IOException
   */
  public void output ( Appendable out) throws IOException {
    if(this.sbs.size()==0) {
      out.append('\n');
    } else {
      for(StringBuilder sb: this.sbs) {
        out.append(sb).append('\n');
      }
    }
  }
  
  
  /**With this class a member of {@link TestSignalRecorderSet} can be built
   * which produces a separation line. 
   *
   */
  public static class Empty extends TestSignalRecorder {

    private final String lineSep;
    
    /**ctor
     * @param lineSep null or "", then an empty line will be output.
     *   contains one character, then this character is repeated till the current length of all other lines.
     *   contains a longer String: Writes this string as separation line.
     */
    public Empty(String lineSep) {
      super(null);
      this.lineSep = lineSep;
    }

    /**This operation is empty, does nothing, returns 0, no contribution for presentation.
     *
     */
    @Override public int addSignals(int time, boolean bAdd) throws IOException {
      return 0;
    }
    
    
    
    @Override public void output ( Appendable out) throws IOException {
      if(this.lineSep==null || this.lineSep.length()==0) {
      } else if(this.lineSep.length()==1) {
        super.endSignals(super.pos);
      } else {
        out.append(this.lineSep);
      }
      out.append("\n");
      
    }
   }
  
}
