import java.io.*;
import java.util.*;

class Strategy {

    private int num_param;
    private String path;
    private Map<String, List<Integer>> regretMap;


    Strategy(int size, String inputPath) {
        this.num_param = size;
        this.path = inputPath;
        regretMap = new HashMap<>();
        this.readNodeTree();
    }

    Strategy(int size) {
        this.num_param = size;
        this.path = "";
        regretMap = new HashMap<>();
        // this.readNodeTree();
    }


    double[] getStrategy(String infoStr){
        int sum = 0;
        double[] strategy = new double[this.num_param];

        if (this.regretMap.containsKey(infoStr)) {
            for (int value : this.regretMap.get(infoStr)) if (value > 0) sum += value;
            if (sum > 0) {
                for (int i=0; i<strategy.length; i++) strategy[i] = (double)this.regretMap.get(infoStr).get(i)/(double)sum;
            } else {
                Arrays.fill(strategy, 1.0 / (double) num_param);
            }
        } else {
            List<Integer> regret = new ArrayList<>();
            for (int i=0; i<this.num_param;i++) regret.add(1);
            this.regretMap.put(infoStr,regret);
            for (int i=0; i<strategy.length; i++) strategy[i] = (double) regret.get(i) /(double) num_param;
        }

        return strategy;
    }

    private void setStrategy(String key, List<Integer> regret){
        this.regretMap.put(key,regret);
    }

    void update(int idx, int numRegret, String infoStr){
        if (!regretMap.containsKey(infoStr)) {
            List<Integer> regret = new ArrayList<>();
            for (int i=0; i<this.num_param;i++) regret.add(1);
            this.regretMap.put(infoStr,regret);
        }
        if (idx < 0 || this.regretMap.get(infoStr).size() <= idx) {
            System.err.println("Too small idx");
            return;
        }
        this.regretMap.get(infoStr).set(idx,this.regretMap.get(infoStr).get(idx)+numRegret);
    }

    void writeNodeTree() {
        try  {
            FileWriter file = new FileWriter(this.path);
            PrintWriter writer = new PrintWriter(new BufferedWriter(file));
            for (String s : this.regretMap.keySet()){
                writer.print(s+":");
                writer.println(this.regretMap.get(s));
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readNodeTree() {
        try {
            File f = new File(this.path);
            BufferedReader br = new BufferedReader(new FileReader(f));

            String str;
            List<Integer> regretList;
            String[] data;
            String[] strRegretList;
            while ((str = br.readLine()) != null) {

                data = str.split(":");
                if (data.length != 2) {
                    System.err.println("incorrect data");
                    continue;
                }

                strRegretList = data[1].substring(1,data[1].length()-1).split(",");

                //for (String s : strRegretList) System.out.print(s+", ");

                regretList = new ArrayList<>();
                for (int i=0; i<strRegretList.length; i++) regretList.add(i,Integer.parseInt(strRegretList[i].trim()));
                setStrategy(data[0], regretList);

                //System.out.println(str);

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    int size() {return this.regretMap.size();}

    void compression() {
        for (String s : this.regretMap.keySet()){
            double[] d = this.getStrategy(s);
            for (int i = 0; i < num_param; i++) {
                d[i] *= 100;
                int newRegret = (int) d[i];
                this.regretMap.get(s).set(i, newRegret);
            }
        }
    }

    public Map<String, List<Integer>> getRegretMap() {
        return regretMap;
    }
}
