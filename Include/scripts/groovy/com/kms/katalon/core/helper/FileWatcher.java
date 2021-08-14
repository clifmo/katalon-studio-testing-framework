package com.kms.katalon.core.helper;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class FileWatcher {

    public interface IWatchListener {

        public void call(WatchEvent.Kind<?> eventKind, File file);

    }

    private List<IWatchListener> listeners = new ArrayList<>();

    private Thread watchingThread;

    private String trackedFile;

    private boolean canRun = false;

    private boolean isTrackedFileUpdated = false;

    private FileWatcher() {
    }

    public static FileWatcher watch(File file) {
        return watch(file.getAbsolutePath());
    }

    public static FileWatcher watch(String filePath) {
        FileWatcher watcher = new FileWatcher();
        watcher.trackedFile = FilenameUtils.separatorsToSystem(filePath);
        watcher.startWatching();
        return watcher;
    }

    public void addEventListener(IWatchListener callback) {
        listeners.add(callback);
    }

    @SuppressWarnings("unchecked")
    private void startWatching() {
        if (StringUtils.isBlank(trackedFile)) {
            return;
        }

        canRun = true;
        watchingThread = new Thread() {
            @Override
            public void run() {
                try {
                    do {
                        isTrackedFileUpdated = false;
                        Path filePath = FileSystems.getDefault().getPath(trackedFile);
                        WatchService watcher = FileSystems.getDefault().newWatchService();
                        Path dir = filePath.getParent();
                        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

                        while (canRun && !isTrackedFileUpdated) {
                            for (WatchEvent<?> watchEvent : key.pollEvents()) {
                                WatchEvent.Kind<?> kind = watchEvent.kind();
                                WatchEvent<Path> event = (WatchEvent<Path>) watchEvent;
                                Path filename = event.context();
                                if (filename.compareTo(filePath.getFileName()) == 0) {
                                    dispatchEvent(kind, filename.toFile());
                                }
                            }
                            Thread.sleep(500L);
                        }
                    } while (canRun);
                } catch (IOException | InterruptedException exception) {
                    // Just skip
                }
            }
        };
        watchingThread.start();
    }

    public void stop() {
        canRun = false;
    }

    public void waitToStop() throws InterruptedException {
        watchingThread.join();
    }

    public void updateTrackedFile(String filePath) {
        trackedFile = filePath;
        isTrackedFileUpdated = true;
    }

    private void dispatchEvent(WatchEvent.Kind<?> eventKind, File file) {
        listeners.stream().forEachOrdered(listener -> {
            try {
                listener.call(eventKind, file);
            } catch (Exception exception) {
                // Just skip
            }
        });
    }
}
