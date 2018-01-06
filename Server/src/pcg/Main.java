package pcg;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Cat cat = new Cat();
        cat.setDaemon(true);
        cat.start();
        int i = 10;
        while (true){
            Thread.sleep(1000);
            if(i == 13)
                cat.interrupt();
            System.out.println(i++);
        }
    }
}
