package com.kms.katalon.core.mobile.helper;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;

import org.openqa.selenium.WebElement;

import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.mobile.constants.StringConstants;
import com.kms.katalon.core.mobile.driver.AppiumSessionCollector;
import com.kms.katalon.core.mobile.exception.MobileException;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;

public final class IOSHelper {

    private IOSHelper() {
    }

    private static final KeywordLogger logger = KeywordLogger.getInstance(IOSHelper.class);

    public static int getStatusBarHeight(AppiumDriver<? extends WebElement> driver, DevicePixelRatio scaleFactor) {
        int statusBarHeight = 0;
        IOSDriver<? extends WebElement> iosDriver = (IOSDriver<? extends WebElement>) driver;
        try {
            statusBarHeight = getStatusBarHeightByScreenshot(iosDriver);
            return (int) (statusBarHeight / scaleFactor.ratioY);
        } catch (Exception ignored) {
            logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_FAILED_GET_OS_STATUSBAR, "Screenshot"));
        }
        logger.logWarning(StringConstants.KW_LOG_FAILED_GET_STATUSBAR);
        return statusBarHeight;
    }

    private static int getStatusBarHeightByScreenshot(IOSDriver<? extends WebElement> driver) throws MobileException {
        BufferedImage screenshot = MobileScreenCaptor.takeScreenshot(driver);
        BufferedImage viewportScreenshot = MobileScreenCaptor.takeViewportScreenshot(driver);
        if (screenshot.getHeight() == viewportScreenshot.getHeight()) {
            throw new MobileException(StringConstants.KW_MSG_SCREENSHOT_STATUSBAR_INFO_FAIL);
        }
        return screenshot.getHeight() - viewportScreenshot.getHeight();
    }

    public static String getActiveAppBundleIdFromSession(IOSDriver<? extends WebElement> driver) {
        return (String) AppiumSessionCollector.getSession(driver)
                .getProperties()
                .get(MobileCommonHelper.PROPERTY_NAME_IOS_BUNDLEID);
    }

}
