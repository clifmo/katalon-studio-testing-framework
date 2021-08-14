package com.kms.katalon.core.webui.helper.screenshot;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.openqa.selenium.WebDriver;

import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.webui.common.ImageTextProperties;
import com.kms.katalon.core.webui.constants.CoreWebuiMessageConstants;
import com.kms.katalon.core.webui.driver.DriverFactory;
import com.kms.katalon.core.webui.util.FileUtil;
import com.kms.katalon.core.webui.util.ParseUtil;
import com.kms.katalon.core.webui.util.StringUtils;

public class ScreenshotTextHelper {

    private static final KeywordLogger logger = KeywordLogger.getInstance(ScreenshotTextHelper.class);

    private static final int MAX_FONT_SIZE = 50;

    private static final int MAX_CHARACTERS = 100;

    private static final String UPDATED_TEXT_WIDTH = "newBufferedWidth";

    private static final String UPDATED_TEXT_HEIGHT = "newBufferedHeight";

    private static final String SPLITTED_TEXT = "splittedText";

    private static final String IMAGE_X = "imageX";

    private static final String IMAGE_Y = "imageY";

    private static final String TEXT_X = "textX";

    private static final String TEXT_Y = "textY";

    /**
     * To add text to given screenshot.
     * 
     * @param pathToScreenshot path to captured screenshot needed to add text
     * @param screenshotOptions
     * @return true if add text to screenshot successfully and vice versa.
     * @throws Exception
     */
    public static BufferedImage addTextToScreenShot(String pathToScreenshot, Map<String, Object> screenshotOptions) {
        try {
            BufferedImage image = ImageIO.read(new File(pathToScreenshot));
            ImageTextProperties options = parseMapToImageTextObject(screenshotOptions);
            if (options == null || !checkValidImageTextProperties(options)) {
                return null;
            }
            image = addTextToBufferedImage(image, options);
            FileUtil.saveImage(image, pathToScreenshot);
            return image;
        } catch (Exception exception) {
            logger.logWarning(MessageFormat.format(CoreWebuiMessageConstants.KW_SCREENSHOT_EXCEPTION_WHILE_ADDING_TEXT, exception.getMessage()));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static BufferedImage addTextToBufferedImage(BufferedImage screenshot,
            ImageTextProperties screenshotOptions) {
        WebDriver driver = DriverFactory.getWebDriver();
        Objects.requireNonNull(screenshot);
        Objects.requireNonNull(driver);

        Graphics2D screenshotGrahics = (Graphics2D) screenshot.getGraphics();

        Font font = new Font(screenshotOptions.getFont(), screenshotOptions.getFontStyle(),
                screenshotOptions.getFontSize());
        FontMetrics fontMetrics = screenshotGrahics.getFontMetrics(font);

        // Calculate Buffered Metrics
        Map<String, Object> bufferedMetrics = calculateMetricsForAddingText(screenshot, fontMetrics,
                screenshotOptions.getText(), screenshotOptions.getX(), screenshotOptions.getY());
        BufferedImage screenshotWithText = screenshot;
        try {
            screenshotWithText = new BufferedImage((int) bufferedMetrics.get(UPDATED_TEXT_WIDTH),
                    (int) bufferedMetrics.get(UPDATED_TEXT_HEIGHT), BufferedImage.TYPE_INT_ARGB);
        } catch (OutOfMemoryError | IllegalArgumentException exception) {
            logger.logWarning(CoreWebuiMessageConstants.MSG_ERR_SCREENSHOT_EXCEPTION_IMAGE_TOO_LARGE);
            return screenshot;
        }
        Graphics2D screenshotWithTextGrahics = screenshotWithText.createGraphics();
        Color oldColor = screenshotWithTextGrahics.getColor();
        // Fill background
        screenshotWithTextGrahics.setPaint(Color.WHITE);
        screenshotWithTextGrahics.fillRect(0, 0, (int) bufferedMetrics.get(UPDATED_TEXT_WIDTH),
                (int) bufferedMetrics.get(UPDATED_TEXT_HEIGHT));
        // Draw image
        screenshotWithTextGrahics.setColor(oldColor);
        screenshotWithTextGrahics.drawImage(screenshot, null, (int) bufferedMetrics.get(IMAGE_X),
                (int) bufferedMetrics.get(IMAGE_Y));
        // Draw text
        screenshotWithTextGrahics.setFont(font);
        screenshotWithTextGrahics.setColor(screenshotOptions.getFontColor());
        StringUtils.drawSplittedText((ArrayList<String>) bufferedMetrics.get(SPLITTED_TEXT), fontMetrics,
                screenshotWithTextGrahics, (int) bufferedMetrics.get(TEXT_X), (int) bufferedMetrics.get(TEXT_Y));
        screenshotWithTextGrahics.dispose();
        return screenshotWithText;
    }

    private static Map<String, Object> calculateMetricsForAddingText(BufferedImage screenshot, FontMetrics fontMetrics,
            String text, int expectedTextX, int expectedTextY) {
        Map<String, Object> metrics = new HashMap<String, Object>();
        // Get Image Size
        int screenshotWidth = screenshot.getWidth();
        int screenshotHeight = screenshot.getHeight();
        // Save Image Start Point
        int imageX = expectedTextX >= 0 ? 0 : -expectedTextX;
        metrics.put(IMAGE_X, imageX);
        int imageY = expectedTextY >= 0 ? 0 : -expectedTextY;
        metrics.put(IMAGE_Y, imageY);
        // Save Text Start Point
        metrics.put(TEXT_X, expectedTextX <= 0 ? 0 : expectedTextX);
        metrics.put(TEXT_Y, (expectedTextY <= 0 ? 0 : expectedTextY) + fontMetrics.getAscent());
        // Calculate Text Size
        int textWidth = 0;
        if (expectedTextX > 0 && fontMetrics.stringWidth("AAAAA") > (screenshotWidth - expectedTextX)) {
            textWidth = fontMetrics.stringWidth(text) < 1000 ? fontMetrics.stringWidth(text) : 1000;
        } else {
            textWidth = screenshotWidth - expectedTextX;
        }
        int textHeight = 0;
        List<String> splittedText = StringUtils.wrap(text, fontMetrics, textWidth);
        metrics.put(SPLITTED_TEXT, splittedText);
        textHeight = splittedText.size() * (fontMetrics.getHeight() + fontMetrics.getAscent());
        // Save new BufferedImage (width=(maxEnd-minStart); height=(maxEnd-minStart))
        int newBufferedWidth = 0;
        int moduloBetweenTextAndScreenshotWidth = ((textWidth - screenshotWidth) + expectedTextX);
        if (expectedTextX >= 0) {
            if (moduloBetweenTextAndScreenshotWidth >= 0) {
                newBufferedWidth = expectedTextX + textWidth;
            } else {
                newBufferedWidth = screenshotWidth;
            }
        } else {
            if (moduloBetweenTextAndScreenshotWidth > 0) {
                newBufferedWidth = textWidth;
            } else {
                newBufferedWidth = screenshotWidth - expectedTextX;
            }
        }
        metrics.put(UPDATED_TEXT_WIDTH, newBufferedWidth);
        int newBufferedHeight = 0;
        int moduloBetweenTextAndScreenshotHeight = ((textHeight - screenshotHeight) + expectedTextY);
        if (expectedTextY >= 0) {
            if (moduloBetweenTextAndScreenshotHeight >= 0) {
                newBufferedHeight = expectedTextY + textHeight;
            } else {
                newBufferedHeight = screenshotHeight;
            }
        } else {
            if (moduloBetweenTextAndScreenshotHeight > 0) {
                newBufferedHeight = textHeight;
            } else {
                newBufferedHeight = screenshotHeight - expectedTextY;
            }
        }
        metrics.put(UPDATED_TEXT_HEIGHT, newBufferedHeight);

        return metrics;
    }

    private static boolean checkValidImageTextProperties(ImageTextProperties screenshotOptions) {
        String screenshotText = screenshotOptions.getText();
        if ( screenshotText == null || screenshotText.isEmpty()) {
            throw new IllegalArgumentException(
                    MessageFormat.format(CoreWebuiMessageConstants.MSG_ERR_PROPERTY_MUST_NOT_BE_NULL, "text"));
        }
        if (screenshotOptions.getFontSize() > MAX_FONT_SIZE) {
            throw new IllegalArgumentException(CoreWebuiMessageConstants.MSG_WARN_FONT_SIZE_TOO_LARGE);
        }
        if (screenshotText.length() > MAX_CHARACTERS) {
            logger.logWarning(
                    MessageFormat.format(CoreWebuiMessageConstants.MSG_ERR_IMAGE_TEXT_TOO_LONG, MAX_CHARACTERS));
            screenshotOptions.setText(screenshotText.substring(0, MAX_CHARACTERS));
        }
        return checkValidFont(screenshotOptions.getFont());
    }

    public static ImageTextProperties parseMapToImageTextObject(Map<String, Object> properties) {
        ImageTextProperties imageTextProperties = new ImageTextProperties();
        for (String option : properties.keySet()) {
            Object value = properties.get(option);
            try {
                switch (option) {
                    case "text":
                        imageTextProperties.setText(ParseUtil.parseString(value));
                        break;
                    case "x":
                        if (value != null) {
                            imageTextProperties.setX(ParseUtil.parseInt(value));
                        }
                        break;
                    case "y":
                        if (value != null) {
                            imageTextProperties.setY(ParseUtil.parseInt(value));
                        }
                        break;
                    case "font":
                        if (value != null) {
                            imageTextProperties.setFont((String) value);
                        }
                        break;
                    case "fontsize":
                    case "fontSize":
                        if (value != null) {
                            imageTextProperties.setFontSize(ParseUtil.parseInt(value));
                        }
                        break;
                    case "fontcolor":
                    case "fontColor":
                        if (value != null) {
                            imageTextProperties.setFontColor(ParseUtil.parseColor(value));
                        }
                        break;
                    case "fontstyle":
                    case "fontStyle":
                        if (value != null) {
                            imageTextProperties.setFontStyle(ParseUtil.parseFontStyle(value));
                        }
                        break;
                    default:
                        throw new IllegalArgumentException(
                                MessageFormat.format(CoreWebuiMessageConstants.MSG_ERR_INVALID_PROPERTY, option));
                }
            } catch (ClassCastException | IllegalArgumentException exception) {
                logger.logWarning(MessageFormat.format(CoreWebuiMessageConstants.MSG_WARN_INVALID_OPTION, option,
                        exception.getMessage()));
                return null;
            }
        }
        return imageTextProperties;
    }

    private static boolean checkValidFont(String font) {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] allFontFamilyNames = graphicsEnvironment.getAvailableFontFamilyNames();
        List<String> fontList = Arrays.asList(allFontFamilyNames);
        if (font != null && fontList.contains(font)) {
            return true;
        }
        throw new IllegalArgumentException(MessageFormat.format(CoreWebuiMessageConstants.MSG_ERR_INVALID_FONT, font,
                org.apache.commons.lang3.StringUtils.join(fontList, ",")));
    }
}
