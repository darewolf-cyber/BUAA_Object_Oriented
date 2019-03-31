public final class AddSub extends Expr
{
    private boolean addOrSub;
    private Expr expr1;
    private Expr expr2;

    AddSub(boolean as, Expr e1, Expr e2)
    {
        addOrSub = as;
        expr1 = e1;
        expr2 = e2;
    }

    public boolean isAddOrSub() {
        return addOrSub;
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
        if (addOrSub) {
            return expr1.qiuDao().add(expr2.qiuDao());
        } else {
            return expr1.qiuDao().sub(expr2.qiuDao());
        }
    }
}
