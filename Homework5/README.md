### 输入输出接口使用方法

参考博客 [idea添加jar包依赖](https://blog.csdn.net/zwj1030711290/article/details/56678353/)

具体使用方法看指导书。

已提供两个jar包，自行下载使用。注意添加依赖是针对单个工程的，其它工程若想引用，必须逐个添加。

### 参考架构

这次作业简直是摸鱼划水。只要会一点多线程和同步就能做。典型的生产者消费者模型。以后的电梯要考虑顺路捎带，多个电梯共同调度的问题，必须用客户端-服务端模型。（而且，就像实际的电梯一样，内外按键，使用look算法，而不是FAFS）

`class Elevator extends Thread` 电梯类，继承线程类，接收请求

`class InputThread extends Thread` 输入线程，产生请求

`class WhenToStop` 共享消息类，用于指示输入是否完毕

具体架构查看Homework8--第5-7次作业总结