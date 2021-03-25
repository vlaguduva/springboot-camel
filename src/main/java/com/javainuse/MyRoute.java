package com.javainuse;


import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.apache.camel.ShutdownRoute;

@Component
public class MyRoute extends RouteBuilder {
 
    @Override
    public void configure() throws Exception {
		
		getContext().setShutdownStrategy(new MyShutdownStrategy());
		getContext().getShutdownStrategy().setTimeout(5 * 60);
		
    	from("file:/var/inputFolder?noop=true").startupOrder(1).delay(120000).to("direct:intermediate");
		
		from("direct:intermediate").startupOrder(2)
                    .shutdownRoute(ShutdownRoute.Defer).to("file:/var/outputFolder");
		
    }
}
