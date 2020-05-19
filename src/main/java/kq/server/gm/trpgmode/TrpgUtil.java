package kq.server.gm.trpgmode;

import kq.server.util.RandomUtil;

/**
 * trpg工具类
 */
public class TrpgUtil {

    /**
     * 进行骰子判定
     * @return 1-100
     */
    public static int getTrpgJudge(){
        int point = RandomUtil.getNextInt(100) + 1;
        return point;
    }

    /**
     * 判定结果
     * @param judge
     * @param i
     * @return
     */
    public static String judgeMsg(int judge, int i) {
        if(judge <= i && judge <= 4) return "大成功";
        if(judge <= i) return "成功";
        if(judge > i && judge >= 97) return "大失败";
        if(judge > i) return "失败";

        return "失败";
    }

    /**
     * 目标m1是否是s1
     * @param s1
     * @param m1
     * @return
     */
    public static boolean isDoOpt(String s1, String m1){
        return s1.equals(m1);
    }

    /**
     * 目标m1,m2是否是s1,s2
     * @param s1
     * @param s2
     * @param m1
     * @param m2
     * @return
     */
    public static boolean isDoOpt(String s1, String s2, String m1, String m2){
        return (s1.equals(m1) && s2.equals(m2));
    }
}
