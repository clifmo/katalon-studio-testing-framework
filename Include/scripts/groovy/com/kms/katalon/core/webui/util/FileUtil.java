package com.kms.katalon.core.webui.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.assertthat.selenium_shutterbug.core.Capture;
import com.assertthat.selenium_shutterbug.core.Shutterbug;
import com.kms.katalon.core.driver.DriverType;
import com.kms.katalon.core.exception.StepFailedException;
import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.testobject.TestObject;
import com.kms.katalon.core.util.internal.TestOpsUtil;
import com.kms.katalon.core.webui.common.WebUiCommonHelper;
import com.kms.katalon.core.webui.constants.StringConstants;
import com.kms.katalon.core.webui.driver.DriverFactory;
import com.kms.katalon.core.webui.driver.WebUIDriverType;

public class FileUtil {

    private static final String SCREENSHOT_FOLDER = "resources/screen";

    private static final String AUTHENTICATION_FOLDER = "resources/authentication";

    private static final String EXTENSIONS_FOLDER_NAME = "resources/extensions";

    private static final KeywordLogger logger = KeywordLogger.getInstance(FileUtil.class);

    public static String takesScreenshot(String fileName, List<TestObject> hideElements, Color hideColor,
            boolean isTestOpsVisionCheckPoint) throws Exception {
        if (!isTestOpsVisionCheckPoint) {
            takeDefaultScreenshot(fileName);
            return fileName;
        }

        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException(StringConstants.KW_SCREENSHOT_EXCEPTION_FILENAME_NULL_EMPTY);
        }
        String savedFileName = TestOpsUtil.replaceTestOpsVisionFileName(fileName.trim());
        WebDriver driver = DriverFactory.getWebDriver();
        BufferedImage image = takeViewportScreenshot(driver);
        hideElements(image, driver, hideElements, hideColor, getScrollX(driver), getScrollY(driver));
        saveImage(image, savedFileName);
        return TestOpsUtil.getRelativePathForLog(savedFileName);
    }

    public static String takeFullPageScreenshot(String fileName, List<TestObject> ignoredElements,
            List<TestObject> hideElements, Color hideColor, boolean isTestOpsVisionCheckPoint) throws Exception {
        if (isTestOpsVisionCheckPoint && StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException(StringConstants.KW_SCREENSHOT_EXCEPTION_FILENAME_NULL_EMPTY);
        }
        String savedFileName = isTestOpsVisionCheckPoint ? TestOpsUtil.replaceTestOpsVisionFileName(fileName.trim())
                : fileName;
        WebDriver driver = DriverFactory.getWebDriver();
        Map<WebElement, String> states = ignoreElements(driver, ignoredElements);
        BufferedImage image = null;
        DriverType browser = DriverFactory.getExecutedBrowser();
        long baseX = 0;
        if (browser == WebUIDriverType.CHROME_DRIVER || browser == WebUIDriverType.EDGE_CHROMIUM_DRIVER) {
            image = Shutterbug.shootPage(driver, Capture.FULL).getImage();
            long scrollbarSize = getScrollBarSize(driver);
            baseX -= scrollbarSize;
        } else {
            image = Shutterbug.shootPage(driver, Capture.FULL_SCROLL).getImage();
        }

        restoreElements(driver, states);
        hideElements(image, driver, hideElements, hideColor, baseX, 0);
        saveImage(image, savedFileName);
        return TestOpsUtil.getRelativePathForLog(savedFileName);
    }

    public static String takeElementScreenshot(String fileName, TestObject element, List<TestObject> hideElements,
            Color hideColor, boolean isTestOpsVisionCheckPoint) throws Exception {
        if (isTestOpsVisionCheckPoint && StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException(StringConstants.KW_SCREENSHOT_EXCEPTION_FILENAME_NULL_EMPTY);
        }
        if (element == null) {
            throw new IllegalArgumentException(StringConstants.KW_SCREENSHOT_EXCEPTION_ELEMENT_NULL);
        }

        String savedFileName = isTestOpsVisionCheckPoint ? TestOpsUtil.replaceTestOpsVisionFileName(fileName.trim())
                : fileName;
        WebDriver driver = DriverFactory.getWebDriver();
        WebElement capturedElement = WebUiCommonHelper.findWebElement(element, 0);
        BufferedImage image = Shutterbug.shootElement(driver, capturedElement).getImage();
        Rectangle baseRect = capturedElement.getRect();
        hideElements(image, driver, hideElements, hideColor, baseRect.x, baseRect.y);
        saveImage(image, savedFileName);
        return TestOpsUtil.getRelativePathForLog(savedFileName);
    }

    public static String takeAreaScreenshot(String fileName, Rectangle rect, List<TestObject> hideElements,
            Color hideColor, boolean isTestOpsVisionCheckPoint) throws IOException {
        if (isTestOpsVisionCheckPoint && StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException(StringConstants.KW_SCREENSHOT_EXCEPTION_FILENAME_NULL_EMPTY);
        }
        if (rect == null) {
            throw new IllegalArgumentException(StringConstants.KW_SCREENSHOT_EXCEPTION_AREA_NULL);
        }

        String savedFileName = isTestOpsVisionCheckPoint ? TestOpsUtil.replaceTestOpsVisionFileName(fileName.trim())
                : fileName;
        WebDriver driver = DriverFactory.getWebDriver();
        double devicePixelRatio = WebUiCommonHelper.getDevicePixelRatio(driver);
        Rectangle actualRect = getActualRectangle(rect, 0, 0, devicePixelRatio);
        BufferedImage image = Shutterbug.shootPage(driver, Capture.VIEWPORT).getImage();
        if ((actualRect.x + actualRect.width) > image.getWidth()
                || (actualRect.y + actualRect.height) > image.getHeight()) {
            throw new IllegalArgumentException(StringConstants.KW_SCREENSHOT_EXCEPTION_AREA_LARGER);
        }
        image = hideElements(image, driver, hideElements, hideColor, getScrollX(driver), getScrollY(driver));
        saveImage(image.getSubimage(actualRect.x, actualRect.y, actualRect.width, actualRect.height), savedFileName);
        return savedFileName;
    }

    private static Map<WebElement, String> ignoreElements(WebDriver driver, List<TestObject> testObjects)
            throws Exception {
        if (testObjects == null || driver == null) {
            return null;
        }

        Map<WebElement, String> preState = new HashMap<>();
        JavascriptExecutor jsx = (JavascriptExecutor) driver;
        int counter = 0;
        for (TestObject to : testObjects) {
            try {
                WebElement element = WebUiCommonHelper.findWebElement(to, 0);
                String state = jsx.executeScript("return arguments[0].style.visibility", element).toString();
                preState.put(element, state);
                jsx.executeScript("arguments[0].style.visibility = 'hidden'", element);
                ++counter;
            } catch (Exception e) {
                logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SCREENSHOT_FULLPAGE_FAIL_HIDE_OBJECT,
                        to.getObjectId()));
            }
        }

        logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SCREENSHOT_FULLPAGE_HIDDEN_COUNTER, counter));
        return preState;
    }

    private static BufferedImage hideElements(BufferedImage screenshot, WebDriver driver, List<TestObject> testObjects,
            Color hideColor, long baseX, long baseY) {
        if (testObjects == null || testObjects.isEmpty()) {
            logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SCREENSHOT_FULLPAGE_HIDDEN_COUNTER, 0));
            return screenshot;
        }
        Objects.requireNonNull(screenshot);
        Objects.requireNonNull(driver);
        Objects.requireNonNull(testObjects);
        int counter = 0;
        if (hideColor == null) {
            hideColor = Color.GRAY;
        }
        Graphics2D g = screenshot.createGraphics();
        g.setColor(hideColor);
        double pixelRatio = WebUiCommonHelper.getDevicePixelRatio(driver);
        for (TestObject to : testObjects) {
            try {
                WebElement element = WebUiCommonHelper.findWebElement(to, 0);
                Rectangle rect = getActualRectangle(element.getRect(), baseX, baseY, pixelRatio);
                if (rect.x < 0 || rect.y < 0 || rect.x > screenshot.getWidth() || rect.y > screenshot.getHeight()) {
                    logger.logInfo(MessageFormat.format(
                            StringConstants.KW_LOG_INFO_SCREENSHOT_FULLPAGE_FAIL_HIDE_OBJECT_OUT_OF_IMAGE,
                            to.getObjectId()));
                    continue;
                }
                g.fillRect(rect.x, rect.y, rect.width, rect.height);
                ++counter;
            } catch (Exception e) {
                logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SCREENSHOT_FULLPAGE_FAIL_HIDE_OBJECT,
                        to.getObjectId()));
            }

        }

        g.dispose();
        logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SCREENSHOT_FULLPAGE_HIDDEN_COUNTER, counter));
        return screenshot;
    }

    private static Rectangle getActualRectangle(Rectangle baseRect, long baseX, long baseY, double pixelRatio) {
        Objects.requireNonNull(baseRect);
        int actualRectX = (int) ((baseRect.x - baseX) * pixelRatio);
        int actualRectY = (int) ((baseRect.y - baseY) * pixelRatio);
        int actualRectW = (int) (baseRect.width * pixelRatio);
        int actualRectH = (int) (baseRect.height * pixelRatio);
        return new Rectangle(actualRectX, actualRectY, actualRectH, actualRectW);
    }

    private static void restoreElements(WebDriver driver, Map<WebElement, String> states) throws Exception {
        if (states == null || driver == null) {
            return;
        }

        JavascriptExecutor jsx = (JavascriptExecutor) driver;
        for (WebElement e : states.keySet()) {
            jsx.executeScript("arguments[0].style.visibility = '" + states.get(e) + "'", e);
        }
    }

    public static File extractScreenFiles() throws Exception {
        String path = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path = URLDecoder.decode(path, "utf-8");
        File jarFile = new File(path);
        if (jarFile.isFile()) {
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if (name.startsWith(SCREENSHOT_FOLDER) && name.endsWith(".png")) {
                    String mappingFileName = name.replace(SCREENSHOT_FOLDER + "/", "");
                    File tmpFile = new File(System.getProperty("java.io.tmpdir") + mappingFileName);
                    if (tmpFile.exists()) {
                        tmpFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(tmpFile);
                    IOUtils.copy(jar.getInputStream(jarEntry), fos);
                    fos.flush();
                    fos.close();
                }
            }
            jar.close();
            return new File(System.getProperty("java.io.tmpdir"));
        } else { // Run with IDE
            File folder = new File(path + "../" + SCREENSHOT_FOLDER);
            return folder;
        }
    }

    public static File getAuthenticationDirectory() throws IOException {
        String path = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path = URLDecoder.decode(path, "utf-8");
        File jarFile = new File(path);
        if (jarFile.isFile()) {
            String kmsIePath = jarFile.getParentFile().getParentFile().getAbsolutePath() + "/configuration/"
                    + AUTHENTICATION_FOLDER;
            return new File(kmsIePath);
        } else { // Run with IDE
            File folder = new File(path + "../" + AUTHENTICATION_FOLDER);
            return folder;
        }
    }

    /**
     * Return a file representing directory resources/extensions
     * 
     * @return {@link File}
     * @throws IOException
     */
    public static File getExtensionsDirectory() throws IOException {
        String path = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path = URLDecoder.decode(path, "utf-8");
        File jarFile = new File(path);
        if (jarFile.isFile()) {
            String kmsIePath = jarFile.getParentFile().getParentFile().getAbsolutePath() + "/configuration/"
                    + EXTENSIONS_FOLDER_NAME;
            return new File(kmsIePath);
        } else { // Run with IDE
            File folder = new File(path + ".." + File.separator + EXTENSIONS_FOLDER_NAME);
            return folder;
        }
    }

    public static String getRelativePath(String path, String baseDir) {
        String relativePath = new File(baseDir).toPath().relativize(new File(path).toPath()).toString();
        return FilenameUtils.separatorsToUnix(relativePath);
    }

    public static boolean isInBaseFolder(String absolutePath, String absoluteBaseDir) {
        File file = new File(absolutePath);
        File baseDir = new File(absoluteBaseDir);
        return file.getAbsolutePath().startsWith(baseDir.getAbsolutePath());
    }

    private static BufferedImage takeViewportScreenshot(WebDriver driver) {
        BufferedImage image = Shutterbug.shootPage(driver, Capture.VIEWPORT).getImage();
        if (DriverFactory.getExecutedBrowser() == WebUIDriverType.IOS_DRIVER) {
            return removeBrowserAndOSStatusBar(driver, image);
        }
        return image;
    }

    private static BufferedImage removeBrowserAndOSStatusBar(WebDriver driver, BufferedImage image) {
        double devicePixelRatio = WebUiCommonHelper.getDevicePixelRatio(driver);
        int viewportWidth = (int) (WebUiCommonHelper.getViewportWidth(driver) * devicePixelRatio);
        int viewportHeight = (int) (WebUiCommonHelper.getViewportHeight(driver) * devicePixelRatio);
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        if (viewportHeight != image.getHeight() || viewportWidth != image.getWidth()) {
            return image.getSubimage(imageWidth - viewportWidth, imageHeight - viewportHeight, viewportWidth,
                    viewportHeight);
        }
        return image;
    }

    public static void saveImage(BufferedImage image, String fileName) throws IOException, SecurityException {
        File file = new File(fileName);
        TestOpsUtil.ensureDirectory(file, true);
        ImageIO.write(image, TestOpsUtil.DEFAULT_IMAGE_EXTENSION, file);
    }

    private static void takeDefaultScreenshot(String fileName)
            throws WebDriverException, StepFailedException, IOException {
        FileUtils.copyFile(((TakesScreenshot) DriverFactory.getWebDriver()).getScreenshotAs(OutputType.FILE),
                new File(fileName), false);
    }

    private static long getScrollX(WebDriver driver) {
        if (driver == null) {
            return 0;
        }
        try {
            return (Long) ((JavascriptExecutor) driver).executeScript("return (window.pageXOffset !== undefined)\n"
                    + "  ? window.pageXOffset\n"
                    + "  : (document.documentElement || document.body.parentNode || document.body).scrollLeft;");
        } catch (NullPointerException | WebDriverException e) {
            return 0;
        }
    }

    private static long getScrollY(WebDriver driver) {
        if (driver == null) {
            return 0;
        }
        try {
            return (Long) ((JavascriptExecutor) driver)
                    .executeScript("return (window.pageYOffset !== undefined)\n" + "  ? window.pageYOffset\n"
                            + "  : (document.documentElement || document.body.parentNode || document.body).scrollTop;");
        } catch (NullPointerException | WebDriverException e) {
            return 0;
        }
    }

    private static long getScrollBarSize(WebDriver driver) {
        if (driver == null) {
            return 0;
        }
        try {
            return (Long) ((JavascriptExecutor) driver)
                    .executeScript("return (window.innerWidth - document.body.clientWidth)");
        } catch (NullPointerException | WebDriverException e) {
            return 0;
        }
    }

}
