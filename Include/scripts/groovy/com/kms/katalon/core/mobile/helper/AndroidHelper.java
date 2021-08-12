package com.kms.katalon.core.mobile.helper;

import java.text.MessageFormat;
import java.util.Map;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.mobile.constants.StringConstants;
import com.kms.katalon.core.util.internal.JsonUtil;

import io.appium.java_client.android.AndroidDriver;

public final class AndroidHelper {

    private AndroidHelper() {
    }

    private static final KeywordLogger logger = KeywordLogger.getInstance(AndroidHelper.class);

    public static int getStatusBarHeightAndroid(AndroidDriver<? extends WebElement> driver,
            DevicePixelRatio pixelRatio) {
        try {
            return getStatusBarByProperty(driver);
        } catch (WebDriverException e) {
            logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_FAILED_GET_OS_STATUSBAR, "default method"));
        }
        try {
            return getStatusBarHeightByCommand(driver, pixelRatio);
        } catch (WebDriverException e) {
            logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_FAILED_GET_OS_STATUSBAR, "command"));
        }
        logger.logWarning(StringConstants.KW_LOG_FAILED_GET_STATUSBAR);
        return 0;
    }

    private static int getStatusBarByProperty(AndroidDriver<? extends WebElement> driver) throws WebDriverException {
        Map<String, String> bars = driver.getSystemBars();
        AndroidSystemBarProperties barsProps = JsonUtil.fromJson(JsonUtil.toJson(bars),
                AndroidSystemBarProperties.class);
        return barsProps.getStatusBar().getHeight();
    }

    private static int getStatusBarHeightByCommand(AndroidDriver<? extends WebElement> driver,
            DevicePixelRatio pixelRatio) {
        Object res = driver.executeScript("mobile:viewportRect");
        ViewportRect vp = JsonUtil.fromJson(res.toString(), ViewportRect.class);
        return vp.top == 0 ? 0 : (int) (vp.top / pixelRatio.ratioY);
    }

}

class ViewportRect {
    public int left;

    public int top;

    public int height;

    public int width;

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}

class AndroidSystemBarProperties {
    private OSBarProperties statusBar;

    private OSBarProperties navigationBar;

    public OSBarProperties getStatusBar() {
        return statusBar;
    }

    public void setStatusBar(OSBarProperties statusBar) {
        this.statusBar = statusBar;
    }

    public OSBarProperties getNavigationBar() {
        return navigationBar;
    }

    public void setNavigationBar(OSBarProperties navigationBar) {
        this.navigationBar = navigationBar;
    }

}

class OSBarProperties {
    private int x;

    private int y;

    private int width;

    private int height;

    private boolean visible;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public OSBarProperties(int x, int y, int width, int height, boolean visible) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = visible;
    }
}
