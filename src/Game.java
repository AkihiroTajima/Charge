import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;

public class Game {
    private Player me;
    private Player op;
    private gameInfo gameinfo;
    statData sd;

    Game() {
        this.me = new Player("mystr.csv");
        this.op = new Player("opstr.csv");
        this.gameinfo = new gameInfo();
        this.sd = new statData();
    }

    private void setGame() {
        me.set();
        op.set();
        gameinfo.clear();
    }

    void printGame(){
        setGame();
        while (!gameinfo.isFinish()) {
            gameinfo.update(this.me,this.op);

            gameinfo.myACT  = fromOrdinal(me.Hand(gameinfo.toString()));
            gameinfo.opsACT = fromOrdinal(op.Hand(gameinfo.toString()));

            System.out.println(gameinfo.myACT.toString() + "," + gameinfo.opsACT.toString() + "," + this.gameinfo.toString());
        }
        sd.calc(gameinfo.out);
        System.out.println(gameinfo.out.toString());
        System.out.println(gameinfo.reason);
        System.out.println("------------------------------");
    }

    void cfr(boolean learnMe, boolean learnOp) {
        setGame();
        while (!gameinfo.isFinish()) {
            gameinfo.update(this.me,this.op);


            gameinfo.myACT  = fromOrdinal(me.Hand(gameinfo.toString()));
            gameinfo.opsACT = fromOrdinal(op.Hand(gameinfo.toString()));

            if (learnMe) updateStrategy();
            if (learnOp) updateOPStrategy();

            //System.out.println(gameinfo.myACT.toString() + "," + gameinfo.opsACT.toString() + "," + this.gameinfo.toString());
        }
        //System.out.println(gameinfo.out.toString());
        //System.out.println("------------------------------");
        sd.calc(gameinfo.out);
    }

    void opFixed(boolean learnMe, double[] d) {
        setGame();
        while (!gameinfo.isFinish()) {
            gameinfo.update(this.me,this.op);

            gameinfo.myACT  = fromOrdinal(me.Hand(gameinfo.toString()));
            gameinfo.opsACT = fromOrdinal(op.sendStrategyHand(d));

            if (learnMe) updateStrategy();
        }
        sd.calc(gameinfo.out);
    }

    double calcStrategy() {
        setGame();
        double res = 0;
        double[][] before = new double[100][3];
        double[][] after = new double[100][3];
        for (int a = 0; a < 100; a++) {
            for (int b = 0; b < 3; b++) {
                before[a][b] = 0;
                after[a][b] = 0;
            }
        }

        Map<String, List<Integer>> regMap = me.getStrate().getRegretMap();
        int ib = 0;
        for (String s : regMap.keySet()){
            double sum = 0;
            for (int j=0; j<3; j++) sum += regMap.get(s).get(j);
            for (int j=0; j<3; j++) before[ib][j] = (double) regMap.get(s).get(j) / sum;
            ib++;
        }

        while (!gameinfo.isFinish()) {
            gameinfo.update(this.me,this.op);

            gameinfo.myACT  = fromOrdinal(me.Hand(gameinfo.toString()));
            gameinfo.opsACT = fromOrdinal(op.Hand(gameinfo.toString()));

            updateStrategy();
            updateOPStrategy();
        }

        Map<String, List<Integer>> regMapAfter = me.getStrate().getRegretMap();
        int ia = 0;
        for (String s : regMap.keySet()){
            double sum = 0;
            for (int j=0; j<3; j++) sum += regMapAfter.get(s).get(j);
            for (int j=0; j<3; j++) after[ia][j] = (double) regMapAfter.get(s).get(j) / sum;
            ia++;
        }

        for (int a = 0; a < 100; a++) {
            for (int b = 0; b < 3; b++) {
                res += abs(before[a][b] - after[a][b]);
            }
        }

        return res;
    }

    void comp() {
        setGame();
        while (!gameinfo.isFinish()) {
            gameinfo.update(this.me,this.op);

            gameinfo.myACT  = fromOrdinal(me.Hand(gameinfo.toString()));
            gameinfo.opsACT = fromOrdinal(op.Hand(gameinfo.toString()));

            updateStrategy();
            //updateOPStrategy();
        }
        this.sd.calc(gameinfo.out);
        me.compStrategy();
    }
    void comp(boolean learnMe, boolean learnOp) {
        setGame();
        while (!gameinfo.isFinish()) {
            gameinfo.update(this.me,this.op);

            gameinfo.myACT  = fromOrdinal(me.Hand(gameinfo.toString()));
            gameinfo.opsACT = fromOrdinal(op.Hand(gameinfo.toString()));

            if (learnMe) updateStrategy();
            if (learnOp) updateOPStrategy();
        }
        this.sd.calc(gameinfo.out);
        me.compStrategy();
        op.compStrategy();
    }

    void save() {
        me.save();
        op.save();
    }

    private int getRegret(OUTCOME o){
        if (o == OUTCOME.WIN) return 2;
        else if (o == OUTCOME.LOSE) return 0;
        else return 1;
    }

