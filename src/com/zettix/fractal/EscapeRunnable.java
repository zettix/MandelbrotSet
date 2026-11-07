package com.zettix.fractal;

import java.util.concurrent.Callable;
import java.math.BigDecimal;
import java.math.MathContext;

public class EscapeRunnable implements Callable<Void> { 

    private BigDecimal far;
    private int maxcount;
    private MathContext mc;
    private BigDecimal x;
    private BigDecimal y;
    public int x_index;
    public int y_index;
    private int limit_count = 0;
    private final BigDecimal TWO;
    public int escape_count = 0;
    private int[] buff;

    public EscapeRunnable(BigDecimal cx, BigDecimal cy,  BigDecimal far, int maxcount, MathContext mc, int[] buff, int x, int y) {
        this.far = far;
        this.maxcount = maxcount;
        this.mc = mc;
        this.x = cx;
        this.y = cy;
        this.buff = buff;
        this.TWO = new BigDecimal("2", mc);
        this.x_index = x;
        this.y_index = y;
        //System.out.println("EscapeRunnable initialized for point (" + x + ", " + y + ")");
    }

    private void Escape() {
        //BigDecimal radius = x.multiply(x, mc).add(y.multiply(y, mc), mc);
        int count =  0;
        BigDecimal x2 = x.add(BigDecimal.ZERO);
        BigDecimal y2 = y.add(BigDecimal.ZERO);
        //System.out.println("EscapeRunnable initialized for point (" + x + " + " + y + " i) with far: " + 
         //   far + " and maxcount: " + maxcount + " and radius: " + radius);
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
              limit_count++;
              break;
            }
            y2 = TWO.multiply(x2, mc).multiply(y2, mc).add(y, mc);
            x2 = a2.add(b2.negate()).add(x, mc);
            //System.out.println("x:" + x + " y:" + y);
          }
        } catch (java.lang.ArithmeticException ex) {
            count = maxcount;
        }
        //System.out.println("Escape(" + x_index + ", " + y_index + ") count: " + count);
        escape_count = count;
    }

    public Void call() {
        //System.out.println("EscapeThread.run() start");
        Escape();
        //System.out.println("EscapeThread.run() end");
        buff[x_index] = escape_count;
        return null;
    }
}