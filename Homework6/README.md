### 输入输出接口

和上次作业不同，请注意。请下载本次作业专用接口。具体见指导书。

### 参考架构

把请求分为电梯外请求（按照出发楼层分类）和电梯内乘客（按照目标楼层分类）。

`class InputThread extends Thread` ：输入线程

`class Elevator extends Thread` ：电梯线程

`class InputtingState` ：boolean包装类，指示输入是否完成。内部使用原子类型

线程间通信：可重入锁`Reentrantlock`，用`lock() unlock() await() signalAll()`四个函数实现同步。当电梯内无乘客，且外部无请求时，电梯停下等待。输入线程是主动线程，不因为电梯而等待。**坑点：在notify其他线程之后必须释放锁，否则其他线程将没有运行的机会。另外，lock和unlock一定要配对使用，否则会引发异常**。

电梯操作：开门、关门、上客、下客、移动一层

电梯属性：运行方向、开门状态、乘客数量

容器：电梯内乘客、电梯外请求（用`HashMap<Integer, LinkedBlockingQueue<PersonRequest>>`）。注意一定要使用线程安全的容器。不要认为自己加锁就不需要线程安全的容器了，至少你的元素个数是线程不安全的，因为它不是原子操作。Java为每种基本类型封装了原子Atomic类型，自行百度。

具体架构参考Homework8--5~7次作业总结