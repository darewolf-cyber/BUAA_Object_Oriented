### 输入输出接口

具体用法见指导书。注意输入是1.4版本，输出是1.1版本，别搞错了。

### 参考架构

1、电梯类，同上次。只不过，增加属性：核载、速度、停靠层

2、调度器类：这是新增的东西，它用来把接收到的请求分配给各个电梯

3、输入线程，不变

三部电梯统一建模，单例模式。用lock和condition保证互斥访问请求队列。

停止机制：用一个“原子整数”（自己用同步实现的），记录当前未完成的请求数量（一个请求从产生那一刻起到到达目标楼层出去的这段时间，都算未完成。所以，在中间换乘出电梯不算完成）。用一个原子布尔变量指示输入状态。当未完成请求为0且停止输入时，三部电梯和调度器都要停下。（坑点：一部电梯停下之后，要通知其它电梯也停下，否则会无限等待。所以多用signalAll没不是）

输出同步：输出对stdout是竞争关系，如果输出不同步，可能出现一个输出插进另一个输出的情况，造成混乱。因为只有电梯线程有输出，所以输出加一个static成员的锁，来保证每次只有一个电梯线程输出。

楼层自己分析，记得需要特判第3层

优化时间性能：随机分配（平摊分析）。具体参见Homework8--第5-7次作业总结。

dalao们用图论等东西，我不会用（至少在这个问题上不会用），而且我觉得这是面向对象课，不是算法或软工，没必要那么玩。