package com.zettix.fractal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;
import java.lang.Math;


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


public class ImageGen {
  private int[][] data;
  private RenderConfig rc;
  private final double PI = 3.14156;
  private final int redshift;
  private final int greenshift;
  private final int blueshift;
  private final double redscale;
  private final double greenscale;
  private final double bluescale;

  public ImageGen(int redshift, int greenshift, int blueshift, double redscale, double greenscale, double bluescale) {
    this.redshift = redshift;
    this.greenshift = greenshift;
    this.blueshift = blueshift;
    this.redscale = redscale;
    this.greenscale = greenscale;
    this.bluescale = bluescale;
  }

  private void downSample(int box_size) {
    int ry = rc.getResY();
    int rx = rc.getResX();
    int dy = rx / box_size;
    int dx = ry / box_size;
    int[][] data2 = new int[dy][dx];
    System.out.println("New size x:" + dx + " y:" + dy);
    
    for (int y = 0; y < dy; y++) {
      for (int x = 0; x < dx; x++) {
        int a = 0;
        for (int yi = 0; yi < box_size; yi++) {
          for (int xi = 0; xi < box_size; xi++) {
            a += data[y * box_size + yi][x * box_size + xi];
          }
        }
        data2[y][x] = a / box_size / box_size;
      }
    }
    data = data2;
    rc.setResX(dx);
    rc.setResY(dy);
  }

  private void boxFilter(int count) {
    int ry = rc.getResY();
    int rx = rc.getResX();
    int[][] data2 = new int[ry][rx];
    while (count > 0) {
      for (int y = 2; y < ry - 2; y++) {
        for (int x = 2; x < rx - 2; x++) {
          int a = 0;
          for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
              a += data[y + dy][x + dx];
            }
          }
          data2[y][x] = a / 9;
        }
      }
      for (int y = 2; y < ry - 2; y++) {
        for (int x = 2; x < rx - 2; x++) {
          data[y][x] = data2[y][x];
        }
      }
      count--;
    }
  }

  public int getColor(int val) {
    int a = 255;
    int stretch =  redshift;
    int stretch2 = greenshift;
    int stretch3 = blueshift;
    double valfactor = (double) (val % stretch) / (double) stretch;
    double valfactor2 = (double) (val % stretch2) / (double) stretch2;
    double valfactor3 = (double) (val % stretch3) / (double) stretch3;
    double s1 = Math.sin( valfactor * PI * 2.0 + redscale);
    double s2 = Math.sin( valfactor2 * PI * 2.0 + 2.0* PI / 3.0 + greenscale);
    double s3 = Math.sin( valfactor3 * PI * 2.0 + PI * 4.0 / 3.0 + bluescale);
    
    int r = (int)((Math.sqrt(s1 * s1)) * 255.0);
    int g = (int)((Math.sqrt(s2 *s2)) * 255.0);
    int b = (int)((Math.sqrt(s3 *s3)) * 255.0);
    if (val % 5 == -10) {
      r /= 2;
      b /= 2;
      g /= 2;
    }
      
    int color = (a << 24) | (r << 16) | (g << 8) | b;
    return color;
  }

  public void ReadImage(String filename) {
    Path filepath = Paths.get(filename);
    try {
        List<String> lines = Files.readAllLines(filepath);
        String s = lines.get(0);
        rc = RenderConfig.fromString(s);
        int xres = rc.getResX();
        int yres = rc.getResY();
        System.out.println("RC:" + rc);
        data = new int[yres][xres];
        //Arrays.fill(data, 0);
        for (int y = 1; y< lines.size(); y++) {
          String[] nums = lines.get(y).split(" ");
          for (int x = 0; x < xres; x++) {
              int val = Integer.parseInt(nums[x]);
              data[y - 1][x] = val;
          }
        }  
    } catch (IOException ex) {
        System.err.println("ERROR: " + ex);
    }
  }


  public void WriteSwatch(String filename) {
    int xres = 2048;
    int yres = 64;
    
    BufferedImage img = new BufferedImage(xres, yres, BufferedImage.TYPE_INT_RGB);
    
    try {
      for (int y = 0; y < yres; y++) {
        System.out.println("y:" + y);
        for (int x = 0; x < xres; x++) {
            img.setRGB(x, y, getColor(x));
        }
      }
      ImageIO.write(img, "png", new File(filename + ".png"));   
    } catch (IOException ex) {
        System.err.println("ERROR: " + ex);
    }
  }

  public void WriteImage(String filename) {
   int xres = rc.getResY();
   int yres = rc.getResY();
    
    BufferedImage img = new BufferedImage(xres, yres, BufferedImage.TYPE_INT_RGB);
    
    try {
      for (int y = 0; y < yres; y++) {
        for (int x = 0; x < xres; x++) {
            img.setRGB(x, y, getColor(data[y][x]));
        }
      }
      ImageIO.write(img, "png", new File(filename + ".png"));   
    } catch (IOException ex) {
        System.err.println("ERROR: " + ex);
    }
  }

  public static void main(String... args) {
        String filename = ""; // = args[0];
        int boxcount = 0;
        int downsample = 0;
        int redshift = 255;
        int greenshift = 255;
        int blueshift = 255;
        double redscale = 1.0;
        double greenscale = 1.0;
        double bluescale = 1.0;

        if (args.length > 0) {
        	for (int idx = 0; idx < args.length; idx+=2) {
        		String key = args[idx];
            String val = "";
            if (args.length > idx + 1) {
        		  val = args[idx + 1];
            }
        		if (key.equals("-x")) {
        			boxcount = Integer.parseInt(val);
            } else if (key.equals("-swatch")) {
              ImageGen ig = new ImageGen(redshift, greenshift, blueshift, redscale, greenscale, bluescale);
              ig.WriteSwatch("swatch-" + val);
        			System.exit(0);
        		} else if (key.equals("-d")) {
        			downsample = Integer.parseInt(val);
        		} else if (key.equals("-r")) {
        			redshift = Integer.parseInt(val);
        		} else if (key.equals("-g")) {
        			greenshift = Integer.parseInt(val);
        		} else if (key.equals("-b")) {
        			blueshift = Integer.parseInt(val);
        		} else if (key.equals("-rs")) {
        			redscale = Double.parseDouble(val);
        		} else if (key.equals("-gs")) {
        			greenscale = Double.parseDouble(val);
        		} else if (key.equals("-bs")) {
        			bluescale = Double.parseDouble(val);
        		} else {
              if (key.startsWith("-")) {
                System.out.println("Unknown option: " + key);
                System.exit(1);
              }
        			System.out.println("Filename: " + key);
              filename = key;
        		}
        	}
        }
        System.out.println("redshift: " + redshift + " greenshift: " + greenshift + " blueshift: " + blueshift);
        System.out.println("redscale: " + redscale + " greenscale: " + greenscale + " bluescale: " + bluescale);
        ImageGen ig = new ImageGen(
          redshift, greenshift, blueshift, redscale, greenscale, bluescale
        );
        ig.ReadImage(filename);
        if (boxcount > 0) {
        	ig.boxFilter(boxcount);
        }
        if (downsample > 0) {
        	ig.downSample(downsample);
        }
        ig.WriteImage(filename);
  }
}
