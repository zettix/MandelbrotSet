package com.zettix.fractal;


public class App {

    public static void main(String[] args) throws Exception {
        RenderConfig rc = RenderConfig.parse(args);
        Mandelbrot mb = new Mandelbrot(rc);
        mb.Render();
    }
}
