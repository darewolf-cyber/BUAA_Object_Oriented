import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class InputThread extends Thread
{
    private final LinkedBlockingQueue<PersonRequest> requests;
    private WhenToStop state;
    private ElevatorInput elevatorInput = new ElevatorInput(System.in);
    private PersonRequest request = null;

    public InputThread(LinkedBlockingQueue<PersonRequest> queue,
                       WhenToStop condition) {
        requests = queue;
        state = condition;
    }

    @Override
    public void run() {
        while ((request = elevatorInput.nextPersonRequest()) != null) {
            try {
                requests.put(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        state.stopInputting();
        try {
            elevatorInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
