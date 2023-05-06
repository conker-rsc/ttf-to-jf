import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFrame;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FontConverter {
  static Frame frame;
  static int gameFontSize = 0;
  static byte[] fontData;
  static byte[] jfFont = new byte[100000];
  static boolean flag = false;

  static String name;
  static int size;
  static byte style;
  static String inPath;
  static String outPath;

  public static void main(String[] args) {
    CommandLineParser parser = new DefaultParser();

    Options options = new Options();
    options.addRequiredOption("name", "font-name", true, "font name");
    options.addRequiredOption("size", "font-size", true, "font size");
    options.addRequiredOption("style", "font-style", true, "font style");
    options.addRequiredOption("in", "input-file", true, "path to ttf file");
    options.addRequiredOption("out", "output-path", true, "output directory");

    HelpFormatter formatter = new HelpFormatter();

    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (MissingOptionException moe) {
      System.out.println("Please provide all required options" + System.lineSeparator());
      formatter.printHelp("ttf-to-jf", options);
      System.exit(1);
    } catch (ParseException pe) {
      formatter.printHelp("ttf-to-jf", options);
      System.out.println();
      throw new RuntimeException("Unexpected error occurred", pe);
    }

    if (null == line) {
      throw new RuntimeException("Unexpected error");
    }

    try {
      frame = new JFrame();
      frame.setVisible(true);
      frame.toFront();
      frame.getGraphics();

      name = line.getOptionValue("name");
      size = Integer.parseInt(line.getOptionValue("size"));
      String styleArg = line.getOptionValue("style");

      if (styleArg.equals("p")) {
        style = Font.PLAIN;
      } else if (styleArg.equals("b")) {
        style = Font.BOLD;
      } else {
        throw new RuntimeException("Style must be provided as \"p\" or \"b\"");
      }

      inPath = line.getOptionValue("in");
      outPath = line.getOptionValue("out");

      if (!outPath.endsWith("/")) {
        outPath += "/";
      }

      createFont();

      String finalPath = outPath + name + ".jf";

      try {
        Files.write(new File(finalPath).toPath(), fontData);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      System.out.println("Successfully created font: " + finalPath);

      frame.dispose();

      System.exit(0);
    } catch (Exception e) {
      frame.dispose();
      System.out.println("Unexpected error occurred" + System.lineSeparator());
      e.printStackTrace();
      System.exit(1);
    }
  }

  static void createFont() {
    // TODO: Determine what these are used for... figure out by testing various fonts
    //  the characters below would probably break the font somehow if this is truly required
    //  boolean fontF = false;
    //  boolean fontD = false;
    //  (see 233 deob for implementation)
    //  likely that "fontD" was used to customize specific char widths for
    //  a non-monospace font

    Font parentFont;
    Font font;
    InputStream is;
    try {
      is = Files.newInputStream(Paths.get(inPath));
      parentFont = Font.createFont(Font.TRUETYPE_FONT, is);
      font = parentFont.deriveFont(style, (float) size);
    } catch (IOException | FontFormatException e) {
      throw new RuntimeException(e);
    }

    FontMetrics metrics = frame.getFontMetrics(font);
    gameFontSize = 855;
    String characters =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";

    for (int charIndex = 0; charIndex < 95; ++charIndex) {
      if (!readFont(charIndex, font, characters.charAt(charIndex), metrics)) {
        return;
      }
    }

    fontData = new byte[gameFontSize];

    System.arraycopy(jfFont, 0, fontData, 0, gameFontSize);
  }

  private static boolean readFont(int charIndex, Font font, char character, FontMetrics metrics) {
    int charWidth = metrics.charWidth(character);

    int maxAscent = metrics.getMaxAscent();
    int heightCalculated = metrics.getMaxAscent() + metrics.getMaxDescent();
    int height = metrics.getHeight();
    Image img = frame.createImage(charWidth, heightCalculated);
    if (img == null) {
      return false;
    } else {
      Graphics graphics = img.getGraphics();
      graphics.setColor(Color.black);
      graphics.fillRect(0, 0, charWidth, heightCalculated);
      graphics.setColor(Color.white);
      graphics.setFont(font);
      graphics.drawString(String.valueOf(character), 0, maxAscent);

      int[] charPixels = new int[charWidth * heightCalculated];
      PixelGrabber pixelGrabber =
          new PixelGrabber(img, 0, 0, charWidth, heightCalculated, charPixels, 0, charWidth);

      try {
        pixelGrabber.grabPixels();
      } catch (InterruptedException var29) {
        return false;
      }

      img.flush();
      int right = 0;
      int bottom = 0;
      int var19 = charWidth;

      label213:
      for (int y = 0; y < heightCalculated; ++y) {
        for (int x = 0; charWidth > x; ++x) {
          int pixel = charPixels[y * charWidth + x];
          if ((0xffffff & pixel) != 0) {
            bottom = y;
            break label213;
          }
        }
      }

      int heightCalculated2 = heightCalculated;

      int var25;
      int var24;
      label199:
      for (int x = 0; charWidth > x; ++x) {
        for (int y = 0; y < heightCalculated; ++y) {
          int pixel = charPixels[y * charWidth + x];
          if ((pixel & 0xffffff) != 0) {
            right = x;
            break label199;
          }
        }
      }

      int var26;
      label185:
      for (var24 = heightCalculated - 1; var24 >= 0; --var24) {
        for (var25 = 0; charWidth > var25; ++var25) {
          var26 = charPixels[charWidth * var24 + var25];
          if ((var26 & 0xffffff) != 0) {
            heightCalculated2 = 1 + var24;
            break label185;
          }
        }
      }

      int var27;
      label171:
      for (var25 = charWidth - 1; var25 >= 0; --var25) {
        for (var26 = 0; var26 < heightCalculated; ++var26) {
          var27 = charPixels[charWidth * var26 + var25];
          if ((var27 & 0xffffff) != 0) {
            var19 = 1 + var25;
            break label171;
          }
        }
      }

      jfFont[9 * charIndex] = (byte) (gameFontSize >> 14);
      jfFont[charIndex * 9 + 1] = (byte) (127 & gameFontSize >> 7);
      jfFont[charIndex * 9 + 2] = (byte) (127 & gameFontSize);
      jfFont[9 * charIndex + 3] = (byte) (-right + var19);
      jfFont[4 + 9 * charIndex] = (byte) (heightCalculated2 - bottom);
      jfFont[9 * charIndex + 5] = (byte) right;
      jfFont[9 * charIndex + 6] = (byte) (maxAscent - bottom);
      jfFont[charIndex * 9 + 7] = (byte) charWidth;
      jfFont[8 + 9 * charIndex] = (byte) height;

      for (int y = bottom; heightCalculated2 > y; ++y) {
        for (int x = right; var19 > x; ++x) {
          int var28 = 255 & charPixels[y * charWidth + x];
          if (var28 > 30 && var28 < 230) {
            flag = true;
          }

          jfFont[gameFontSize++] = (byte) var28;
        }
      }

      return true;
    }
  }
}
