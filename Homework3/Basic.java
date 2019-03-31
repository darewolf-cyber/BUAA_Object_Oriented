import java.math.BigInteger;

public final class Basic extends Expr
{
    private static final int CONSTANT = 0;
    private static final int POWER = 1;
    private static final int SIN = 2;
    private static final int COS = 3;

    public static final String[] FUNC_NAME = {"", "x", "sin(x)", "cos(x)"};

    private BigInteger para;
    private int expClass;

    public int getExpClass() {
        return expClass;
    }

    public BigInteger getPara() {
        return para;
    }

    public Basic(int funcClass, BigInteger arg)
    {
        if (arg.equals(ZERO) && funcClass != 0) {
            expClass = 0;
            para = ONE;
        } else {
            para = arg;
            expClass = funcClass;
        }
    }

    @Override
    public Expr qiuDao()
    {
        final Expr tmp1 = new Basic(CONSTANT, para);
        switch (expClass)
        {
            case CONSTANT:
                return new Basic(0, ZERO);
            case POWER:
                if (para.equals(ZERO) || para.equals(ONE)) {
                    return tmp1;
                } else {
                    Expr tmp2 =
                            new Basic(POWER, para.subtract(ONE));
                    return tmp1.multiply(tmp2);
                }
            case SIN:
                if (para.equals(ZERO)) {
                    return new Basic(0, ZERO);
                } else {
                    Expr cosx = new Basic(COS, ONE);
                    if (para.equals(ONE)) {
                        return cosx;
                    } else {
                        Expr tmp2 =
                                new Basic(SIN, para.subtract(ONE));
                        return tmp1.multiply(tmp2).multiply(cosx);
                    }
                }
            case COS:
                final Expr ccc = new Basic(0, NEGONE);
                if (para.equals(ZERO)) {
                    return new Basic(0, ZERO);
                } else {
                    Expr sinx = new Basic(SIN, ONE);
                    sinx = ccc.multiply(sinx);
                    if (para.equals(ONE)) {
                        return sinx;
                    } else {
                        Expr tmp2 =
                                new Basic(COS, para.subtract(ONE));
                        return tmp1.multiply(tmp2).multiply(sinx);
                    }
                }
            default:
                throw new UnsupportedOperationException();
        }
    }
}
