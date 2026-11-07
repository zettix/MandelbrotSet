package com.zettix.fractal;

import java.lang.StringBuilder;

import java.math.BigDecimal;
import java.math.MathContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.IOException;


class Mandelbrot {
    private RenderConfig rc;
    private BigDecimal minX;
    private BigDecimal minY;
    private BigDecimal far;
    private BigDecimal two;
    private String magX;
    private String magY;
    private int xr;
    private int yr;
    private int maxcount;
    private int[] buff;
    private MathContext mc;
    private  BigDecimal y_inc;
    private  BigDecimal x_inc;
    private boolean quiet;
    private int limit_count;
    private int ycont = 0;

    /*
     <-------- magX / xres --->
     minX <-----  cy, cx -----> maxX

     stepx = magx / xres
     minx = cx - magx / 2
     maxx = cx - magx / 2 + magx = cx + magx / 2
    */

    public Mandelbrot(
        RenderConfig rc
    ) {
        // RenderConfig
        this.rc = rc;

        String center_X = rc.getCenterX(); 
        String center_Y = rc.getCenterY();
        String mag_x = rc.getMagX();
        String mag_y = rc.getMagY();
        int xres = rc.getResX();
        int yres = rc.getResY();
        int max_iterations = rc.getMaxIterations();
        int precision = rc.getPrecision();
        boolean quiet_please = rc.getQuiet();

        mc = new MathContext(precision);
        far = new BigDecimal("4", mc);
        two = new BigDecimal("2", mc);
        magX = mag_x;
        magY = mag_y;
        quiet = quiet_please;

        BigDecimal cx = new BigDecimal(center_X, mc);
        BigDecimal mx = new BigDecimal(mag_x, mc);
        x_inc = mx.divide(new BigDecimal(xres), mc);
        minX = cx.add(mx.divide(two, mc).negate());
        xr = xres;

        BigDecimal cy = new BigDecimal(center_Y, mc);
        BigDecimal my = new BigDecimal(mag_y, mc);
        y_inc = my.divide(new BigDecimal(yres), mc);
        minY = cy.add(my.divide(two, mc).negate());
        yr = yres;

        maxcount = max_iterations;
        Log("" + rc);
        buff = new int[xr];
    }

    private void Log(String msg) {
        if (! quiet) {
          System.out.println(msg);
        }
    }

    private static String timeDeltaToHMS(long delta) {
      delta = delta / 1000;
      long hours = delta / 3600;
      long minutes = (delta % 3600) / 60;
      long seconds = delta % 60;
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void continueRun() {
        String filename = rc.outputFilename();
        Path p = Paths.get(filename);
        try {
            List<String> file_lines = Files.readAllLines(p);
            String configLine = file_lines.get(0);
            RenderConfig lastConfig = RenderConfig.fromString(configLine);
            if (lastConfig.getCenterX().equals(rc.getCenterX()) &&
                lastConfig.getCenterY().equals(rc.getCenterY()) &&
                lastConfig.getMagX().equals(rc.getMagX()) &&
                lastConfig.getMagY().equals(rc.getMagY()) &&
                lastConfig.getResX() == rc.getResX() &&
                lastConfig.getResY() == rc.getResY() &&
                lastConfig.getMaxIterations() == rc.getMaxIterations() &&
                lastConfig.getPrecision() == rc.getPrecision()) {
                    Log("Continuing from previous run");
                    ycont = file_lines.size() - 1;
            } else {
                Log("Configuration mismatch, starting new run");
            }
        } catch (IOException ex) {
            Log("No previous run found, starting new run:" + ex.getMessage());
        }
    }

    public void Render() {
        int poolsize = 5;
        int processors = Runtime.getRuntime().availableProcessors() + 1;
        System.out.println("Processors: " + processors + " poolsize: " + poolsize);

        BigDecimal ypos = (minY.add(y_inc.negate()));
        int pct = 0;
        long now = System.currentTimeMillis();
                String filename = rc.outputFilename();
        Log("Writing to:" + filename);
        Path p = Paths.get(filename);
        continueRun();
        try {
          if (ycont == 0) {
                Files.writeString(p, "" + rc + "\n");
                Log("Preamble done");
            } else {
                Log("Continuing from line " + ycont);
                ypos = ypos.add(y_inc.multiply(new BigDecimal(ycont), mc));
          }

          for (int y = ycont; y < yr; y++) {
              StringBuilder sb = new StringBuilder();
              ypos = ypos.add(y_inc, mc);
              BigDecimal xpos = (minX.add(x_inc.negate()));
              ExecutorService executor = Executors.newFixedThreadPool(poolsize);
              for (int x = 0; x < xr; x++) {
                  xpos = xpos.add(x_inc, mc);
                  Callable<Void> t = new EscapeRunnable(xpos, ypos, far,
                      maxcount, mc, buff, x, y);
                  executor.submit(t);
              }
              executor.shutdown();
              try {
                  executor.awaitTermination(1000l, TimeUnit.SECONDS);
              } catch (InterruptedException e) {
                  System.err.println("Executor interrupted: " + e);
              }
              for (int x = 0; x < xr; x++) {
                  sb.append(buff[x]);
                  sb.append(" ");
              }
              if (! quiet) {
                int newpct = (y * 100) / yr;
                if (newpct != pct) {
                  pct = newpct;
                  long delta = System.currentTimeMillis() - now;
                  float forecast = (float) (delta)  / ( (float) pct / 100f) - delta;
                  long longforecast = (long) forecast;
                  String forecastString = timeDeltaToHMS(longforecast);
                  String deltaString = timeDeltaToHMS(delta);
                  Log("" + pct + " ETA: " + forecastString + " elapsed: " +
                      deltaString + " limit_count:" + limit_count);
                  limit_count = 0;
                }
              }
              sb.append("\n");
              Files.writeString(p, sb.toString(), StandardOpenOption.APPEND);
          }
        } catch (IOException ex) {
            System.err.println("Cannot write " + filename + " " + ex);
        }
    }

    public void Emit() {
        for (int y = 0; y < yr; y++) {
            for (int x = 0; x < xr; x++) {
              int esc = buff[x];
              if (esc > 90) {
                System.out.print("#");
              } else {
                System.out.print(".");
              }
          }
          System.out.println();
       }
    }
}
