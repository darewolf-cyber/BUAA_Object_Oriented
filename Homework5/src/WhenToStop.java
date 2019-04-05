public class WhenToStop
{
    private boolean inputting = true;

    public void stopInputting() {
        inputting = false;
    }

    public boolean isInputting() {
        return inputting;
    }
}
