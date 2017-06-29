package com.adyen.examples.api.Soap;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import com.adyen.services.common.Amount;
import com.adyen.services.payment.AnyType2AnyTypeMap;
import com.adyen.services.payment.AnyType2AnyTypeMap.Entry;
import com.adyen.services.payment.PaymentPortType;
import com.adyen.services.payment.PaymentRequest;
import com.adyen.services.payment.PaymentResult;
import com.adyen.services.payment.PaymentService;
import com.adyen.services.payment.ServiceException;

/**
 * Create Client-Side Encryption Payment (SOAP)
 * 
 * Merchants that require more stringent security protocols or do not want the additional overhead of managing their PCI
 * compliance, may decide to implement Client-Side Encryption (CSE). This is particularly useful for Mobile payment
 * flows where only cards are being offered, as it may result in faster load times and an overall improvement to the
 * shopper flow. The Adyen Hosted Payment Page (HPP) provides the most comprehensive level of PCI compliancy and you do
 * not have any PCI obligations. Using CSE reduces your PCI scope when compared to implementing the API without
 * encryption.
 * 
 * If you would like to implement CSE, please provide the completed PCI Self Assessment Questionnaire (SAQ) A to the
 * Adyen Support Team (support@adyen.com). The form can be found here:
 * https://www.pcisecuritystandards.org/security_standards/documents.php?category=saqs
 * 
 * Please note: using our API requires a web service user. Set up your Webservice user:
 * Adyen CA >> Settings >> Users >> ws@Company. >> Generate Password >> Submit
 * 
 * @link /2.API/Soap/CreatePaymentCSE
 * @author Created by Adyen - Payments Made Easy
 */

@WebServlet(urlPatterns = { "/2.API/Soap/CreatePaymentCSE" })
public class CreatePaymentCSE extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// Generate current time server-side and set it as request attribute
		request.setAttribute("generationTime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date()));

		// Forward request to corresponding JSP page
		request.getRequestDispatcher("/2.API/create-payment-cse.jsp").forward(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/**
		 * SOAP settings
		 * - wsdl: the WSDL url you are using (Test/Live)
		 * - wsUser: your web service user
		 * - wsPassword: your web service user's password
		 */
		String wsdl = "https://pal-test.adyen.com/pal/Payment.wsdl";
		String wsUser = "YourWSUser";
		String wsPassword = "YourWSPassword";

		/**
		 * Create SOAP client, using classes in adyen-wsdl-cxf.jar library (generated by wsdl2java tool, Apache CXF).
		 * 
		 * @see WebContent/WEB-INF/lib/adyen-wsdl-cxf.jar
		 */
		PaymentService service = new PaymentService(new URL(wsdl));
		PaymentPortType client = service.getPaymentHttpPort();

		// Set HTTP Authentication
		((BindingProvider) client).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, wsUser);
		((BindingProvider) client).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, wsPassword);

		/**
		 * A payment can be submitted by sending a PaymentRequest to the authorise action of the web service.
		 * The request should contain the following variables:
		 * 
		 * <pre>
		 * - merchantAccount           : The merchant account for which you want to process the payment
		 * - amount
		 *     - currency              : The three character ISO currency code.
		 *     - value                 : The transaction amount in minor units (e.g. EUR 1,00 = 100).
		 * - reference                 : Your reference for this payment.
		 * - shopperIP                 : The shopper's IP address. (recommended)
		 * - shopperEmail              : The shopper's email address. (recommended)
		 * - shopperReference          : An ID that uniquely identifes the shopper, such as a customer id. (recommended)
		 * - fraudOffset               : An integer that is added to the normal fraud score. (optional)
		 * - additionalData.card.encrypted.json: The encrypted card catched by the POST variables.
		 */

		// Create new payment request
		PaymentRequest paymentRequest = new PaymentRequest();
		paymentRequest.setMerchantAccount("YourMerchantAccount");
		paymentRequest.setReference("TEST-PAYMENT-" + new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()));
		paymentRequest.setShopperIP("123.123.123.123");
		paymentRequest.setShopperEmail("test@example.com");
		paymentRequest.setShopperReference("YourReference");
		paymentRequest.setFraudOffset(0);

		// Set amount
		Amount amount = new Amount();
		amount.setCurrency("EUR");
		amount.setValue(199L);
		paymentRequest.setAmount(amount);

		// Set additional data
		Entry encryptedCard = new Entry();
		encryptedCard.setKey("card.encrypted.json");
		encryptedCard.setValue(request.getParameter("adyen-encrypted-data"));

		AnyType2AnyTypeMap additionalData = new AnyType2AnyTypeMap();
		additionalData.getEntry().add(encryptedCard);
		paymentRequest.setAdditionalData(additionalData);

		/**
		 * Send the authorise request.
		 */
		PaymentResult paymentResult;
		try {
			paymentResult = client.authorise(paymentRequest);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}

		/**
		 * If the payment passes validation a risk analysis will be done and, depending on the outcome, an authorisation
		 * will be attempted. You receive a payment response with the following fields:
		 * 
		 * <pre>
		 * - pspReference    : Adyen's unique reference that is associated with the payment.
		 * - resultCode      : The result of the payment. Possible values: Authorised, Refused, Error or Received.
		 * - authCode        : The authorisation code if the payment was successful. Blank otherwise.
		 * - refusalReason   : Adyen's mapped refusal reason, populated if the payment was refused.
		 * </pre>
		 */
		PrintWriter out = response.getWriter();

		out.println("Payment Result:");
		out.println("- pspReference: " + paymentResult.getPspReference());
		out.println("- resultCode: " + paymentResult.getResultCode());
		out.println("- authCode: " + paymentResult.getAuthCode());
		out.println("- refusalReason: " + paymentResult.getRefusalReason());

	}

}