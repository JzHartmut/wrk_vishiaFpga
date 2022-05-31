package org.vishia.fpga.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.vishia.java2Vhdl.J2Vhdl_FpgaData;
import org.vishia.java2Vhdl.J2Vhdl_ModuleInstance;
import org.vishia.java2Vhdl.J2Vhdl_ModuleType;
import org.vishia.java2Vhdl.VhdlConv;
import org.vishia.java2Vhdl.VhdlExprTerm;
import org.vishia.java2Vhdl.parseJava.JavaSrc;
import org.vishia.util.Debugutil;
import org.vishia.util.StringFunctions;
import org.vishia.util.TestOrg;

/**To test expression generation from Java to Vhdl
 * <br>
 * It uses {@link TestParseJava} as base.
 * @author hartmut Schorrig
 *
 */
public class Test_VhdlExpr {

  
  public static void main(String[] args) {
    Test_VhdlExpr thiz = new Test_VhdlExpr();
  //  thiz.log = System.out;                                 // set it for debug
    try {
      String[] testArgs = {"---TESTverbose:5"};
      TestOrg test = new TestOrg("Test_VHDLExpr, from Java via RPN", 2, testArgs);
      thiz.testExpression(test);
      test.finish();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  private Appendable log = null;

  
  TestParseJava testParseJava = new TestParseJava();

  
  VhdlConv vhdlConv = VhdlConv.d; //new VhdlConv(null, fdata);
  
  J2Vhdl_FpgaData fdata = VhdlConv.d.fdata; //new J2Vhdl_FpgaData();
  
  String[] vhdlExpr = new String[50];
  
  
  JavaSrc.Expression[] exprRpn = new JavaSrc.Expression[50];
  
  String[] vhdlExprCmp = {
        "test_Prc.q3 = test_Prc.q4"
      , "test_Prc.q1='1' OR  (test_Prc.q2 = test_Prc.q3 AND test_Prc.q4='1')"
      , "NOT test_Prc.q1='1' OR  (test_Prc.q2 = test_Prc.q3 AND test_Prc.q4='0')"
      , "test_Prc.stdv1(3)='1'"
      , "NOT test_Prc.q1='1' OR  (test_Prc.q2 = test_Prc.q3 AND  test_Prc.bitv1(2)='0')"   //4
      , "test_Prc.bitv2(2)='0'"
      , "test_Prc.q1='1' AND  (test_Prc.q2 AND test_Prc.q3 )"  //This is faulty, todo, )='1' missing in BIT AND
      , "test_Prc.bitv1 = test_Prc.bitv2  AND  (test_Prc.q1 AND test_Prc.q2 )  " //todo twice, also adapt length of bit vectors
      , "test_Prc.bitv2(1) <=  '1';"
      , "test_Prc.bitv1(5 DOWNTO 0) <=  test_Prc.bitv2; "
      , "test_Prc.bitv1(5 DOWNTO 0) <=  test_Prc.bitv2; "
      , "test_Prc.bitv1(5 DOWNTO 0) <=  test_Prc.bitv2; "
      , "test_Prc.bitv1(5 DOWNTO 0) <=  test_Prc.bitv2; "
      , "test_Prc.bitv1(5 DOWNTO 0) <=  test_Prc.bitv2; "
      , "test_Prc.bitv1(5 DOWNTO 0) <=  test_Prc.bitv2; "
      , "test_Prc.bitv1(5 DOWNTO 0) <=  test_Prc.bitv2; "
      , "test_Prc.q6 <=  test_Prc.q1 AND test_Prc.q2;"
      , "test_Prc.q7 <=  test_Prc.q1 AND test_Prc.q2;"
      , "test_Prc.q7 <=  test_Prc.q1 XOR test_Prc.q2;"
      , "test_Prc.q8 <=  test_Prc.q1 XNOR test_Prc.q2;"
      , "IF test_Prc.q1='1' THEN test_Prc.q6  <= test_Prc.q2;\n" + 
        "  ELSE test_Prc.q6  <= test_Prc.q3; END IF;"
      , "IF test_Prc.q2='1' THEN t1  := test_Prc.q3;\n" +                                     //20 
        "ELSE t1  := test_Prc.q4; END IF;  \n" + 
        "test_Prc.q9 <=  (test_Prc.q1 AND t1 ) OR test_Prc.q5;  "
      , "  IF (test_Prc.q1 AND test_Prc.q2)='1' THEN test_Prc.q7  <= test_Prc.q4;\n" + 
          "  ELSE IF (test_Prc.q2 AND  NOT test_Prc.q5)='1' THEN test_Prc.q7  <= test_Prc.q3 AND test_Prc.q4;\n" + 
          "  ELSE test_Prc.q7  <= test_Prc.q5; END IF; END IF;"
      , "IF test_Prc.q1='1' THEN \n" + 
          "  IF test_Prc.q3='1' THEN test_Prc.q7  <= test_Prc.q4;\n" + 
          "  ELSE test_Prc.q7  <= test_Prc.q5; END IF;\n" + 
          "  ELSE   IF test_Prc.q2='1' THEN test_Prc.q7  <= test_Prc.q3 AND test_Prc.q4;\n" + 
          "  ELSE test_Prc.q7  <= test_Prc.q5; END IF; END IF;"
      , "  IF test_Prc.q1='1' THEN test_Prc.bitv1  <= test_Prc.bitv2;\n" + 
          "  ELSE   IF (test_Prc.q2 AND  (test_Prc.q3 OR test_Prc.q4)  )='1' THEN test_Prc.bitv1  <= test_Prc.bitv2;\n" + 
          "  ELSE test_Prc.bitv1  <=  \"1100001110101010\"; END IF; END IF;\n" + 
          ""
      , "  IF (test_Prc.q1 AND test_Prc.q2 )='1' THEN test_Prc.bitv1  <=  \"011110\";\n" + 
          "  ELSE test_Prc.bitv1  <= test_Prc.bitv2(5 DOWNTO 0); END IF;\n" + 
          ""
      , "test_Prc.bitv1(2) <=test_Prc.q1;  "                                                    //15
      , "test_Prc.q9 <=  test_Prc.q1 AND test_Prc.stdv1(2);  "
      , "test_Prc.bitv2(6 DOWNTO 1) <=test_Prc.bitv1(5 DOWNTO 0);  "
      , "IF  test_Prc.bitv1 = \"011000\" THEN  test_Prc.q6  <=  '1'; ELSE  test_Prc.q6  <=  '0'; END IF;  "
      , "IF  test_Prc.bitv1(4 DOWNTO 2) = \"110\" THEN  test_Prc.q6  <=  '1'; ELSE  test_Prc.q6  <=  '0'; END IF;  "
      , "IF test_Prc.bitv1(4 DOWNTO 2) =  \"110\" AND test_Prc.q2='1' THEN test_Prc.q9  <=  '1'; ELSE test_Prc.q9  <=  '0'; END IF;  "
      , "test_Prc.r <=   test_Prc.b?+  test_Prc.c; \n"
          + "  test_Prc.s <=   test_Prc.a?+ test_Prc.r  ;  "  //9
      , "IF  test_Prc.stdv1( 1)='1' THEN  test_Prc.q8  <=  '1'; ELSE  test_Prc.q8  <=  '0'; END IF;  "
      , "IF  test_Prc.stdv1( 1)='0' THEN  test_Prc.q9  <=  '1'; ELSE  test_Prc.q9  <=  '0'; END IF;  "
      , "test_Prc.q7 <=  test_Prc.bitv1(1) AND test_Prc.bitv2(3);  "
      , "test_Prc.stdv1 <= TO_STDLOGICVECTOR(test_Prc.bitv1);  "
      , "test_Prc.bitv1 <= TO_BITVECTOR(test_Prc.stdv1);  "
      , "test_Prc.q8 <=  '0';  "
      , "test_Prc.bitv1 <=   \"111000\";  "
      , "test_Prc.stdv1 <=   \"001100\";  "
      , "test_Prc.stdv1 <=   test_Prc.stdv1 +  1;  "
      , "test_Prc.bitv1 <=   test_Prc.q1 & test_Prc.bitv1(5 DOWNTO 1);  " //18
      , ""
      , ""                                                 // 22 dont generate somewhat, time_1 = time
          
 //    , "test_Prc.x <= test_Prc.a +   test_Prc.b *  test_Prc.c  "
//    , "test_Prc.y <= ( test_Prc.a +  test_Prc.b)  *  test_Prc.c" 
//    , "test_Prc.z <= ( test_Prc.a +  test_Prc.b)  *   ( test_Prc.c +  test_Prc(.d)"   
//    , "test_Prc.m <= test_Prc.a +   ( test_Prc.b +  test_Prc.c)"
//    , "test_Prc.q6 <= test_Prc.q1 OR   ( test_Prc.q2 AND  test_Prc.q4)"   
//    , "test_Prc.q7 <= test_Prc.c <  test_Prc.d OR   ( test_Prc.b >  test_Prc.a AND   test_Prc.c >  test_Prc.b )"   
//    , "test_Prc.u <= ( test_Prc.a +  test_Prc.b)  *  test_Prc.c -  test_Prc.d"
//    , "test_Prc.v <= test_Prc.a *   ( test_Prc.d +  test_Prc.g)   /  test_Prc.e +   ( test_Prc.a +  test_Prc.b)"
//    , "test_Prc.w <= test_Prc.a +   test_Prc.b *   ( ( test_Prc.c +  test_Prc.f)  /   ( test_Prc.a +  test_Prc.b)  )    -  test_Prc.e +   test_Prc.d *  test_Prc.g /  test_Prc.e"
//    , "IF ( test_Prc.q1 OR ( test_Prc.q2 AND  test_Prc.q4)    = '1' ) THEN  test_Prc.p <=  test_Prc.a +  test_Prc.b; ELSE  test_Prc.p <=  test_Prc.e +   test_Prc.c *  test_Prc.d ; END IF"
    , null
    , null
  };
  
  static String[] sExprRpnCmp = 
 { "@ q3; == q4;" 
 , "@ q8; @ q1; == q2; = @;" 
 , "@ bitv2; = Fpga.TODO operaton etc.;" 
 , "@ q6; @ bitv1; == TODO operaton etc.; = @;" 
 , "@ s; @ a; @ r; @ b; + c; = @; + @; = @;" 
 , "@ stdv1; = bitv1;" 
 , "@ bitv1; = stdv1;" 
 };

    
    
    
    
    


    
  
  
  
  //private final JavaSrc.Expression[] expr = new JavaSrc.Expression[20];
  

  
  /**Tests some expressions which are given in the Java src file.
   * @param testParent
   * @throws IllegalCharsetNameException
   * @throws UnsupportedCharsetException
   * @throws FileNotFoundException
   * @throws IOException
   */
  void testExpression(TestOrg testParent) throws IllegalCharsetNameException, UnsupportedCharsetException, FileNotFoundException, IOException {
    TestOrg test = new TestOrg("test Expressions in ParseExample", 7, testParent);
    J2Vhdl_ModuleType mdlType = new J2Vhdl_ModuleType("Test", null, null, false);
    J2Vhdl_ModuleInstance mdl = new J2Vhdl_ModuleInstance("test", mdlType, false);
    this.vhdlConv.bAppendLineColumn = false;
    
    try {
      String pathJavasrc = "d:/vishia/Fpga/src_tree/src/test/java/testJava_VhdlConv/org/vishia/fpga/test/ExmplVHDL.java";
      File fileIn = new File(pathJavasrc);
      JavaSrc res = this.testParseJava.parseJava(fileIn);
      int ixTest = 0;
      //VhdlExprTerm exprDst = new VhdlExprTerm(vhdlConv);
      StringBuilder sb = new StringBuilder(100);
      for(JavaSrc.ClassDefinition theClass: res.get_classDefinition()) {
        String nameTheClass = theClass.get_classident();
        JavaSrc.ClassContent theClassC = theClass.get_classContent();
        for(JavaSrc.ClassDefinition iClass : theClassC.get_classDefinition()) { // get inner class of public module class  
          this.vhdlConv.setInnerClass("prc", "test");         // should know which class is currently in focus to build names from gloable view     
          this.vhdlConv.mapVariables("test", nameTheClass, iClass);  // records from all inner classes, same name as type
          JavaSrc.ClassContent iClassC = iClass.get_classContent();
          for(JavaSrc.MethodDefinition rOper: iClassC.get_methodDefinition()) {
            StringBuilder sbLocals = new StringBuilder();
            this.vhdlConv.createProcessVar(sbLocals, rOper.get_methodbody());     // gather and output definition of local process variables
            
            for(JavaSrc.Statement rStmnt: rOper.get_methodbody().get_statement()) {
              JavaSrc.If_statement ifStmnt = rStmnt.get_if_statement();
              final boolean bNeedBool;
              JavaSrc.Expression rExpr;
              if(ifStmnt !=null) {                         // expression as condition in if:
                bNeedBool = true;                          // should be a boolean expression type.
                rExpr = ifStmnt.get_Expression();          // statement is if(expr)
              }
              else {
                bNeedBool = false;
                rExpr = rStmnt.get_Expression();           // statement is an expression
              }
              if(rExpr !=null) {
                if(ixTest==41)
                  Debugutil.stop();
                rExpr.prep(this.log);
                JavaSrc.Expression exprRpn = rExpr;
                if(exprRpn !=null) {
                  rStmnt.set_AssignExpression(exprRpn);    // replace the expression in the statement with the RPN form
                  this.exprRpn[ixTest] = exprRpn;
                  //System.out.println(exprRpn.toString());
                  //
                  if(ixTest==7)
                    Debugutil.stop();
                  this.vhdlConv.genExpression(sb, exprRpn, bNeedBool, true, mdl, "prc",  "\n  ", null);
                  this.vhdlExpr[ixTest] = sb.toString();
                  //System.out.println(exprDst.b.toString());
                  int[] pos2Err = new int[1];
                  int posErr = StringFunctions.compareWhSpacePos(this.vhdlExpr[ixTest], 0, -1, this.vhdlExprCmp[ixTest], 0, -1, pos2Err);
                  if(posErr ==0 || ( pos2Err[0] == this.vhdlExprCmp[ixTest].length())) {
                    test.expect(true, 6, "" + ixTest + ".: " + this.vhdlExpr[ixTest]);
                  } 
                  else {
                    int posErr2 = pos2Err[0];
                    if(posErr2<0) { posErr2 = - posErr2; }
                    test.expect(false, 6,  ixTest + " expected:\n" + this.vhdlExprCmp[ixTest].substring(0, posErr2-1) + ">>>>>" + this.vhdlExprCmp[ixTest].substring(posErr2-1));
                    System.out.println("\n----Result------------------\n" +  this.vhdlExpr[ixTest] + "\n-----------------\n");  //the result maybe correct to copy
                    //System.out.println("       " + this.vhdlExprCmp[ixTest]);
                  }
                  sb.setLength(0);
                  
                  
                  //
                  if(++ixTest >= this.exprRpn.length) { ixTest = this.exprRpn.length-1; } //limit it.
                }
                //JavaSrcPrep.ExpressionPrep rpExpr = new JavaSrcPrep.ExpressionPrep(rExpr, null);
                Debugutil.stop();
              }
            }
          }
        }
      }
    }
    catch(Exception exc) {
      test.exception(exc);
    }
    test.finish();
    Debugutil.stop();
  }

  
  
}
