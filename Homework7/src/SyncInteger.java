public class SyncInteger
{
    private int value;

    public SyncInteger(int x) {
        value = x;
    }

    public synchronized int getValue(int choose) {
        if (choose < 0) {
            value--;
        } else if (choose > 0) {
            value++;
        }
        return value;
    }
}
