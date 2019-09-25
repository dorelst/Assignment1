import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Assignment1 {
    ExecutorService detectionNodesExecutor = Executors.newFixedThreadPool(5);

    public void runDetectionNodes() {
        final CountDownLatch countDownLatch = new CountDownLatch(4);
        List<DetectionNodeQues> detectionNodesQues = new ArrayList<>();
        int numberOfIterations = 100;
        for (int i=0; i<4; i++) {
            detectionNodesQues.add(new DetectionNodeQues(i));
        }
        //List<Future> futures = new ArrayList<>();
        List<DetectionNodeProcessor> tasks = new ArrayList<>();

        for (int i=0; i<4; i++) {
            List<DetectionNodeQues> createOtherProcessorsQues = new ArrayList<>();
            for (int j=0; j<4; j++) {
                if (j != i) {
                    createOtherProcessorsQues.add(detectionNodesQues.get(j));
                }
            }
            tasks.add(new DetectionNodeProcessor(i, detectionNodesQues.get(i), createOtherProcessorsQues, numberOfIterations, countDownLatch));
        }
        Sampler sampler = new Sampler(countDownLatch);

        for (int i=0; i<4; i++) {
               detectionNodesExecutor.execute(tasks.get(i));
        }
        detectionNodesExecutor.execute(sampler);
/*
        detectionNodesExecutor.execute(() -> {
            for (int i = 0; i<100; i++) {
                System.out.println("Inside sampler. i = "+i);

            }
            countDownLatch.countDown();
        });
*//*
        for (int i=0; i<10; i++) {
            System.out.println("Inside main sampler. i = "+i);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*/
        detectionNodesExecutor.shutdown();

    }
    public static void main(String[] args) {
        Assignment1 assignment1 = new Assignment1();
        assignment1.runDetectionNodes();
    }
}
