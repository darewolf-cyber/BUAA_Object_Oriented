import java.math.BigInteger;

public abstract class Expr
{
    public static final BigInteger ZERO = new BigInteger("0");
    public static final BigInteger ONE = new BigInteger("1");
    public static final BigInteger NEGONE = new BigInteger("-1");

    private boolean isConstant()
    {
        return getClass() == Basic.class
                && ((Basic) this).getExpClass() == 0;
    }

    private boolean equalsToZero()
    {
        return isConstant()
                && ((Basic) this).getPara().equals(ZERO);
    }

    private boolean equalsToOne()
    {
        return isConstant()
                && ((Basic) this).getPara().equals(ONE);
    }

    private boolean isX()
    {
        return getClass() == Basic.class
                && ((Basic) this).getExpClass() == 1
                && ((Basic) this).getPara().equals(ONE);
    }

    public final Expr add(Expr expr)
    {
        if (isConstant() && expr.isConstant()) {
            Basic num1 = (Basic) this;
            Basic num2 = (Basic) expr;
            return new Basic(0,
                    num1.getPara().add(num2.getPara()));
        } else if (equalsToZero()) {
            return expr;
        } else if (expr.equalsToZero()) {
            return this;
        } else {
            return new AddSub(true, this, expr);
        }
    }

    public final Expr sub(Expr expr)
    {
        if (isConstant() && expr.isConstant()) {
            Basic num1 = (Basic) this;
            Basic num2 = (Basic) expr;
            return new Basic(0,
                    num1.getPara().subtract(num2.getPara()));
        } else if (equalsToZero()) {
            final Expr tmp = new Basic(0, NEGONE);
            return new Multiply(tmp, expr);
        } else if (expr.equalsToZero()) {
            return this;
        } else {
            return new AddSub(true, this, expr);
        }
    }

    public final Expr multiply(Expr expr)
    {
        if (isConstant() && expr.isConstant()) {
            Basic num1 = (Basic) this;
            Basic num2 = (Basic) expr;
            return new Basic(0,
                    num1.getPara().multiply(num2.getPara()));
        } else if (equalsToZero() || expr.equalsToZero()) {
            return new Basic(0, ZERO);
        } else if (equalsToOne()) {
            return expr;
        } else if (expr.equalsToOne()) {
            return this;
        } else {
            return new Multiply(this, expr);
        }
    }

    public final Expr composite(Expr expr)
    {
        if (isConstant()) {
            return this;
        } else if (isX()) {
            return expr;
        } else if (expr.isX()) {
            return this;
        } else if (getClass() == AddSub.class) {
            AddSub tmp = (AddSub) this;
            Expr tmp1 = tmp.getExpr1().composite(expr);
            Expr tmp2 = tmp.getExpr2().composite(expr);
            if (tmp.isAddOrSub()) {
                return tmp1.add(tmp2);
            } else {
                return tmp1.sub(tmp2);
            }
        } else if (getClass() == Multiply.class) {
            Multiply tmp = (Multiply) this;
            Expr tmp1 = tmp.getExpr1().composite(expr);
            Expr tmp2 = tmp.getExpr2().composite(expr);
            return tmp1.multiply(tmp2);
        } else {
            return new Composite(this, expr);
        }
    }

    public abstract Expr qiuDao();
}
