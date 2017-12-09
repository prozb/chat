import org.sqlite.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static Pattern pattern;
    public static Matcher matcher;
    public static void main(String[] args) {
      /*  System.out.println(emailCheck("rozbich15@gmail.com"));
        System.out.println(emailCheck("12@gmail.com"));
        System.out.println(emailCheck("1111111@gmail.com"));
        System.out.println(emailCheck("1111111@2.com"));*/

        System.out.println(urlChecker("https://www.google.de."));
        System.out.println(urlChecker("ftp://www.google.de"));
        System.out.println(urlChecker("http://www.google.de."));
        System.out.println(urlChecker("https://www.google.aaa"));

    }
    public static boolean urlChecker(String ip){
        pattern = Pattern.compile("^(https|http)://www\\.[a-zA-Z0-9]{2,15}\\.[a-z]{2,3}\\.?$");
        matcher = pattern.matcher(ip);
        return matcher.matches();
    }
    public static boolean emailCheck(String email){
        pattern = Pattern.compile("[A-Za-z0-9]{3,10}@[A-Za-z]{3,6}\\.(com|de|ua)");
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public static boolean check(String msg){

        //Pattern pattern = Pattern.compile("192\\.168\\.[0-9]{1,3}\\.[2-8]{1,3}");
        pattern = Pattern.compile("("+ Commands.CONNECT + "|" + Commands.DISCONNECT + "|"
                                            + Commands.MESSAGE + "):[a-z]");
        Matcher m = pattern.matcher(msg);
        return m.matches();
    }
}
