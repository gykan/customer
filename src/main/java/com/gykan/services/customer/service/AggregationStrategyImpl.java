package com.gykan.services.customer.service;

import java.util.List;

import com.gykan.services.common.model.Account;
import com.gykan.services.common.model.Customer;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class AggregationStrategyImpl implements AggregationStrategy {
 
    @SuppressWarnings("unchecked")
	public Exchange aggregate(Exchange original, Exchange resource) {
        Object originalBody = original.getIn().getBody();
        Object resourceResponse = resource.getIn().getBody();
        Customer customer = (Customer) originalBody;
        customer.setAccounts((List<Account>) resourceResponse);
        return original;
    }

}
