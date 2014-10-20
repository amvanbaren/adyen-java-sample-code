package com.adyen.examples.recurring;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import com.adyen.services.payment.Recurring;
import com.adyen.services.recurring.RecurringDetail;
import com.adyen.services.recurring.RecurringDetailsRequest;
import com.adyen.services.recurring.RecurringDetailsResult;
import com.adyen.services.recurring.RecurringPortType;
import com.adyen.services.recurring.RecurringService;
import com.adyen.services.recurring.ServiceException;

/**
 * Retrieve recurring contract details (SOAP)
 * 
 * Once a shopper has stored RECURRING details with Adyen you are able to process a RECURRING payment. This file shows
 * you how to retrieve the RECURRING contract(s) for a shopper using SOAP.
 * 
 * Please note: using our API requires a web service user. Set up your Webservice user:
 * Adyen CA >> Settings >> Users >> ws@Company. >> Generate Password >> Submit
 * 
 * @link /5.Recurring/Soap/RetrieveRecurringContract
 * @author Created by Adyen - Payments Made Easy
 */

@WebServlet(urlPatterns = { "/5.Recurring/Soap/RetrieveRecurringContract" })
public class RetrieveRecurringContractSoap extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/**
		 * SOAP settings
		 * - wsdl: the WSDL url you are using (Test/Live)
		 * - wsUser: your web service user
		 * - wsPassword: your web service user's password
		 */
		String wsdl = "https://pal-test.adyen.com/pal/Recurring.wsdl";
		String wsUser = "YourWSUser";
		String wsPassword = "YourWSPassword";

		/**
		 * Create SOAP client, using classes in adyen-wsdl-cxf.jar library (generated by wsdl2java tool, Apache CXF).
		 * 
		 * @see WebContent/WEB-INF/lib/adyen-wsdl-cxf.jar
		 */
		RecurringService service = new RecurringService(new URL(wsdl));
		RecurringPortType client = service.getRecurringHttpPort();

		// Set HTTP Authentication
		((BindingProvider) client).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, wsUser);
		((BindingProvider) client).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, wsPassword);

		/**
		 * The recurring details request should contain the following variables:
		 * 
		 * <pre>
		 * - merchantAccount    : Your merchant account.
		 * - shopperReference   : The reference to the shopper. This shopperReference must be the same as the
		 *                        shopperReference used in the initial payment.
		 * - recurring
		 *     - contract       : This should be the same value as recurringContract in the payment where the recurring
		 *                        contract was created. However if ONECLICK,RECURRING was specified initially then this
		 *                        field can be either ONECLICK or RECURRING.
		 * </pre>
		 */

		// Create new recurring details request
		RecurringDetailsRequest recurringRequest = new RecurringDetailsRequest();
		recurringRequest.setMerchantAccount("YourMerchantAccount");
		recurringRequest.setShopperReference("TheShopperReference");

		// Set recurring
		Recurring recurring = new Recurring();
		recurring.setContract("RECURRING");
		recurringRequest.setRecurring(recurring);

		/**
		 * Send the recurring details request.
		 */
		RecurringDetailsResult recurringResult;
		try {
			recurringResult = client.listRecurringDetails(recurringRequest);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}

		/**
		 * The recurring details response will contain the following fields:
		 * 
		 * <pre>
		 * - creationDate
		 * - lastKnownShopperEmail
		 * - shopperReference
		 * - recurringDetail              : A list of zero or more details, containing:
		 *     - recurringDetailReference : The reference the details are stored under.
		 *     - variant                  : The payment method (e.g. mc, visa, elv, ideal, paypal).
		 *                                  For some variants, like iDEAL, the sub-brand is returned like idealrabobank.
		 *     - creationDate             : The date when the recurring details were created.
		 *     - card                     : A container for credit card data.
		 *     - elv                      : A container for ELV data.
		 *     - bank                     : A container for BankAccount data.
		 * </pre>
		 * 
		 * The recurring contracts are stored in the same object types as you would have submitted in the initial
		 * payment. Depending on the payment method one or more fields may be blank or incomplete (e.g. CVC for
		 * card). Only one of the detail containers (card/elv/bank) will be returned per detail block, the others will
		 * be null. For PayPal there is no detail container.
		 */
		PrintWriter out = response.getWriter();

		out.println("Recurring Details Result:");
		out.println("- creationDate: " + recurringResult.getCreationDate());
		out.println("- lastKnownShopperEmail: " + recurringResult.getLastKnownShopperEmail());
		out.println("- shopperReference: " + recurringResult.getShopperReference());
		out.println("- recurringDetail:");

		for (RecurringDetail recurringDetail : recurringResult.getDetails().getRecurringDetail()) {
			out.println("  > * recurringDetailReference: " + recurringDetail.getRecurringDetailReference());
			out.println("    * variant: " + recurringDetail.getVariant());
			out.println("    * creationDate: " + recurringDetail.getCreationDate());
			out.println("    * bank: " + recurringDetail.getBank());
			out.println("    * card: " + recurringDetail.getCard());
			out.println("    * elv: " + recurringDetail.getElv());
			out.println("    * name: " + recurringDetail.getName());
		}
		
	}

}