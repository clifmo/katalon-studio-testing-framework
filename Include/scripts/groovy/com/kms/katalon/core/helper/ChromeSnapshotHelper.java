package com.kms.katalon.core.helper;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.openqa.selenium.WebDriver;

import com.github.kklisura.cdt.protocol.commands.Page;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.event.EventBusSingleton;
import com.kms.katalon.core.event.TestingEvent;
import com.kms.katalon.core.execution.TestExecutionStringUtil;
import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.testcase.BrokenTestCaseSummary;
import com.kms.katalon.core.util.CDTUtils;
import com.kms.katalon.core.util.internal.PathUtil;

public class ChromeSnapshotHelper {

    protected final KeywordLogger logger = KeywordLogger.getInstance(this.getClass());

    private WebDriver driver;

    public ChromeSnapshotHelper() {
        EventBusSingleton.getInstance().getEventBus().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTestingEvent(TestingEvent event) {
        switch (event.getType()) {
            case BROWSER_OPENED:
                if (isEnableTimeCapsule()) {
                    this.driver = (WebDriver) event.getData();
                }
                break;
            default:
                break;
        }
    }

    private boolean isEnableTimeCapsule() {
        return RunConfiguration.shouldApplyTimeCapsule();
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public String captureSnapshot(String testArtifactFolderPath, String testName) {
        String pathToMHTML = generateMHTMLPath(testArtifactFolderPath, testName);
        if (this.driver == null) {
            return null;
        }
        ChromeDevToolsService chromeDevToolsService = CDTUtils.getService(this.driver);
        if (chromeDevToolsService == null) {
            return null;
        }
        Page page = chromeDevToolsService.getPage();
        String dataOfMHTML = page.captureSnapshot();
        writeFile(pathToMHTML, dataOfMHTML);
        String mhtmlPathRelativeToProjectFolder = getPathRelativeToProjectFolder(pathToMHTML);
        logger.logInfo("Time Capsule is available at " + mhtmlPathRelativeToProjectFolder);
        return pathToMHTML;
    }

    private String generateMHTMLPath(String folderToStoreMhtml, String testName) {
        String unoffensiveTestCaseName = TestExecutionStringUtil.getUnoffensiveTestCaseName(testName);
        return folderToStoreMhtml + File.separator + unoffensiveTestCaseName + "."
                + BrokenTestCaseSummary.Constants.MHTML_EXTENSION;
    }

    private void writeFile(String fileName, String data) {
        try {
            File file = new File(fileName);
            file.createNewFile();
            FileUtils.writeStringToFile(file, data);
        } catch (IOException | NullPointerException exception) {
            logger.logWarning(exception.getMessage());
        }
    }

    private String getPathRelativeToProjectFolder(String absolutePath) {
        String base = RunConfiguration.getProjectDir();
        return PathUtil.absoluteToRelativePath(absolutePath, base);
    }
}
