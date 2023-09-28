package org.vishia.fpga;

//tag::classhead[]
/**This interface should unify a module for FPGA.
 * The here defined operations are necessary especially for test in Java.
 * Not necessary for VHDL translation. 
 * @author Hartmut Schorrig www.vishia.org
 *
 */
public interface FpgaModule_ifc {
//end::classhead[]

  /**Version, history and license.
   * <ul>
   * <li>2012-12-04 Hartmut add reset 
   * <li>2022-02-17 Hartmut created: 
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
  public final static String sVersion = "2022-02-17"; 


//tag::body[]
  
  /**Creates an initial state as after hardware reset.
   * Usual the default ctors should be called here.
   * The operation should be called first on start of simulation.
   * Especially necessary on reusing of a given instance (without new construction)
   * for several tests.
   * Pattern: <pre>
   * void reset ( ) {
   *   this.my = new My();
   * }</pre>
   * @since 2022-12-04 Hint for older version rename {@link org.vishia.fpga.stdmodules.Reset#res(int, int)} from older name reset().
   */
  void reset ( );
  
  /**This operation should prepare all D-inputs of flipflops. It is the creation of the q_d instances.
   * Pattern: <pre>
   * void step ( int time) {
   *   this.my_d = new My(time, this.my, this, ref);
   * }</pre>
   * 
   * @param time
   */
  void step ( int time);
  
  /**This operation should update the Q-Outputs of flipflop from D
   * and can also output signals to ports.
   * Pattern: <pre>
   * void update ( ) {
   *   this.my = this.my_d;
   * }</pre>
   */
  void update ( );
  
  
}
//end::body[]
