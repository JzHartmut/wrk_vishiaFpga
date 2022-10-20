package org.vishia.fpga.testutil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.vishia.util.StringFunctions;
import org.vishia.util.StringFunctions_C;


/**A base for a test signal recorder for horizontal time deterministic presentation of states. 
 * A proper derivation of this class should be implement in any module. 
 * The derivation should define some StringBuilder for output lines.
 * The instantiation of this class should be done in the Test environment, not in the module itself (!).
 * 
 * */
public abstract class TestSignalRecorder {

  /**Version, history and license.
   * <ul>
   * <li>2022-09-xx better operation {@link #checkLen(StringBuilder)}  
   * <li>2022-07-10 new possible operation {@link #explainSignals(String)} proper usable but optional only
   * <li>2022-06-31 Some renaming for better semantic.
   * <li>2022-05-11 Hartmut created.
   * </ul>
   * <br><br>
   * <b>Copyright/Copyleft</b>:
   * For this source the LGPL Lesser General Public License,
   * published by the Free Software Foundation is valid.
   * It means:
   * <ol>
   * <li> You can use this source without any restriction for any desired purpose.
   * <li> You can redistribute copies of this source to everybody.
   * <li> Every user of this source, also the user of redistribute copies
   *    with or without payment, must accept this license for further using.
   * <li> But the LPGL is not appropriate for a whole software product,
   *    if this source is only a part of them. It means, the user
   *    must publish this part of source,
   *    but don't need to publish the whole source of the own product.
   * <li> You can study and modify (improve) this source
   *    for own using or for redistribution, but you have to license the
   *    modified sources likewise under this LGPL Lesser General Public License.
   *    You mustn't delete this Copyright/Copyleft inscription in this source file.
   * </ol>
   * If you are intent to use this sources without publishing its usage, you can get
   * a second license subscribing a special contract with the author. 
   * 
   * @author Hartmut Schorrig = hartmut.schorrig@vishia.de
   */
  public final static String sVersion = "2022-06-11"; 

  /**The length of the title of a line, after clean. */
  public static int lenClean = 20;
  
  protected int posStart = lenClean;
  
  /**Registered module name to build the line title. */
  protected final String moduleName;
  
  /**The normal position in the StringBuilder, set on {@link #endSignals(int)}.
   * All StringBuilder are filled till this pos in {@link #endSignals(int)}
   */
  protected int pos;
  
  private List<StringBuilder> sbs;
  
  /**Each module with several output lines need an instance of this class.
   * @param moduleName as start for the line.
   */
  public TestSignalRecorder(String moduleName) {
    this.moduleName = moduleName;
  }

  /**Helper operation to register and resets one line.
   * @param ob any of the output buffer
   * @param signaleName the signal name on start of line will be combined with the module name of the constructor. 
   */
  protected void registerLine(StringBuilder ob, String signaleName) {
    ob.setLength(0);
    this.sbs.add(ob);
    ob.append(this.moduleName).append('.').append(signaleName);
    while(ob.length() < (this.posStart-1)) { ob.append('_'); }
    ob.append(':');
    this.pos = ob.length();                                // increase posStart if one line description is longer,
    if(this.posStart < this.pos) { this.posStart = pos; }  // it is then valid for all following lines.
  }
  
  /**This operation should be overridden in a kind that all buffers should be created and reseted
   * using the {@link #registerLine(StringBuilder, String)} operation. 
   * Example/Template: <pre>
   *  @Override public void clean() {
      super.clean();
      super.startLine(this.sbCtLow, "ctLow");
      super.startLine(this.sbCt, "ct");
      super.startLine(this.sbtime, "time");
    }
   * </pre>
   * Note: This operation <code>super.clean();</code> should be called at first, it creates newly the container 
   * used in {@link #registerLine(StringBuilder, String)}.
   */
  public final void clean() {
    this.sbs = new LinkedList<StringBuilder>();
  }
  
  
  
  public abstract void registerLines();
  
  /**Optional operation can output a String which explains the signals of the module.
   * @param which String to control which should be output, specific usable.
   * @return empty string, then nothing is output additionally, or a whole line ending with line feed "\n" with adequate content.  
   */
  public String explainSignals(String which) { return ""; }
  
