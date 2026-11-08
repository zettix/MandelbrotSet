package com.zettix.fractal;

import java.util.concurrent.Callable;
import java.math.BigDecimal;
import java.math.MathContext;

public class EscapeRunnable implements Callable<Void> { 

    private final BigDecimal far;
    private final int maxcount;
    private final MathContext mc;
    private final BigDecimal[] x;
    private final BigDecimal y;
    public final int[] x_index;
    private final BigDecimal TWO;
    public int escape_count = 0;
    private int[] buff;

    public EscapeRunnable(BigDecimal[] cx, BigDecimal cy,  BigDecimal far, int maxcount, MathContext mc, int[] buff, int[] x) {
        this.far = far;
        this.maxcount = maxcount;
        this.mc = mc;
        this.x = cx;
        this.y = cy;
        this.buff = buff;
        this.TWO = new BigDecimal("2", mc);
        this.x_index = x;
    }

    private void Escape(int idx) {
        int count =  0;
        BigDecimal x2 = x[idx].add(BigDecimal.ZERO);
        BigDecimal y2 = y.add(BigDecimal.ZERO);
        try {
          while (true) {
            BigDecimal a2 = x2.multiply(x2, mc);
            BigDecimal b2 = y2.multiply(y2, mc);
            BigDecimal radius2 = a2.add(b2, mc);
            if (radius2.compareTo(far) > 0) {
                break;
            }
            count++;
            if (count >= maxcount) {
              break;
            }
            y2 = TWO.multiply(x2, mc).multiply(y2, mc).add(y, mc);
            x2 = a2.add(b2.negate()).add(x[idx], mc);
          }
        } catch (java.lang.ArithmeticException ex) {
            count = maxcount;
        }
        escape_count = count;
    }

    public Void call() {
        for (int idx : x_index) {
            Escape(idx);
            buff[idx] = escape_count;
        }
        return null;
    }
}