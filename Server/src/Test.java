import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static Scanner in = new Scanner(System.in);
    public static Pattern pattern;
    public static Matcher matcher;

    public static void main(String[] args) {
      /*  System.out.println(emailCheck("rozbich15@gmail.com"));
        System.out.println(emailCheck("12@gmail.com"));
        System.out.println(emailCheck("1111111@gmail.com"));
        System.out.println(emailCheck("1111111@2.com"));*/

      /*  System.out.println(urlChecker("https://www.google.de."));
        System.out.println(urlChecker("ftp://www.google.de"));
        System.out.println(urlChecker("http://www.google.de."));
        System.out.println(urlChecker("https://www.google.aaa"));*/
/*
        System.out.println(someDist("123123123123"));
        System.out.println(someDist("123lkasfja123"));
        System.out.println(someDist("\n\r"));*/

        //System.out.println(someDist("BACON asdjafspj"));
        String key = "";

        while (!key.equals("exit")){
            String name = in.nextLine();
            if (connect(name)) {
                name = name.replaceAll("connect:\\s", "");
                System.out.println("Hello, your name is: " + name);
            }else{
                System.out.println("wrong name");
            }
        }
    }
    public static boolean connect(String val){
        pattern = Pattern.compile("^connect: [a-zA-Z0-9]{3,30}");

        matcher = pattern.matcher(val);
        return matcher.matches();
    }
    public static boolean someDist(String msg){
        //pattern = Pattern.compile("^BACON.+");
        pattern = Pattern.compile("BACON.*");
        //pattern = Pattern.compile(".+BACON.+");
        //pattern = Pattern.compile("\\s+");

        matcher = pattern.matcher(msg);
        return  matcher.matches();
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
        pattern = Pattern.compile("("+ Constants.CONNECT + "|" + Constants.DISCONNECT + "|"
                                            + Constants.MESSAGE + "):[a-z]");
        Matcher m = pattern.matcher(msg);
        return m.matches();
    }
}