  /**This operation should be implemented to add the necessary signals to all existing lines
   * which are created or cleaned in the overridden {@link #clean()} and registered with {@link #registerLine(StringBuilder, String)}.
   *   Usage example/pattern:<pre>
    (at)Override public int addSignals ( int time, boolean bAdd ) throws IOException {
      BlinkingLedCt mdl = BlinkingLedCt.this;
      int zCurr = this.sbCt.length(); // current length for this time
      int zAdd = 0;                   // >0 then position of new length for this time
      if(mdl.ref.clkDiv.q.ce) {       // because the own states switches only with this ce, the signals should also recorded only then.
        if(mdl.q.ctLow == 1) {        // on this condition
          this.wrCt = 5;              // switch on, write 5 steps info
        }
        if(--this.wrCt >0) {          // if one of the 5 infos shouls be written:
          StringFunctions_C.appendHex(this.sbCtLow, mdl.q.ctLow,4).append(' ');    //append info
          StringFunctions_C.appendHex(this.sbCt, mdl.q.ct,2);                      //append info
          if(checkLen(this.sbtime, zCurr)) {      // add the time information if here is space.
            StringFunctions_C.appendIntPict(this.sbtime, time, "33'331.111.11");   // append time info
          }
          zAdd = this.sbCtLow.length();  //length of buffers for new time determined by the sbCtLow, the longest entry.
        } 
        else if(this.wrCt ==0) {      // end of the 5 steps, append .... as separation
          this.sbCtLow.append("..... ");
          zAdd = this.sbCtLow.length();  //length of buffers for new time determined by the sbCtLow, the longest entry.
        }
      }// if ce
      return zAdd;       // will be used in TestSignalRecorderSet.addSignals(zAdd) to set all lines to this length
    }//addSignals
   * </pre>
   * Here signals are only added if the ce of the other module is set. Elsewhere the operation returns 0.
   * This is sensible because signals either changes only with this ce, or they are interesting only in ce steps.
   * <br>
   * Furthermore not in any step signals are added, only after a trigger value. And then 5 times.
   * <br>
   * Last not least a time information is added as second or millisecond presentation value.
   *    
   * @param time the system time may be used for output
   * @param lenCurr the current length of the StringBuilder lines before adding for this timestamp.
   *   This parameter can be used for {@link #checkLen(StringBuilder, int)}.
   * @param bAdd true then other TestSignalsRecorders have added an information in this step time before.
   *   This information can be used to decide whether to add.
   *   But it depends on the order of registering in {@link TestSignalRecorderSet#registerRecorder(TestSignalRecorder)}.
   *   If you want to use this function, this test generator is subordinate, it accepts the behavior of the recorders before
   *   delivered in the return value of this operation. 
   *   If the return value of all called before addSignals(...) operation is 0, then this value is false
   *   on usage of {@link TestSignalRecorderSet#addSignals(int)}.
   * @return 0 if no signal is added. Elsewhere the length of the longest yet written internal buffer.  
   * @throws IOException 
   */
  public abstract int addSignals ( int time, int lenCurr, boolean bAdd) throws IOException;
  
  /**Simple for with bAdd = true, see {@link #addSignals(int, boolean)}
   */
  public final int addSignals ( int time) throws IOException { return addSignals(time, 0, true);}
  
  
  
  
  /**This operation fills all registered StringBuilder to the same length. 
   * It can be called manually, but it is automatically called for all TestSignalRecorder
   * using the {@link TestSignalRecorderSet#addSignals(int)}.
   * @throws IOException */
  protected void endSignals ( int pos) throws IOException {
    this.pos = pos;
    if(this.sbs !=null) {
    for(StringBuilder sb : this.sbs) {
      while(sb.length() < pos) { sb.append(' '); }
    } }
  }
  
  
  
  
  /**This operation can be used to decide whether the StringBuilder has place for a new longer information,
   * for example a bus value (hexa). See Example in {@link #addSignals(int, boolean)}  
   * @param sb the StringBuilder should be used for append
   * @param zTime The length of relevant other StringBuilder lines for this time step.
   *   It is the length before writing an information to the other StringBuilder in this step time, 
   *   not the position after writing as used for {@link #endSignals(int)}.
   *   It means it should be gathered first before fill any other buffer, see example on {@link #addSignals(int, boolean)}.
   *   If the <code>sb.length()</code> of this StringBuilder is lesser, 
   *   then firstly the content is padded with spaces to ensure the same position as given for zTime. 
   *   But this is also done if {@link #endSignals(int)} was called also for this buffer.
   * @return true if there is space to add an information. The sb is not longer as zTime, and the last character is a space. 
   *   false if sb is longer as the zTime position, or it does not contain at least one space for separation on the last position. 
   *   It means do not write into.
   */
  @Deprecated protected static boolean checkLen ( StringBuilder sb, int zTime) {
    int zsb = sb.length();                   //length of sb
    if(zsb >= zTime && (sb.charAt(zsb-1) !=' ' && zsb != TestSignalRecorder.lenClean)) {    // >= because should at least one space
      return false;             // do not write to this buffer, no space for the zTime position.
    }
    else {
      while(++zsb <= zTime) {   // padding with spaces till zTime
        sb.append(' ');         // append spaces till the current position of the time
      }
      return true;              // can append
    }
  }
  
  
  
