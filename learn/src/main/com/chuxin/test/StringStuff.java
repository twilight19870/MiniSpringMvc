package chuxin.test;//(c) A+ Computer Science
//www.apluscompsci.com
//Name -

public class StringStuff {
    private String word;

    /*有参构造方法,初始化成员变量word*/
    public StringStuff(String word) {
        this.word = word;
    }

    public String getFirstLastLetters() {
        return word.substring(0, 1) + word.substring(word.length() - 1);
    }

    public String getMiddleLetter() {
        return word.substring(word.length() / 2, word.length() / 2 + 1);
    }

    public boolean sameFirstLastLetters() {
        String letter1 = (word.substring(0, 1));
        String lastletter = (word.substring((word.length() - 1)));
        if (letter1.equals(lastletter)) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        return "" + word;
    }
}