import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DetectionNodeQues {
    private int id;
    private volatile int logicalClock;
    private List<String> sendingQue;
    private List<String> receivingQue;
    private Queue<String> receivedMessages;
    private List<String> eventsJournal;


    public DetectionNodeQues (int id) {
        this.id = id;
        this.logicalClock = 0;
        this.sendingQue = new ArrayList<>();
        String initialMessage = "Sent Messages Que for Detection Node "+id;
        this.sendingQue.add(initialMessage);
        this.receivingQue = new ArrayList<>();
        this.eventsJournal = new ArrayList<>();
        this.receivedMessages = new ArrayDeque<>();
    }

    public int getId() {
        return id;
    }

    public int getLogicalClock() {
        return logicalClock;
    }

    public void setLogicalClock(int logicalClock) {
        this.logicalClock = logicalClock;
    }

    public List<String> getSendingQue() {
        return sendingQue;
    }

    public List<String> getReceivingQue() {
        return receivingQue;
    }

    public List<String> getEventsJournal() {
        return eventsJournal;
    }

    public Queue<String> getReceivedMessages() {
        return receivedMessages;
    }

    public void addMessageToSendingQue(String message) {
        getSendingQue().add(message);
    }

    public void addMessageToReceivingQue(String message) {
        synchronized (getReceivingQue()){
            getReceivingQue().add(message);
        }
    }

    public void addMessageToReceivedMessages(String message) {
        getReceivedMessages().add(message);
    }

    public String getMessageFromReceivingQue(int senderNodeId) {
        String message = "";
        synchronized (getReceivingQue()){
            if (!getReceivingQue().isEmpty()) {
                Iterator<String> iterator = getReceivingQue().iterator();
                int nodeId;
                while (iterator.hasNext()) {
                    message = iterator.next();
                    nodeId = getNodeIdFromIncomingMessage(message);
                    if (nodeId == senderNodeId) {
                        iterator.remove();
                        break;
                    }
                    message = "";
                }
            }
        }
        return message;
    }

    public int getNodeIdFromIncomingMessage (String message) {
        String[] splitMessage = message.split(" ", 3);
        return (Integer.parseInt(splitMessage[1]));
    }

    public int getNodeLogicalClockFromIncomingMessage(String message) {
        String[] splitMessage = message.split(" ", 2);
        return (Integer.parseInt(splitMessage[0]));
    }

    public void addMessageToEventsJournal (String message) {
        getEventsJournal().add(message);
    }
}
