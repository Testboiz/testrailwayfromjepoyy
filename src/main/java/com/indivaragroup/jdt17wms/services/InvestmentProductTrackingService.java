package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.TransactionHistoryRepository;
import com.indivaragroup.jdt17wms.repositories.ProductPriceRepository;
import org.springframework.stereotype.Service;

@Service
public class InvestmentProductTrackingService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ProductPriceRepository productPriceRepository;

    public InvestmentProductTrackingService(TransactionHistoryRepository transactionHistoryRepository,
                                            ProductPriceRepository productPriceRepository) {
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.productPriceRepository = productPriceRepository;
    }
}
