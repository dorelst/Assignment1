import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TestThreadCommunication {
    ExecutorService detectionNodesExecutor = Executors.newFixedThreadPool(4);

    private static class DetectionNode implements Callable<List<Integer>> {
        private int iterations;
        private volatile List<Integer> receiveingQue;
        private List<Integer> outgoingQue;
        public DetectionNode(int numberOfIterations) {
            this.iterations = numberOfIterations;
            this.receiveingQue = new ArrayList<>();
            this.outgoingQue = new ArrayList<>();
        }
        public List<Integer> call(){
            for(int i=0;i<iterations;i++) {
                int decision = (int)(Math.random()*10);
                if ((decision == 3) || (decision == 7)) {
                    //send message
                    receiveingQue.add(decision);
                } else {
                    outgoingQue.add(decision);
                }
            }
            return receiveingQue;
        }
    }

    public void runDetectionsNode() {
        List<Future<List<Integer>>> futures = new ArrayList<>();
        List<DetectionNode> tasks = new ArrayList<>();
        for(int i=0;i<4;i++){
            tasks.add(new DetectionNode(10));
        }
        try {
            futures = detectionNodesExecutor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        detectionNodesExecutor.shutdown();

        for(Future<List<Integer>> future : futures) {
            try {
                List<Integer> result = future.get();
                System.out.println("result "+result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        TestThreadCommunication testThreadCommunication = new TestThreadCommunication();
        testThreadCommunication.runDetectionsNode();
    }
}
