package org.apache;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import org.apache.arrow.plasma.PlasmaClient;
import org.apache.arrow.plasma.exceptions.DuplicateObjectException;
import org.apache.arrow.plasma.exceptions.PlasmaClientException;
import org.apache.arrow.plasma.exceptions.PlasmaGetException;

/**
 * Plasma Server store may dead or no response during the runtime
 * Wrapper plasmaClient methods with a timeOut mechanism
 */
public class PlasmaTimeOutWrapper implements Callable<Object> {
  private String methodName;
  private PlasmaClient plasmaClient;
  private ExecutorService executorService;
  private Object[] paramValues;
  private long timeOutInSeconds;

  private PlasmaTimeOutWrapper() {
  }

  public static Object run(
          String methodName,
          PlasmaClient plasmaClient,
          ExecutorService executorService,
          Object[] paramValues, long timeOutInSeconds)
          throws InterruptedException, ExecutionException, TimeoutException,
          DuplicateObjectException, PlasmaGetException, PlasmaClientException {
    PlasmaTimeOutWrapper plasmaTimeOutWrapper = new PlasmaTimeOutWrapper();
    return plasmaTimeOutWrapper.submitFutureTask(methodName, plasmaClient,
            executorService, paramValues, timeOutInSeconds);
  }

  public Object submitFutureTask(
          String methodName,
          PlasmaClient plasmaClient,
          ExecutorService executorService,
          Object[] paramValues, long timeOutInSeconds)
          throws InterruptedException, ExecutionException, TimeoutException,
          DuplicateObjectException, PlasmaGetException, PlasmaClientException {
    this.methodName = methodName;
    this.plasmaClient = plasmaClient;
    this.executorService = executorService;
    this.paramValues = paramValues;
    this.timeOutInSeconds = timeOutInSeconds;
    FutureTask<Object> futureTask = (FutureTask<Object>) executorService.submit(this);
    executorService.execute(futureTask);
    return futureTask.get(timeOutInSeconds, TimeUnit.SECONDS);
  }

  @Override
  public Object call() throws Exception {
    Method[] methods = plasmaClient.getClass().getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      if (this.methodName.equals(methods[i].getName())) {
        if (methods[i].getParameterTypes().length == paramValues.length) {
          return methods[i].invoke(plasmaClient, paramValues);
        }
      }
    }
    throw new Exception("Plasma Client do not have " + this.methodName + " !" + "Or " +
            this.methodName + " params not match");
  }
}
