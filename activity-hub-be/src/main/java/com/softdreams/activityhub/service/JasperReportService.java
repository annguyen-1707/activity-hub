package com.softdreams.activityhub.service;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
@Slf4j
public class JasperReportService {

    private final Map<String, JasperReport> compiledReports = new ConcurrentHashMap<>();

    public byte[] exportToPdf(String reportName, Map<String, Object> parameters, Collection<?> data) {
        try {
            JasperReport jasperReport = getOrCompileReport(reportName);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data != null ? data : java.util.Collections.emptyList());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            log.error("Failed to generate JasperReport PDF for report: {}", reportName, e);
            throw new RuntimeException("Lỗi khi tạo báo cáo PDF: " + e.getMessage(), e);
        }
    }

    private JasperReport getOrCompileReport(String reportName) throws Exception {
        return compiledReports.computeIfAbsent(reportName, name -> {
            try {
                String path = "reports/" + name + ".jrxml";
                ClassPathResource resource = new ClassPathResource(path);
                if (!resource.exists()) {
                    throw new IllegalArgumentException("Không tìm thấy file mẫu báo cáo: " + path);
                }
                try (InputStream inputStream = resource.getInputStream()) {
                    log.info("Compiling JasperReport template: {}", path);
                    return JasperCompileManager.compileReport(inputStream);
                }
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi biên dịch mẫu báo cáo " + name + ": " + e.getMessage(), e);
            }
        });
    }
}

