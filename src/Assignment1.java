import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//This is the class that contains the main method and is used to launch all the other threads that do the work
public class Assignment1 {
    ExecutorService detectionNodesExecutor = Executors.newFixedThreadPool(6);

    private void runDetectionNodes() {
        //numberOfIterations controls how many iterations (events) a node will do before stops
        int numberOfIterations = 10000;
        final CountDownLatch countDownLatch = new CountDownLatch(4);

        //detectionNodesQues keeps track of Nodes Ques
        List<DetectionNodeQues> detectionNodesQues = new ArrayList<>();
        for (int i=0; i<4; i++) {
            detectionNodesQues.add(new DetectionNodeQues(i));
        }
        List<Future<?>> futures = new ArrayList<>();
        List<DetectionNodeProcessor> tasks = new ArrayList<>();

        //DetectinNodeProcessor is the class that implements Runnable and it will feeded to the threads to do the work
        //Each DetectionNodeProcessor will get its own que (DetectionNodeQues) and a list with the other nodes ques
        //to send them messages to their receiving que
        for (int i=0; i<4; i++) {
            List<DetectionNodeQues> createOtherProcessorsQues = new ArrayList<>();
            for (int j=0; j<4; j++) {
                if (j != i) {
                    createOtherProcessorsQues.add(detectionNodesQues.get(j));
                }
            }
            tasks.add(new DetectionNodeProcessor(i, detectionNodesQues.get(i), createOtherProcessorsQues, numberOfIterations, countDownLatch));
        }


        Future<?> f;

        //Sampler is the thread that collects samples of the detection nodes logical clocks during their execution
        Sampler sampler = new Sampler(countDownLatch, futures, detectionNodesQues);
        detectionNodesExecutor.submit(sampler);

        //The threads are launched and main thread will wait for all of them to finish, before it finishes
        for (int i=0; i<4; i++) {
            f = detectionNodesExecutor.submit(tasks.get(i));
            futures.add(f);
        }

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