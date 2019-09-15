import java.util.Random;

class Player {

    private int numOfConsecutiveBarriers;
    private boolean isCarge;
    private Strategy strate;
    private int NUM_ACTIONS = 3;
    private static final Random random = new Random();

    Player(String path) {
        this.strate = new Strategy(NUM_ACTIONS,path);
    }

    void set(){
        this.numOfConsecutiveBarriers = 0;
        this.isCarge = false;
    }

    private int getAction(double[] strategy) {
        double r = random.nextDouble();
        int a = 0;
        double cumulativeProbability =  0;
        while (a < NUM_ACTIONS - 1) {
            cumulativeProbability += strategy[a];
            if (r < cumulativeProbability)
                break;
            a++;
        }
        return a;
    }

    int Hand(String info){
        int a = getAction(this.strate.getStrategy(info));
        switch (a) {
            case 0: return this.attack();
            case 1: return this.barrier();
            case 2: return this.charge();
            default: System.err.println("unexpected index of action from getAction()");
        }
        return 2;
    }

    int sendStrategyHand(double[] d){
        double[] d2 = {d[0], d[1], d[2]};
        if (!this.isCarge) d2[0] = 0;
        if (this.numOfConsecutiveBarriers == 2) d2[1] = 0;
        double sum = 0;
        for (double val : d2) if (val>0) sum += val;
        if (sum == 0) return this.charge();

        for (int i=0; i<d2.length; i++) {
            d2[i] /= sum;
        }
        int select = getAction(d2);

        switch (select) {
            case 0: return this.attack();
            case 1: return this.barrier();
            case 2: return this.charge();
            default: System.out.println("unexpected index of action from getAction()");
        }

        return 2;
    }

    private int attack(){
        this.isCarge = false;
        this.numOfConsecutiveBarriers = 0;
        return 0;
    }

    private int barrier(){
        this.numOfConsecutiveBarriers++;
        return 1;
    }

    private int charge(){
        this.isCarge = true;
        this.numOfConsecutiveBarriers = 0;
        return 2;
    }

    int getNumOfConsecutiveBarriers() {
        return numOfConsecutiveBarriers;
    }

    boolean isCarge() {
        return this.isCarge;
    }

    void update(String info, int regret, int idx) {
        this.strate.update(idx,regret,info);
    }

    void save() {
        this.strate.writeNodeTree();
    }

    public Strategy getStrate() {
        return strate;
    }

    public void compStrategy() {
        this.strate.compression();
    }

    int size() {return this.strate.size();}
}
