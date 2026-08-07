package com.gabrielformario.credit_recovery_api.strategy.controller;

import com.gabrielformario.credit_recovery_api.strategy.domain.CardAction;
import com.gabrielformario.credit_recovery_api.strategy.domain.CommunicationChannel;
import com.gabrielformario.credit_recovery_api.strategy.domain.CreditAction;
import com.gabrielformario.credit_recovery_api.strategy.dto.StrategyResponse;
import com.gabrielformario.credit_recovery_api.strategy.repository.StrategyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StrategyControllerTest {

	private static final String STRATEGIES_URL = "/api/v1/strategies";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StrategyRepository strategyRepository;

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

	@Test
	void shouldReturnOkWhenStrategyExists() throws Exception {
		when(strategyRepository.findByCustomerId("PJ-12345"))
				.thenReturn(Optional.of(new StrategyResponse(
						"PJ-12345",
						CreditAction.NEGATIVATION,
						CommunicationChannel.WHATSAPP,
						CardAction.TEMPORARY_BLOCK,
						true,
						true,
						Instant.parse("2026-08-07T15:00:00Z")
				)));

		mockMvc.perform(get(STRATEGIES_URL + "/PJ-12345"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value("PJ-12345"))
				.andExpect(jsonPath("$.cardAction").value("TEMPORARY_BLOCK"));
	}

	@Test
	void shouldReturnNotFoundWhenStrategyDoesNotExist() throws Exception {
		when(strategyRepository.findByCustomerId("PJ-NOT-FOUND"))
				.thenReturn(Optional.empty());

		mockMvc.perform(get(STRATEGIES_URL + "/PJ-NOT-FOUND"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Strategy not found for customerId: PJ-NOT-FOUND"));
	}
}