    private void  updateStrategy() {
        OUTCOME outA = gameinfo.simulate(ACT.ATTACK);
        OUTCOME outB = gameinfo.simulate(ACT.BARRIER);
        OUTCOME outC = gameinfo.simulate(ACT.CHARGE);

        int util = getRegret(gameinfo.out);
        int utila = getRegret(outA);
        int utilb = getRegret(outB);
        int utilc = getRegret(outC);

        int min = Math.min(Math.min(utila-util,utilb-util),utilc-util);

//        { // デバッグ
//            System.out.println("cfr =============");
//            System.out.println("a" + (utila-util+abs(min)));
//            System.out.println("b" + (utilb-util+abs(min)));
//            System.out.println("c" + (utilc-util+abs(min)));
//        }

        this.me.update(this.gameinfo.toString(),utila-util+abs(min), 0);
        this.me.update(this.gameinfo.toString(),utilb-util+abs(min), 1);
        this.me.update(this.gameinfo.toString(),utilc-util+abs(min), 2);
    }

    private void  updateOPStrategy() {
        OUTCOME outA = gameinfo.opSimulate(ACT.ATTACK);
        OUTCOME outB = gameinfo.opSimulate(ACT.BARRIER);
        OUTCOME outC = gameinfo.opSimulate(ACT.CHARGE);

        int util = getRegret(gameinfo.out);
        int utila = getRegret(outA);
        int utilb = getRegret(outB);
        int utilc = getRegret(outC);

        int min = Math.min(Math.min(utila-util,utilb-util),utilc-util);

//        { // デバッグ
//            System.out.println("cfr =============");
//            System.out.println("a" + (utila-util+abs(min)));
//            System.out.println("b" + (utilb-util+abs(min)));
//            System.out.println("c" + (utilc-util+abs(min)));
//        }

        this.op.update(this.gameinfo.toString(),utila-util+abs(min), 0);
        this.op.update(this.gameinfo.toString(),utilb-util+abs(min), 1);
        this.op.update(this.gameinfo.toString(),utilc-util+abs(min), 2);
    }

    private static <E extends Enum<E>> E fromOrdinal(int ordinal) {
        E[] enumArray = ((Class<E>) ACT.class).getEnumConstants();
        return enumArray[ordinal];
    }

    public static void main(String[] args) {
        System.out.println(fromOrdinal(0));
        System.out.println(fromOrdinal(0).toString());
        System.out.println(fromOrdinal(1));
        System.out.println(fromOrdinal(1).toString());
        System.out.println(fromOrdinal(2));
        System.out.println(fromOrdinal(2).toString());
    }
}

class statData {
    private int numWin;
    private int numLose;
    private int numDraw;

    statData() {
        this.numDraw = 0;
        this.numLose = 0;
        this.numWin = 0;
    }

    void calc(OUTCOME o) {
        if (o == OUTCOME.DREW) this.numDraw++;
        if (o == OUTCOME.LOSE) this.numLose++;
        if (o == OUTCOME.WIN) this.numWin++;
    }

    void reset() {
        this.numDraw = 0;
        this.numLose = 0;
        this.numWin = 0;
    }

    @Override
    public String toString() {
        return  "Win:  " + this.numWin + "\n" +
                "Lose: " + this.numLose + "\n" +
                "Draw: " + this.numDraw;
    }

    public int getNumWin() {return  this.numWin;}
}

class gameInfo {
    private playerInfo pf;
    ACT myACT;
    ACT opsACT;
    OUTCOME out;
    String reason;

    gameInfo() {
        this.pf = new playerInfo();
    }

    void update(Player me, Player op) {
        this.pf.update(me, op);
    }

    void clear() {
        this.pf.clear();
        this.myACT = ACT.first;
        this.opsACT = ACT.first;
        this.out = OUTCOME.DREW;
        this.reason = "";
    }

    boolean isFinish() {
        if (this.myACT == ACT.first || this.opsACT == ACT.first) return false;
        boolean outP1 = false;
        if (Integer.parseInt(this.pf.numbP1) >= 2 && this.myACT == ACT.BARRIER) {
            outP1 = true;
            { //デバッグ
                //System.out.println("me バリア回数違反");
                reason = "me バリア回数違反";
            }
        }
        if (this.myACT == ACT.ATTACK && this.pf.chargedP1.equals("f")) {
            outP1 = true;
            { //デバッグ
                //System.out.println("me チャージ無しアタック");
                reason = "me チャージ無しアタック";
            }
        }
        boolean outP2 = false;
        if (Integer.parseInt(this.pf.numbP2) >= 2 && this.opsACT == ACT.BARRIER) {
            outP2 = true;
            { //デバッグ
                //System.out.println("op バリア回数違反");
                reason = "op バリア回数違反";
            }
        }
         if (this.opsACT == ACT.ATTACK && this.pf.chargedP2.equals("f")) {
            outP2 = true;
            { //デバッグ
                //System.out.println("op チャージ無しアタック");
                reason = "op チャージ無しアタック";
            }
        }

        if (outP1 && outP2) {
            { //デバッグ
                //System.out.println("違反相殺");
                reason += "\n違反相殺";
            }
            return true;
        }
        if (outP1 || outP2) {
            if (outP1) this.out = OUTCOME.LOSE;
            else this.out = OUTCOME.WIN;

            { //デバッグ
                //System.out.println("ルール違反");
                reason += "\nルール違反";
            }

            return true;
        }
        OUTCOME temp = rule(this.myACT, this.opsACT);
        if (temp != OUTCOME.DREW) {
            this.out = temp;
            { //デバッグ
                //System.out.println("ルールによる勝敗");
                reason = "ルールによる勝敗";
            }
            return true;
        }
        return false;
    }

