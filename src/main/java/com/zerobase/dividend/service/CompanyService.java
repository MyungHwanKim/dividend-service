package com.zerobase.dividend.service;

import com.zerobase.dividend.exception.impl.AlreadyExistCompanyException;
import com.zerobase.dividend.exception.impl.NoCompanyException;
import com.zerobase.dividend.exception.impl.NoExistCompanyTickerException;
import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.persist.entity.CompanyEntity;
import com.zerobase.dividend.persist.entity.DividendEntity;
import com.zerobase.dividend.persist.repository.CompanyRepository;
import com.zerobase.dividend.persist.repository.DividendRepository;
import com.zerobase.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new AlreadyExistCompanyException();
        }
        return storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new NoExistCompanyTickerException();
        }

        ScrapedResult scrapedResult = yahooFinanceScraper.scrap(company);

        CompanyEntity companyEntity = companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities = scrapedResult.getDividendEntities().stream()
                                                            .map(e -> new DividendEntity(companyEntity.getId(), e))
                                                            .collect(Collectors.toList());

        dividendRepository.saveAll(dividendEntities);

        return company;
    }

    // Like 방식
    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());
    }

    public void addAutocompleteKeyword(String keyword) {
        trie.put(keyword, null);
    }

    // Trie 방식
    public List<String> autocomplete(String keyword) {
        return (List<String>) trie.prefixMap(keyword).keySet()
                                .stream()
                                .limit(10)
                                .collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword) {
        trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        CompanyEntity companyEntity = companyRepository.findByTicker(ticker)
                .orElseThrow(NoCompanyException::new);

        dividendRepository.deleteAllByCompanyId(companyEntity.getId());
        companyRepository.delete(companyEntity);

        deleteAutocompleteKeyword(companyEntity.getName());

        return companyEntity.getName();
    }
}
