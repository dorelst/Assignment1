import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Assignment1 {
    ExecutorService detectionNodesExecutor = Executors.newFixedThreadPool(5);

    public void runDetectionNodes() {
        int numberOfIterations = 10000;
        final CountDownLatch countDownLatch = new CountDownLatch(4);
        List<DetectionNodeQues> detectionNodesQues = new ArrayList<>();
        for (int i=0; i<4; i++) {
            detectionNodesQues.add(new DetectionNodeQues(i));
        }
        List<Future<?>> futures = new ArrayList<>();
        List<DetectionNodeProcessor> tasks = new ArrayList<>();


        for (int i=0; i<4; i++) {
            List<DetectionNodeQues> createOtherProcessorsQues = new ArrayList<>();
            DetectionNodeProcessor dnp;
            for (int j=0; j<4; j++) {
                if (j != i) {
                    createOtherProcessorsQues.add(detectionNodesQues.get(j));
                }
            }
            tasks.add(new DetectionNodeProcessor(i, detectionNodesQues.get(i), createOtherProcessorsQues, numberOfIterations, countDownLatch));
        }

        Future<?> f;
        for (int i=0; i<4; i++) {
               f = detectionNodesExecutor.submit(tasks.get(i));
               futures.add(f);
        }
        Sampler sampler = new Sampler(countDownLatch, futures, detectionNodesQues);
        f = detectionNodesExecutor.submit(sampler);
        //futures.add(f);
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
*/
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        detectionNodesExecutor.shutdown();

    }
    public static void main(String[] args) {
        Assignment1 assignment1 = new Assignment1();
        assignment1.runDetectionNodes();
    }
}