    OUTCOME simulate(ACT act) {

        boolean outP1 = false;
        if (Integer.parseInt(this.pf.numbP1) >= 2 && act == ACT.BARRIER) {
            outP1 = true;
        }
        if (act == ACT.ATTACK && this.pf.chargedP1.equals("f")) {
            outP1 = true;
        }
        boolean outP2 = false;
        if (Integer.parseInt(this.pf.numbP2) >= 2 && opsACT == ACT.BARRIER) {
            outP2 = true;
        }
        if (this.opsACT == ACT.ATTACK && this.pf.chargedP2.equals("f")) {
            outP2 = true;
        }

        if (outP1 && outP2) {
            return OUTCOME.DREW;
        }
        if (outP1 || outP2) {
            if (outP1) return OUTCOME.LOSE;
            else return OUTCOME.WIN;
        }
        return rule(act, this.opsACT);
    }
    OUTCOME opSimulate(ACT act) {

        boolean outP1 = false;
        if (Integer.parseInt(this.pf.numbP1) >= 2 && this.myACT == ACT.BARRIER) {
            outP1 = true;
        }
        if (this.myACT == ACT.ATTACK && this.pf.chargedP1.equals("f")) {
            outP1 = true;
        }
        boolean outP2 = false;
        if (Integer.parseInt(this.pf.numbP2) >= 2 && act == ACT.BARRIER) {
            outP2 = true;
        }
        if (act == ACT.ATTACK && this.pf.chargedP2.equals("f")) {
            outP2 = true;
        }

        if (outP1 && outP2) {
            return OUTCOME.DREW;
        }
        if (outP1 || outP2) {
            if (outP1) return OUTCOME.WIN;
            else return OUTCOME.LOSE;
        }
        return rule(act, this.myACT);
    }

    private OUTCOME rule(ACT myACT, ACT opponenACT){
        if          (myACT == ACT.ATTACK    && opponenACT == ACT.ATTACK)    return OUTCOME.DREW;
        else if     (myACT == ACT.ATTACK    && opponenACT == ACT.BARRIER)   return OUTCOME.DREW;
        else if     (myACT == ACT.ATTACK    && opponenACT == ACT.CHARGE)    return OUTCOME.WIN;

        else if     (myACT == ACT.BARRIER   && opponenACT == ACT.ATTACK)    return OUTCOME.DREW;
        else if     (myACT == ACT.BARRIER   && opponenACT == ACT.BARRIER)   return OUTCOME.DREW;
        else if     (myACT == ACT.BARRIER   && opponenACT == ACT.CHARGE)    return OUTCOME.DREW;

        else if     (myACT == ACT.CHARGE    && opponenACT == ACT.ATTACK)    return OUTCOME.LOSE;
        else if     (myACT == ACT.CHARGE    && opponenACT == ACT.BARRIER)   return OUTCOME.DREW;
        else /*if   (myACT == ACT.CHARGE    && opponenACT == ACT.CHARGE)*/  return OUTCOME.DREW;
    }

    public String toString() {return this.pf.toString(); }
}

class playerInfo {
    String numbP1;
    String chargedP1;
    String numbP2 ;
    String chargedP2;

    playerInfo(){
        this.clear();
    }

    void update(Player me, Player op) {
        this.numbP1 = Integer.toString(me.getNumOfConsecutiveBarriers());
        if (me.isCarge()/*true the t*/) this.chargedP1 = "t";
        else this.chargedP1 = "f";

        this.numbP2 = Integer.toString(op.getNumOfConsecutiveBarriers());
        if (op.isCarge()/*true the t*/) this.chargedP2 = "t";
        else this.chargedP2 = "f";
    }

    void clear() {
        this.numbP1 = "0";
        this.chargedP1 = "f";
        this.numbP2 = "0";
        this.chargedP2 = "f";
    }

    public String toString() {return this.numbP1+this.chargedP1+","+this.numbP2+this.chargedP2;}
}

enum ACT{
    ATTACK,
    BARRIER,
    CHARGE,
    first
}

enum OUTCOME {
    WIN,
    LOSE,
    DREW
}