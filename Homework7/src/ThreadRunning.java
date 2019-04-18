import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadRunning
{
    private AtomicBoolean running = new AtomicBoolean(true);

    public void stopRunning() {
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }
}
