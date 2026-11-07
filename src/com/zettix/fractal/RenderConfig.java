package com.zettix.fractal;

import java.util.List;
import java.util.ArrayList;
 
class RenderConfig {
  private   int frame = 0;
  private   String cx = "0.0";
  private   String cy = "0.0";
  private   String mx = "4";
  private   String my = "4";
  private   int xres = 100;
  private   int yres = 100;
  private   int max_iterations = 100;
  private   int precision = 100;
  private   boolean quiet = false;
  private static String sep = "\t";

  public RenderConfig() {};

  public RenderConfig setCenterX(String c_x) { cx = c_x; return this; }
  public RenderConfig setCenterY(String c_y) { cy = c_y;  return this;}
  public RenderConfig setMagX(String m_x) { mx = m_x; return this; }
  public RenderConfig setMagY(String m_y) { my = m_y; return this; }
  public RenderConfig setResX(int r_x) { xres = r_x; return this; }
  public RenderConfig setResY(int r_y) { yres = r_y; return this; }
  public RenderConfig setMaxIterations(int mi) { max_iterations = mi; return this; }
  public RenderConfig setPrecision(int p) { precision = p; return this; }
  public RenderConfig setFrame(int f) { frame = f; return this; }
  public RenderConfig setQuiet(boolean q) { quiet = q; return this; }
  public String getCenterX() { return cx; }
  public String getCenterY() { return cy; }
  public String getMagX() { return mx; }
  public String getMagY() { return my; }
  public int getResX() { return xres; }
  public int getResY() { return yres; }
  public int getMaxIterations() { return max_iterations; }
  public int getPrecision() { return precision; }
  public int getFrame() { return frame; }
  public boolean getQuiet() { return quiet; }

  public String outputFilename() {
    String r = String.format("f%010d.dat", frame);
    return r;
  }

  private static void Help() {
    System.out.println("Usage:");
    System.out.println("-cx /center x/");
    System.out.println("-cy /center y/");
    System.out.println("-mx /mag x/");
    System.out.println("-my /mag y/");
    System.out.println("-rx /resolution x/");
    System.out.println("-ry /resolution y/");
    System.out.println("-i /max iterations/");
    System.out.println("-q quiet");
  }

  public static RenderConfig parse(String[] args) {
        String re =  "-.743643887037151";
        String im =  ".131825904205330";
        RenderConfig rc = new RenderConfig();
        rc.setCenterX(re).
        setCenterY(im).
        setMagX(".004").
        setMagY(".004").
        setResX(100).
        setResY(100).
        setMaxIterations(1000).
        setPrecision(100).
        setFrame(0).
        setQuiet(false);
        int idx = 0;

        try {
          while (idx < args.length) {
              String arg = args[idx];
              if (arg.equals("-cx")) {
                  idx++;
                  rc.setCenterX(args[idx]);
              }
              if (arg.equals("-cy")) {
                  idx++;
                  rc.setCenterY(args[idx]);
              }
              if (arg.equals("-mx")) {
                  idx++;
                  rc.setMagX(args[idx]);
              }
              if (arg.equals("-my")) {
                  idx++;
                  rc.setMagY(args[idx]);
              }
              if (arg.equals("-rx")) {
                  idx++;
                  rc.setResX(Integer.parseInt(args[idx]));
              }
              if (arg.equals("-ry")) {
                  idx++;
                  rc.setResY(Integer.parseInt(args[idx]));
              }
              if (arg.equals("-i")) {
                  idx++;
                  rc.setMaxIterations(Integer.parseInt(args[idx]));
              }
              if (arg.equals("-p")) {
                  idx++;
                  rc.setPrecision(Integer.parseInt(args[idx]));
              }
              if (arg.equals("-f")) {
                  idx++;
                  rc.setFrame(Integer.parseInt(args[idx]));
              }
              if (arg.equals("-q")) {
                  rc.setQuiet(true);
              }
              idx++;
           }
        } catch (Exception ex) {
            Help();
        }
        return rc;
    }

    @Override
    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("frame " + frame  + sep );
      sb.append("cx " + cx  + sep);
      sb.append("cy " + cy  + sep);
      sb.append("mx " + mx  + sep);
      sb.append("my " + my  + sep);
      sb.append("xres " + xres  + sep);
      sb.append("yres " + yres  + sep);
      sb.append("max_iterations " + max_iterations  + sep);
      sb.append("precision " + precision  + sep);
      sb.append("quiet " + quiet);
      return sb.toString();
    }

    //frame 0| cx -.743643887037151| cy .131825904205330| mx 0.00000002| my 0.00000002| xres 100| yres 100| max_iterations 10000| precision 100| quiet false
    public static RenderConfig fromString(String p) { 
      List<String> pargs = new ArrayList<>();
      String[] lp = p.split(sep);
      int idx = 0;
      while (idx < lp.length) {
          String ss = lp[idx];
          String[] kv = ss.split(" ");
          String key = kv[0].trim();
          String val = kv[1].trim();
          if (key.equals("frame")) {
            pargs.add("-f");
          } else if (key.equals("xres")) {
            pargs.add("-rx");
          } else if (key.equals("yres")) {
            pargs.add("-ry");
          } else if (key.equals("max_iterations")) {
            pargs.add("-i");
          } else if (key.equals("quiet")) {
             idx++;
             continue;
          } else {
            pargs.add("-" + key);
          } 
          pargs.add(val);
          idx++;
      }
      String[] vargs = new String[pargs.size()];
      for (int x=0; x < pargs.size(); x++) {
          vargs[x] = pargs.get(x);
      }
      return parse(vargs);
    }
}
