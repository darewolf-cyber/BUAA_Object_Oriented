public final class Composite extends Expr
{
    private Expr outer;
    private Expr inner;

    Composite(Expr e1, Expr e2)
    {
        outer = e1;
        inner = e2;
    }

    public Expr getOuter() {
        return outer;
    }

    public Expr getInner() {
        return inner;
    }

    @Override
    public Expr qiuDao()
    {
        Expr out = outer.qiuDao().composite(inner);
        Expr in = inner.qiuDao();
        return out.multiply(in);
    }
}
