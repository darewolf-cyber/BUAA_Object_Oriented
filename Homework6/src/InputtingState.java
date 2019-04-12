import java.util.concurrent.atomic.AtomicBoolean;

public class InputtingState
{
    private AtomicBoolean inputting = new AtomicBoolean(true);

    public void stopInputting() {
        inputting.set(false);
    }

    public boolean isInputting() {
        return inputting.get();
    }
}
