package chuxin.test;//(c) A+ Computer Science
//www.apluscompsci.com
//Name -

import com.sun.deploy.util.StringUtils;

import java.util.Scanner;

public class StringRunner {
    public static void main(String[] args) {
        String str = "aAbbAbbsA";
        if (str != null && str.length() > 0) {
            char[] chars = str.toCharArray();
            if (chars.length == 1) {
                System.out.println("no");
                return;
            }
            char a = chars[0];
            for (int i = 1; i < chars.length; i++) {
                if (a == chars[i]) {
                    System.out.println("yes");
                    return;
                } else if (a != chars[i] && i == (chars.length - 1)) {
                    System.out.println("no");
                }
            }
        }

    }
}