package com.zerobase.dividend.service;

import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.persist.entity.CompanyEntity;
import com.zerobase.dividend.persist.entity.DividendEntity;
import com.zerobase.dividend.persist.repository.CompanyRepository;
import com.zerobase.dividend.persist.repository.DividendRepository;
import com.zerobase.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CompanyService {

    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new RuntimeException("already exists ticker -> " + ticker);
        }
        return storeCompanyAndDividend(ticker);
    }

    private Company storeCompanyAndDividend(String ticker) {
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        ScrapedResult scrapedResult = yahooFinanceScraper.scrap(company);

        CompanyEntity companyEntity = companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities = scrapedResult.getDividendEntities().stream()
                                                            .map(e -> new DividendEntity(companyEntity.getId(), e))
                                                            .collect(Collectors.toList());

        dividendRepository.saveAll(dividendEntities);

        return company;
    }
}
