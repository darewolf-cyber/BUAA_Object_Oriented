## 简述

**具体类设计请参看Homework4-前三次作业总结**

简要架构：

`Expr`：表达式类（抽象类，含抽象方法`public void qiuDao()`）

`Basic extends Expr`：基本初等函数（包括常数、幂函数、三角函数及其幂）

`AddSub extends Expr`：两个表达式相加或相减所得表达式

`Multiply extends Expr`：两个表达式相乘所得表达式

`Composite extends Expr`：两个函数复合所得表达式

