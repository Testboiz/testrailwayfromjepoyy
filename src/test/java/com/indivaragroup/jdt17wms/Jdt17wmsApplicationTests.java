package com.indivaragroup.jdt17wms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.indivaragroup.jdt17wms.repositories.*;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=" +
				"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
				"org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
				"org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class Jdt17wmsApplicationTests {

	@MockBean
	private AssetRepository assetRepository;

	@MockBean
	private AuditLogRepository auditLogRepository;

	@MockBean
	private ExpenseRepository expenseRepository;

	@MockBean
	private FinancialProfileRepository financialProfileRepository;

	@MockBean
	private GoalRepository goalRepository;

	@MockBean
	private ProductPriceRepository productPriceRepository;

	@MockBean
	private ProductRepository productRepository;

	@MockBean
	private RecommendationRepository recommendationRepository;

	@MockBean
	private TransactionHistoryRepository transactionHistoryRepository;

	@MockBean
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

}
