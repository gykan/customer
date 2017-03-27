package com.gykan.services.customer.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gykan.services.common.model.Customer;
import org.apache.camel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

	@Autowired
	private CamelContext context;

	@Autowired
	ProducerTemplate template;

	private List<Customer> customers = new ArrayList<>();
	
	public Customer findById(Integer id) {
		return new Customer(id, "XXX", "1234567890");
	}
	
	public List<Customer> findAll() {
		Customer c1 = new Customer(1, "XXX", "1234567890");
		Customer c2 = new Customer(2, "YYY", "1234567891");
//        System.out.println(template.requestBodyAndHeader("direct:accounts", "", "id", "1"));
		return Arrays.asList(c1, c2);
	}
	
	public Customer add(Customer customer) {
		customer.setId(customers.size()+1);
		customers.add(customer);
		return customer;
	}
	
}