  /**Checks whether there is space for a longer information.
   * It presumes that {@link #endSignals(int)} was called in the steps before.
   * It means the StringBuilder is filled maybe with spaces at least till {@link #pos}.
   * The information should be written only if there is at least one space on end. 
   * If there is no spaces, first time "..." is added to show the situation, but of course only one time on overfilled buffer.
   * @param sb 
   * @return
   */
  protected boolean checkLen(StringBuilder sb) { 
    int zsb = sb.length();                   //length of sb
    char cLast = sb.charAt(zsb-1);
    if(zsb > this.posStart 
      && ( zsb > this.pos 
         || zsb == this.pos && ((cLast) !=' ')    // >= because should at least one space
      )  ) {
      if(cLast != '.') {
        sb.append("...");  // to show there is too much content.
      }
      return false;             // do not write to this buffer, no space for the zTime position.
    }
    else {   //filled till pos with at least one space on end, see endSignals(int).
      return true;
    }
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
      out.append(explainSignals(""));
      for(StringBuilder sb: this.sbs) {
        out.append(sb).append('\n');
      }
    }
  }
  
  
  
  public StringBuilder getLine(String id) {
    if(!id.startsWith(this.moduleName)) return null;
    int zid = id.length();
    for(StringBuilder sb: this.sbs) {
      if(   StringFunctions.startsWith(sb, id)
         && (sb.charAt(zid) == '_' || sb.charAt(zid) == ':') 
        )
        return sb;
    }
    return null;   //not found
  }

  
  
  
  public static class Time extends TestSignalRecorder {

    StringBuilder sbTime = new StringBuilder(500);
    
    String format;
    
    int timeStep = -1;
    int timeStep5 = 9999999, timeStep10 = 9999999;
    
    
    int time0 = -1;
    
    int posNum;
    
    /**
     * @param format
     * @param factor
     */
    public Time ( String format, int factor) {
      super("Time");
      this.format = format;
    }

    @Override
    public void registerLines() {
      super.clean();
      super.registerLine(this.sbTime, "time");
      
    }

    @Override
    public int addSignals(int time, int lenCurr, boolean bAdd) throws IOException {
      if(this.time0 == -1) { this.time0 = time; }
      if(checkLen(this.sbTime, lenCurr)) {
        if(lenCurr > lenClean && this.timeStep == -1) {    // initialize timeStep first.
          int timediff = time - this.time0;
          int timeStep = 10;
          while(timediff > timeStep) {
            timeStep *=10;
          }
          this.timeStep = timeStep;
          this.timeStep10 = 10* timeStep;
          this.timeStep5 = 5* timeStep;
        }
        int timefrac = time % this.timeStep10;
        if(lenCurr == lenClean || timefrac ==0) {
          StringFunctions_C.appendIntPict(this.sbTime, time, format);
        }
        else {
          if((timefrac % this.timeStep5)==0) {
            this.sbTime.append('^');
          }
          else if((timefrac % this.timeStep)==0) {
            this.sbTime.append('|');
          }
        }
        
      }
      return 0;
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

    @Override public void registerLines() {
      // nothing, no lines
    }

    /**This operation is empty, does nothing, returns 0, no contribution for presentation.
     *
     */
    @Override public int addSignals(int time, int lenCurr, boolean bAdd) throws IOException {
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
