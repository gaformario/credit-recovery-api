package com.gabrielformario.credit_recovery_api.strategy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StrategyControllerTest {

	private static final String STRATEGIES_URL = "/api/v1/strategies";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldReturnCreatedWhenRequestIsValid() throws Exception {
		mockMvc.perform(post(STRATEGIES_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerId": "PJ-12345",
								  "companyName": "Empresa XPTO LTDA",
								  "daysOverdue": 45,
								  "outstandingAmount": 15000.00,
								  "creditScore": 420,
								  "productType": "LOAN"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.customerId").value("PJ-12345"))
				.andExpect(jsonPath("$.communicationChannel").value("WHATSAPP"))
				.andExpect(jsonPath("$.creditAction").value("NEGATIVATION"));
	}

	@Test
	void shouldReturnBadRequestWhenDaysOverdueIsNegative() throws Exception {
		mockMvc.perform(post(STRATEGIES_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerId": "PJ-12345",
								  "companyName": "Empresa XPTO LTDA",
								  "daysOverdue": -1,
								  "outstandingAmount": 15000.00,
								  "creditScore": 420,
								  "productType": "LOAN"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors.daysOverdue").exists());
	}

	@Test
	void shouldReturnTemporaryBlockWhenCreditCardHasMoreThanSixtyDaysOverdue() throws Exception {
		mockMvc.perform(post(STRATEGIES_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerId": "PJ-12345",
								  "companyName": "Empresa XPTO LTDA",
								  "daysOverdue": 61,
								  "outstandingAmount": 15000.00,
								  "creditScore": 420,
								  "productType": "CREDIT_CARD"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.customerId").value("PJ-12345"))
				.andExpect(jsonPath("$.cardAction").value("TEMPORARY_BLOCK"));
	}
}
