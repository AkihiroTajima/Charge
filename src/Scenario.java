import java.io.*;
import java.util.Arrays;

public class Scenario {
    private Game game;
    private Scenario() {
        this.game = new Game();
    }
    private void test(int numGame) {
        for (int i = 0; i < numGame; i++) {
            this.game.printGame();
//            this.game.cfr(true,true);
//            this.game.comp();
        }
        this.game.save();
        System.out.println(game.sd.toString());
    }
    private void strategyDiff(int numGame) {
        double diff;
        try  {
            FileWriter file = new FileWriter("diff.csv");
            PrintWriter writer = new PrintWriter(new BufferedWriter(file));
            for (int i = 0; i < numGame; i++) {
                diff = game.calcStrategy();
                writer.println(diff + ",");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void cfrSelf(int numGame){
        for (int i = 0; i < numGame; i++) {
            //this.game.printGame();
            this.game.cfr(true,false);
            if (i % (numGame/100) == 0) System.out.println((double) i/numGame);
        }

        this.game.save();
        System.out.println(this.game.sd.toString());
        System.out.println( (double) this.game.sd.getNumWin() / numGame);
    }
    private void testStrategy(int dense, int numGame) {
        try {
            FileWriter file = new FileWriter("data/out1.csv", true);
            PrintWriter writer = new PrintWriter(new BufferedWriter(file));

            for (double A = 0; A <= dense; A++)
                for (double B = 0; B <= dense; B++)
                    for (double C = 0; C <= dense; C++) {
                        if (A + B + C != dense) continue;
                        double a = A / dense;
                        double b = B / dense;
                        double c = C / dense;

                        double[] d = {a, b, c};
                        for (int i = 0; i < numGame; i++) {
                            game.opFixed(false, d);
                        }
                        writer.println((double) game.sd.getNumWin() / (double) numGame + "," + Arrays.toString(d));
                        game.sd.reset();
                    }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void clacWinRate(int numGame) {
        try  {
            FileWriter file = new FileWriter("data/out9.csv");
            PrintWriter writer = new PrintWriter(new BufferedWriter(file));
            for (int i = 0; i < numGame; i++) {
//                this.game.comp(false,true);
                this.game.comp(true,false);
                writer.println( (double) game.sd.getNumWin() / (i+1));
                if ( i%1000000 == 0 )System.out.println(progress(i+1, numGame));
            }
            this.game.save();
            System.out.println(game.sd.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void clacWinRate(int numGame, int numTurn) {
        try  {
            FileWriter file = new FileWriter("data/out5.csv");
            PrintWriter writer = new PrintWriter(new BufferedWriter(file));
            boolean lm = false;
            boolean lo = true;
            int div = 1;
            for (int t = 0; t < numTurn; t++) {
                lm = !lm;
                lo = !lo;
                game.sd.reset();
                for (int i = 0; i < numGame; i++) {
                    this.game.comp(lm,lo);
                    writer.println( (double) game.sd.getNumWin() / div);
                    div++;
                }
            }

            this.game.save();
            System.out.println(game.sd.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void compCFR(int numGame) {
        for (int i = 0; i < numGame; i++) {
            //this.game.printGame();
            this.game.comp(true,false);
            if (i % (numGame/100) == 0) System.out.println((double) i/numGame);
        }
        this.game.save();
        System.out.println(this.game.sd.toString());
        System.out.println( (double) this.game.sd.getNumWin() / numGame);
    }

    private String progress(int now, int lim) {
        return String.format("%.2f",(double) now/lim * 100) + " %";
    }

    public static void main(String[] args) {
        int numGame = (int) 1e5;
        int numStable = (int) 1e5;
        int numTurn = 3;
        Scenario s = new Scenario();
//        s.cfrSelf(numGame);
//        s.testStrategy(100,1000);
//        s.test(numGame);
//        s.clacWinRate(numGame);
        s.compCFR(numGame);
    }
}
