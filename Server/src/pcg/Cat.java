package pcg;

public class Cat extends Thread{
    @Override
    public void run() {
        int i = 0;
        while (!Thread.interrupted()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(i++);
        }
        super.run();
    }
}
