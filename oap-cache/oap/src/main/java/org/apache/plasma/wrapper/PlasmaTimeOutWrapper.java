package org.apache.plasma.wrapper;

import java.util.concurrent.*;

import org.apache.arrow.plasma.exceptions.DuplicateObjectException;
import org.apache.arrow.plasma.exceptions.PlasmaClientException;
import org.apache.arrow.plasma.exceptions.PlasmaGetException;

/**
 * Plasma Server store may dead or no response during the runtime
 * Wrapper plasmaClient methods with a timeOut mechanism
 */
public class PlasmaTimeOutWrapper implements Callable<Object> {
  private ExecutorService executorService;
  private Object[] paramValues;
  private long timeOutInSeconds;
  private PlasmaLambdaWrapper wrapper;

  private PlasmaTimeOutWrapper() {
  }

  public static Object run (
          PlasmaLambdaWrapper wrapper,
          ExecutorService executorService,
          Object[] paramValues, long timeOutInSeconds)
          throws InterruptedException, ExecutionException, TimeoutException,
          DuplicateObjectException, PlasmaGetException, PlasmaClientException {
    PlasmaTimeOutWrapper plasmaTimeOutWrapper = new PlasmaTimeOutWrapper();
    return plasmaTimeOutWrapper.submitFutureTask(wrapper,executorService, paramValues, timeOutInSeconds);
  }

  private Object submitFutureTask(
          PlasmaLambdaWrapper wrapper,
          ExecutorService executorService,
          Object[] paramValues, long timeOutInSeconds)
          throws InterruptedException, ExecutionException, TimeoutException,
          DuplicateObjectException, PlasmaGetException, PlasmaClientException {
    this.wrapper = wrapper;
    this.executorService = executorService;
    this.paramValues = paramValues;
    this.timeOutInSeconds = timeOutInSeconds;
    FutureTask<Object> futureTask = (FutureTask<Object>) executorService.submit(this);
    executorService.execute(futureTask);
    return futureTask.get(timeOutInSeconds, TimeUnit.SECONDS);
  }

  @Override
  public Object call() {
    return wrapper.execute(paramValues);
  }
}
