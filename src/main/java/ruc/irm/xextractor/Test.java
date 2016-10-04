package ruc.irm.xextractor;

/**
 * @author Tian Xia
 * @date Aug 31, 2016 11:02
 */
public class Test {
    public static void main(String[] args) {
        String s = "";
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<s.length(); i++) {
            char ch = s.charAt(i);
            if(ch==' ' || ch=='\n') continue;
            sb.append(ch);
        }
        System.out.println(sb.toString());
    }
}
