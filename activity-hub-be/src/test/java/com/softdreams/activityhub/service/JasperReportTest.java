package com.softdreams.activityhub.service;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import net.sf.jasperreports.engine.JasperCompileManager;

public class JasperReportTest {

    @Test
    void testCompileReports() throws Exception {
        String[] reports = {"activity_logs_report", "stock_transactions_report", "orders_report"};
        for (String report : reports) {
            ClassPathResource res = new ClassPathResource("reports/" + report + ".jrxml");
            try (InputStream in = res.getInputStream()) {
                String xml = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                // Clean root element
                String modifiedXml = xml.replaceAll("xmlns=\"[^\"]*\"", "")
                        .replaceAll("xmlns:xsi=\"[^\"]*\"", "")
                        .replaceAll("xsi:schemaLocation=\"[^\"]*\"", "");
                JasperCompileManager.compileReport(new java.io.ByteArrayInputStream(modifiedXml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                System.out.println("SUCCESS for " + report);
            } catch (Throwable e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Report: ").append(report).append("\n");
                sb.append("Exception: ").append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
                Throwable c = e.getCause();
                while (c != null) {
                    sb.append("Cause: ").append(c.getClass().getName()).append(": ").append(c.getMessage()).append("\n");
                    for (StackTraceElement ste : c.getStackTrace()) {
                        sb.append("   at ").append(ste).append("\n");
                    }
                    c = c.getCause();
                }
                org.junit.jupiter.api.Assertions.fail(sb.toString());
            }
        }
    }
}
