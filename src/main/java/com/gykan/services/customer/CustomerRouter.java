package com.gykan.services.customer;

import com.gykan.services.common.model.Account;
import com.gykan.services.common.model.Customer;
import com.gykan.services.customer.service.AggregationStrategyImpl;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.remote.ConsulConfigurationDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spring.boot.FatJarRouter;
import org.apache.camel.zipkin.starter.CamelZipkin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@CamelZipkin
public class CustomerRouter extends FatJarRouter {

	@Autowired
	private CamelContext context;
	
	@Value("${port}")
	private int port;
			
	@Override
	public void configure() throws Exception {
        String consulUrl = System.getProperty("consul");

        if (consulUrl == null) {
            consulUrl = "http://127.0.0.1:8500";
        }

        JacksonDataFormat format = new JacksonDataFormat();
		format.useList();
		format.setUnmarshalType(Account.class);
		
		ConsulConfigurationDefinition config = new ConsulConfigurationDefinition();
		config.setComponent("netty4-http");
		config.setUrl(consulUrl);
		context.setServiceCallConfiguration(config);
		
		restConfiguration()
			.component("netty4-http")
			.bindingMode(RestBindingMode.json)
			.port(port);
		
		from("direct:start").routeId("account-consul").marshal().json(JsonLibrary.Jackson)
			.setHeader(Exchange.HTTP_METHOD, constant("PUT"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
			.to(consulUrl + "/v1/agent/service/register");
		from("direct:stop").shutdownRunningTask(ShutdownRunningTask.CompleteAllTasks)
			.toD(consulUrl + "/v1/agent/service/deregister/${header.id}");
		
		rest("/customer")
			.get("/")
				.to("bean:customerService?method=findAll")
			.post("/").consumes("application/json").type(Customer.class)
				.to("bean:customerService?method=add(${body})")
			.get("/{id}").to("direct:account");
		
		
		from("direct:account")
			.to("bean:customerService?method=findById(${header.id})")
			.log("Msg: ${body}").enrich("direct:acc", new AggregationStrategyImpl());
		
		
		from("direct:acc").setBody().constant(null).serviceCall("account//account").unmarshal(format);
			
	}
		
}
