package com.technochord.stripe;

import com.stripe.model.Charge;
import com.stripe.model.Coupon;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.technochord.stripe.model.CustomerLite;
import com.technochord.stripe.service.StripeService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class StripeApplicationIntegrationTests {

	private StripeService stripeService;

    private String apiKey = System.getProperty("stripe.apiKey");

	@Before
	public void setup() {
	    stripeService = new StripeService(apiKey);
		List<CustomerLite> customerList = stripeService.listAllCustomers();
		for (CustomerLite c : customerList) {
			//cancel all subscriptions
			stripeService.cancelAllExistingSubscriptionsForCustomer(c.getId());
		}
		stripeService.deleteAllCustomers();
	}

	@Test (expected = StripeException.class)
	public void test_creation_of_customer_with_same_email_should_fail() {
		//Create a customer
		String customerId = stripeService.createCustomer("anEmail@email.com", "a description");

		//Create  customer eith same email different description
		stripeService.createCustomer("anEmail@email.com", "a new description");
	}

	@Test
	public void test_retrieval_of_non_existent_customerId() {
		//Create a customer
		String customerId = stripeService.createCustomer("anEmail@email.com", "a description");

		Customer c = stripeService.retrieveCustomerById(customerId + "bogus");
		Assert.assertTrue(c == null);
	}

	@Test
	public void test_retrieval_of_non_existent_customer_email() {
		//Create a customer
		stripeService.createCustomer("anEmail@email.com", "a description");

		Customer c = stripeService.retrieveCustomerByEmail("bogus@email.com");
		Assert.assertTrue(c == null);
	}

	@Test
	public void test_successful_subscription() {
		//Create a customer
		String customerId = stripeService.createCustomer("anEmail@email.com", "a description");

		//Add a payment source
		stripeService.replacePaymentSourceForCustomer(customerId, "tok_amex");

		//Create a subscription against a plan and charge the customer
		String subscriptionId = stripeService.createSubscriptionForCustomerAndCharge("anEmail@email.com", "monthly-plan");

		//Invoice
		Invoice invoice = stripeService.getLatestInvoiceForSubscription(subscriptionId);

		Assert.assertTrue(invoice.getAmountPaid() > 0);
	}

	@Test (expected = StripeException.class)
	public void test_subscription_without_payment_source_on_customer_should_fail() {
		//Create a customer
		String customerId = stripeService.createCustomer("anEmail@email.com", "a description");

		//Create a subscription against a plan and charge the customer
		String subscriptionId = stripeService.createSubscriptionForCustomerAndCharge("anEmail@email.com", "monthly-plan");

	}

	@Test (expected = StripeException.class)
	public void test_subscription_after_removal_of_payment_source_on_customer_should_fail() {
		//Create a customer
		String customerId = stripeService.createCustomer("anEmail@email.com", "a description");

		//Add a payment source
		stripeService.replacePaymentSourceForCustomer(customerId, "tok_amex");

		//Create a subscription against a plan and charge the customer
		String subscriptionId = stripeService.createSubscriptionForCustomerAndCharge("anEmail@email.com", "monthly-plan");

		//Now remove the payment source
		stripeService.removePaymentSourceFromCustomer(customerId);

		//Try subscribing again
		stripeService.createSubscriptionForCustomerAndCharge("anEmail@email.com", "monthly-plan");
	}


	@Test
	public void test_that_coupon_gets_applied() {
		//Create a customer
		String customerId = stripeService.createCustomer("anEmail@email.com", "a description");

		//Add a payment source
		stripeService.replacePaymentSourceForCustomer(customerId, "tok_amex");

		//Create a subscription against a plan and charge the customer
		String subscriptionId = stripeService.createSubscriptionForCustomerAndCharge("anEmail@email.com", "monthly-plan");

		//Check the invoiced amount is correct
		Invoice invoice = stripeService.getLatestInvoiceForSubscription(subscriptionId);

		Assert.assertTrue(invoice.getAmountPaid() == 100);

		//Apply a coupon to the customer
		stripeService.applyCouponToCustomer(customerId, "TEST_COUPON_ID");

		//Subscribe again
		String reSubscriptionId = stripeService.createSubscriptionForCustomerAndCharge("anEmail@email.com", "monthly-plan");

		// Check invoice.. should be the discounted amount
		Invoice reInvoice = stripeService.getLatestInvoiceForSubscription(reSubscriptionId);

		Assert.assertTrue(reInvoice.getAmountPaid() == 90);
	}

	@Test
	public void test_that_payment_source_can_get_changed() {
		//Create a customer
		String customerId = stripeService.createCustomer("anEmail@email.com", "a description");

		//Add a payment source
		stripeService.replacePaymentSourceForCustomer(customerId, "tok_amex");

		//Create a subscription against a plan and charge the customer
		String subscriptionId = stripeService.createSubscriptionForCustomerAndCharge("anEmail@email.com", "monthly-plan");

		//Invoice
		Invoice invoice = stripeService.getLatestInvoiceForSubscription(subscriptionId);

		//Get the related charge
		Charge charge = stripeService.getCharge(invoice.getCharge());

		Assert.assertTrue(charge.getStatus().equals("succeeded"));

		//Change the payment source
		stripeService.replacePaymentSourceForCustomer(customerId, "tok_visa");

		//Subscribe again
		String reSubscriptionId = stripeService.createSubscriptionForCustomerAndCharge("anEmail@email.com", "monthly-plan");

		// Invoice
		Invoice reInvoice = stripeService.getLatestInvoiceForSubscription(reSubscriptionId);

		// Get the related charge
		Charge reCharge = stripeService.getCharge(reInvoice.getCharge());

		//Assert success
		Assert.assertTrue(reCharge.getStatus().equals("succeeded"));
	}

	@Test
	public void test_get_customers_of_a_certain_category_and_updation_of_customer_category() {
		//Create some customers, some with categories
		String customerId1 = stripeService.createCustomer("anEmail1@email.com", "a description1");
		String customerId2 = stripeService.createCustomer("anEmail2@email.com", "a description2");
		String customerId3 = stripeService.createCustomer("anEmail3@email.com", "a description3","CAT1");
		String customerId4 = stripeService.createCustomer("anEmail4@email.com", "a description4");
		String customerId5 = stripeService.createCustomer("anEmail5@email.com", "a description5","CAT1");
		String customerId6 = stripeService.createCustomer("anEmail6@email.com", "a description6","CAT1");
		String customerId7 = stripeService.createCustomer("anEmail7@email.com", "a description7");
		String customerId8 = stripeService.createCustomer("anEmail8@email.com", "a description8");

		//Retrieve that category of customers
		List<CustomerLite> cat1CustomerLiteList = stripeService.listAllCustomersByCategory("CAT1");

		Assert.assertTrue(cat1CustomerLiteList != null);
		Assert.assertTrue(cat1CustomerLiteList.size() == 3);
		Assert.assertTrue(cat1CustomerLiteList.stream().filter(c -> {
			return c.getEmail().equals("anEmail6@email.com");
		}).count() == 1);
		Assert.assertTrue(cat1CustomerLiteList.stream().filter(c -> {
			return c.getEmail().equals("anEmail5@email.com");
		}).count() == 1);
		Assert.assertTrue(cat1CustomerLiteList.stream().filter(c -> {
			return c.getEmail().equals("anEmail3@email.com");
		}).count() == 1);

		//Now update the category of one of the customers
		stripeService.updateCustomerCategory(customerId5, "CAT2");

		//Re-retrieve that category of customers
		cat1CustomerLiteList = stripeService.listAllCustomersByCategory("CAT1");

		Assert.assertTrue(cat1CustomerLiteList != null);
		Assert.assertTrue(cat1CustomerLiteList.size() == 2);

		//Re-retrieve that category of customers
		List<CustomerLite> cat2CustomerLiteList = stripeService.listAllCustomersByCategory("CAT2");

		Assert.assertTrue(cat2CustomerLiteList != null);
		Assert.assertTrue(cat2CustomerLiteList.size() == 1);
		Assert.assertTrue(cat2CustomerLiteList.stream().filter(c -> {
			return c.getEmail().equals("anEmail5@email.com");
		}).count() == 1);
	}

	@Test
	public void test_change_customers_email_before_subscription() {
		//Create a customer
		String customerId1 = stripeService.createCustomer("anEmail1@email.com", "a description1");

		stripeService.changeCustomerEmail(customerId1, "someOther@email.com");

		Customer customer = stripeService.retrieveCustomerByEmail("someOther@email.com");

		Assert.assertTrue(customer != null);
	}

	@Test
	public void test_change_customers_email_after_subscription() {
		//Create a customer
		String customerId = stripeService.createCustomer("anEmail@email.com", "a description");

		//Add a payment source
		stripeService.replacePaymentSourceForCustomer(customerId, "tok_amex");

		//Create a subscription against a plan and charge the customer
		String subscriptionId = stripeService.createSubscriptionForCustomerAndCharge("anEmail@email.com", "monthly-plan");

		stripeService.changeCustomerEmail(customerId, "someOther@email.com");

		Customer customer = stripeService.retrieveCustomerByEmail("someOther@email.com");

		Assert.assertTrue(customer != null);
	}

	@Test (expected = StripeException.class)
	public void test_change_customers_email_to_existing_email() {

		//Create 2 customers
		String customerId1 = stripeService.createCustomer("anEmail1@email.com", "a description1");
		String customerId2 = stripeService.createCustomer("anEmail2@email.com", "a description2");

		stripeService.changeCustomerEmail(customerId1, "anEmail2@email.com");
	}

	@Test
	public void test_if_customer_has_valid_payment_source() {
		//Create a customer
		String customerId = stripeService.createCustomer("anEmail@email.com", "a description");

		//Add a payment source
		stripeService.replacePaymentSourceForCustomer(customerId, "tok_amex");

		Boolean boo = stripeService.doesCustomerHaveActivePaymentSource(customerId);

		Assert.assertTrue(boo);

		stripeService.removePaymentSourceFromCustomer(customerId);

		boo = stripeService.doesCustomerHaveActivePaymentSource(customerId);

		Assert.assertTrue(!boo);
	}

	@Test
	// @Ignore //external dep
	public void test_list_all_coupons() {
		List<Coupon> couponList = stripeService.listAllCoupons();

		Assert.assertTrue(couponList != null);
		Assert.assertTrue(couponList.size() == 1);
	}

	@Test
	public void test_list_all_invoices() {
		List<Invoice> invoiceList = stripeService.listAllInvoices();

		Assert.assertTrue(invoiceList != null);
		Assert.assertTrue(invoiceList.size() > 0);
	}
}
