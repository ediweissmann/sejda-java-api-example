package org.sejda.example;

import java.io.File;

import org.sejda.core.notification.context.GlobalNotificationContext;
import org.sejda.core.service.DefaultTaskExecutionService;
import org.sejda.core.service.TaskExecutionService;
import org.sejda.model.TopLeftRectangularBox;
import org.sejda.model.exception.SejdaRuntimeException;
import org.sejda.model.input.PdfFileSource;
import org.sejda.model.notification.EventListener;
import org.sejda.model.notification.event.PercentageOfWorkDoneChangedEvent;
import org.sejda.model.notification.event.TaskExecutionCompletedEvent;
import org.sejda.model.notification.event.TaskExecutionFailedEvent;
import org.sejda.model.output.ExistingOutputPolicy;
import org.sejda.model.output.FileOrDirectoryTaskOutput;
import org.sejda.model.parameter.SplitByPagesParameters;
import org.sejda.model.parameter.SplitByTextContentParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple application demonstrating how Sejda's Java API can be used.
 *
 * @author Andrea Vacondio
 * @author Edi Weissmann
 * 
 */
public final class SampleCode {

    private static final Logger LOG = LoggerFactory.getLogger(SampleCode.class);

    public static void main(String[] args) {
        exampleSplitByPages();
        exampleSplitByText();
    }

    private static void exampleSplitByPages() {
        // configure the split by pages task
        SplitByPagesParameters taskParameters = new SplitByPagesParameters();
        // which file should be split
        taskParameters.addSource(PdfFileSource.newInstanceNoPassword(new File("/Users/edi/Desktop/test.pdf")));
        // split at page 10 and 20
        taskParameters.addPage(10);
        taskParameters.addPage(20);

        // where to output PDF document results
        taskParameters.setExistingOutputPolicy(ExistingOutputPolicy.OVERWRITE);
        taskParameters.setOutput(new FileOrDirectoryTaskOutput(new File("/tmp/output1")));

        // register listeners to get events about progress, failure, completion.
        registerTaskListeners();

        // execute the task
        TaskExecutionService taskExecutionService = new DefaultTaskExecutionService();
        taskExecutionService.execute(taskParameters);
    }

    private static void exampleSplitByText() {
        // configure the split by text task

        // text area boundaries
        TopLeftRectangularBox textArea = new TopLeftRectangularBox(10, 20, 100, 200);
        SplitByTextContentParameters taskParameters = new SplitByTextContentParameters(textArea);

        // inputs
        taskParameters.addSource(PdfFileSource.newInstanceNoPassword(new File("/Users/edi/Desktop/test.pdf")));

        // where to output PDF document results
        taskParameters.setExistingOutputPolicy(ExistingOutputPolicy.OVERWRITE);
        taskParameters.setOutput(new FileOrDirectoryTaskOutput(new File("/tmp/output2")));

        // register listeners to get events about progress, failure, completion.
        registerTaskListeners();

        // execute the task
        TaskExecutionService taskExecutionService = new DefaultTaskExecutionService();
        taskExecutionService.execute(taskParameters);
    }

    private static void registerTaskListeners() {
        GlobalNotificationContext.getContext().addListener(new ProgressListener());
        GlobalNotificationContext.getContext().addListener(new FailureListener());
        GlobalNotificationContext.getContext().addListener(new CompletionListener());
    }

    /**
     * Listener printing the percentage of work done by the task
     * 
     * @author Andrea Vacondio
     *
     */
    private static class ProgressListener implements EventListener<PercentageOfWorkDoneChangedEvent> {

        @Override
        public void onEvent(PercentageOfWorkDoneChangedEvent event) {
            LOG.info("Task progress: {}% done.", event.getPercentage().toPlainString());
        }
    }

    /**
     * Listener exiting with an error code in case of task failure
     * 
     * @author Andrea Vacondio
     * 
     */
    private static class FailureListener implements EventListener<TaskExecutionFailedEvent> {

        @Override
        public void onEvent(TaskExecutionFailedEvent event) {
            LOG.error("Task execution failed.");
            // rethrow it to the main
            throw new SejdaRuntimeException(event.getFailingCause());
        }
    }

    /**
     * Listener informing the user about the task completion.
     * 
     * @author Andrea Vacondio
     * 
     */
    private static class CompletionListener implements EventListener<TaskExecutionCompletedEvent> {

        @Override
        public void onEvent(TaskExecutionCompletedEvent event) {
            LOG.info("Task completed in {} millis.", event.getExecutionTime());
        }

    }
}
