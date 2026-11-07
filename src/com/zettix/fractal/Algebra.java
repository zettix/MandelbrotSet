package com.zettix.fractal;



import java.math.BigDecimal;

/**
 * 
 * Algebra: Scratchpad for algebraic functions to use generated images of a fractal 
 * to calculate new points for zooming in. The scale is pixel position / resolution.
 *  factor is the zoom level.
 */



public class Algebra {

    /**
     * Interpolate a point between center and center + factor
     * @param center The center point
     * @param scale The scale factor (0.0 to 1.0)
     * @param factor The range factor
     * @return The interpolated point
     */

    public static BigDecimal interpolatePoint(BigDecimal center, double scale, BigDecimal factor) {
        BigDecimal start = center.subtract(new BigDecimal("0.5").multiply(factor));
        BigDecimal result = start.add(factor.multiply(new BigDecimal(scale)));
        System.out.println("start: " + start + "\ncenter:" + center + "\nresult:" + result);
        return result;
    }


    public static void main(String[] args) {
        System.out.println("Algebra");
        BigDecimal factor = new BigDecimal("0.0000000000000002");
                                           //"0.00000000000000020"
        BigDecimal a = new BigDecimal("-0.74364388703715153375");
        double scale  = 25./ 64.;
        System.out.println("Ex:" + interpolatePoint(a, scale, factor));
        
        BigDecimal b =new BigDecimal("0.1318259042053397653125");
        scale  = 39./ 64.;
        System.out.println("Ey:" + interpolatePoint(b, scale, factor));
    }

}
