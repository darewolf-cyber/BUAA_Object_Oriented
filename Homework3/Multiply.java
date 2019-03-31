public final class Multiply extends Expr
{
    private Expr expr1;
    private Expr expr2;

    Multiply(Expr e1, Expr e2)
    {
        expr1 = e1;
        expr2 = e2;
    }

    public Expr getExpr1() {
        return expr1;
    }

    public Expr getExpr2() {
        return expr2;
    }

    @Override
    public Expr qiuDao()
    {
        Expr mul1 = expr1.qiuDao().multiply(expr2);
        Expr mul2 = expr2.qiuDao().multiply(expr1);
        return mul1.add(mul2);
    }

}
