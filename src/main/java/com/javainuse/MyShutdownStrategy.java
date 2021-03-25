package com.javainuse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultShutdownStrategy;
import org.apache.camel.spi.RouteStartupOrder;

/**
 * Custom {@link DefaultShutdownStrategy}.
 * 
 * @author venkatesh_laguduva Created: Mar 1, 2021
 */
public class MyShutdownStrategy extends DefaultShutdownStrategy {

    @Override
    protected boolean doShutdown(final CamelContext context, final List<RouteStartupOrder> routes,
        final long timeout, final TimeUnit timeUnit, final boolean suspendOnly,
        final boolean abortAfterTimeout, final boolean forceShutdown) throws Exception {

        waitForJobCompletion(context, routes, timeout);

		return super.doShutdown(context, routes, 1L, timeUnit, suspendOnly, true, false);

    }

    private void waitForJobCompletion(final CamelContext context, final List<RouteStartupOrder> routes,
        final long timeout) throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        try {
            executorService.invokeAll(Arrays.asList((Callable<Boolean>) () -> {

                while (true) {

                    int size = 0;
                    int inflight = 0;

                    for (RouteStartupOrder order : routes) {
                        inflight = context.getInflightRepository().size(order.getRoute().getId());
                        inflight += getPendingInflightExchanges(order);
                        size += inflight;
                        if (inflight > 0) {
                            System.out.println(inflight + " inflight and pending exchanges for route: " + order.getRoute().getId());
                            System.out.println("remaining routes won't be checked for inflight messages.");
                        }
                    }

                    if (size == 0) {
                        System.out.println("No inflight exchanges to completed.");
                        break;
                    }
                    System.out.println(size + " number of inflight exchanges are pending.");
                    Thread.sleep(1000);
                }
                return Boolean.TRUE;
            }), timeout, TimeUnit.MINUTES);

            System.out.println("Wait for job completion is over.");
			Thread.sleep(60000);
        }
        finally {
            if (!executorService.isShutdown()) {
                executorService.shutdown();
            }
        }
    }
}
