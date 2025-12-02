package util;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class BackgroundTask {

    public static <T> void execute(
            TaskOperation<T> operation,
            TaskCallback<T> callback,
            ErrorHandler errorHandler) {

        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return operation.perform();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    SwingUtilities.invokeLater(() -> callback.onSuccess(result));
                } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                    SwingUtilities.invokeLater(() -> errorHandler.onError(e));
                }
            }
        };
        worker.execute();
    }

    @FunctionalInterface
    public interface TaskOperation<T> {
        T perform() throws Exception;
    }

    @FunctionalInterface
    public interface TaskCallback<T> {
        void onSuccess(T result);
    }

    @FunctionalInterface
    public interface ErrorHandler {
        void onError(Exception e);
    }
}
