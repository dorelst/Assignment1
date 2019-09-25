import java.util.concurrent.CountDownLatch;

public class Sampler implements Runnable {

    private CountDownLatch countDownLatch;

    public Sampler (CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        for (int i = 0; i<10; i++) {
            System.out.println("Inside sampler. i = "+i);
        }

        countDownLatch.countDown();
    }
}
