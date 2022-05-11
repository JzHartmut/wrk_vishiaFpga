library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use ieee.numeric_std.all;
use ieee.std_logic_unsigned.all;

ENTITY BlinkingLed_Fpga IS
PORT (
  clk: IN BIT;
  reset_Pin : IN BIT;
  led1 : OUT BIT;
  led2 : OUT BIT  --last line of port definition
);
END BlinkingLed_Fpga;

ARCHITECTURE BEHAVIORAL OF BlinkingLed_Fpga IS

TYPE BlinkingLedCt_Q_REC IS RECORD
  ctLow : STD_LOGIC_VECTOR(15 DOWNTO 0);
  ct : STD_LOGIC_VECTOR(7 DOWNTO 0);
  led : BIT;
END RECORD BlinkingLedCt_Q_REC;

TYPE ClockDivider_Q_REC IS RECORD
  ct : STD_LOGIC_VECTOR(3 DOWNTO 0);
  ce : BIT;
END RECORD ClockDivider_Q_REC;

TYPE Reset_Q_REC IS RECORD
  resetCount : STD_LOGIC_VECTOR(3 DOWNTO 0);
  res : BIT;
END RECORD Reset_Q_REC;

SIGNAL ce_Q : ClockDivider_Q_REC;
SIGNAL ct_Q : BlinkingLedCt_Q_REC;
SIGNAL res_Q : Reset_Q_REC;

CONSTANT BlinkingLedCfg_onDuration_BlinkingLed : INTEGER := 10;
CONSTANT BlinkingLedCfg_time_BlinkingLed : BIT_VECTOR(7 DOWNTO 0) := x"64";
CONSTANT BlinkingLed_Fpga_onDuration_BlinkingLed : INTEGER := 100;
CONSTANT BlinkingLed_Fpga_time_BlinkingLed : BIT_VECTOR(7 DOWNTO 0) := x"c8";



BEGIN



ce_Q_PRC: PROCESS ( clk )
BEGIN IF(clk'event AND clK='1') THEN

  IF ce_Q.ct <  "1001" THEN
      ce_Q.ct <=  ce_Q.ct + 1;            -- ClockDivider.java, line:32
  ELSE
      ce_Q.ct <=   "0000";            -- ClockDivider.java, line:34
  END IF;            -- ClockDivider.java, line:31
  IF ce_Q.ct =  "0000" THEN ce_Q.ce  <=  '1'; ELSE ce_Q.ce  <=  '0'; END IF;            -- ClockDivider.java, line:36
END IF; END PROCESS;



ct_Q_PRC: PROCESS ( clk )
BEGIN IF(clk'event AND clK='1') THEN

  IF ce_Q.ce='1' THEN
      IF res_Q.res='1' THEN
          ct_Q.ct <= TO_STDLOGICVECTOR(BlinkingLed_Fpga_time_BlinkingLed);            -- BlinkingLedCt.java, line:51
          ct_Q.ctLow <=  x"0000";            -- BlinkingLedCt.java, line:53
      ELSE
        IF ct_Q.ctLow(15)='1' THEN
            ct_Q.ctLow <=  x"61a7";            -- BlinkingLedCt.java, line:56
            IF ct_Q.ct = x"00"  THEN
                ct_Q.ct <= TO_STDLOGICVECTOR(BlinkingLed_Fpga_time_BlinkingLed);            -- BlinkingLedCt.java, line:58
        ELSE
                ct_Q.ct <=  ct_Q.ct - 1 ;            -- BlinkingLedCt.java, line:60
            END IF;            -- BlinkingLedCt.java, line:57
        ELSE
            ct_Q.ctLow <=  ct_Q.ctLow - 1 ;            -- BlinkingLedCt.java, line:64
            END IF;            -- BlinkingLedCt.java, line:56
      END IF;            -- BlinkingLedCt.java, line:50
  ELSE
  END IF;            -- BlinkingLedCt.java, line:47
  IF ct_Q.ct < BlinkingLed_Fpga_onDuration_BlinkingLed  THEN ct_Q.led  <=  '1'; ELSE ct_Q.led  <=  '0'; END IF;            -- BlinkingLedCt.java, line:77
END IF; END PROCESS;



res_Q_PRC: PROCESS ( clk )
BEGIN IF(clk'event AND clK='1') THEN

  IF reset_Pin = '0' THEN
      res_Q.resetCount <=   "0000";            -- Reset.java, line:39
  ELSE
    IF res_Q.res='1' THEN
        res_Q.resetCount <=  res_Q.resetCount + 1;            -- Reset.java, line:42
    ELSE
    END IF;            -- Reset.java, line:42
  END IF;            -- Reset.java, line:38
  IF res_Q.resetCount <  "1110" THEN res_Q.res  <=  '1'; ELSE res_Q.res  <=  '0'; END IF;            -- Reset.java, line:47
END IF; END PROCESS;

led1  <=  '1' WHEN ct_Q.ct(2 DOWNTO 0) =  "000"  ELSE '0';            -- BlinkingLed_Fpga.java, line:105
led2 <=  ct_Q.led;            -- BlinkingLed_Fpga.java, line:106

END BEHAVIORAL;
