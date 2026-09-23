package com.softdreams.activityhub.repository.custom;

import java.sql.Types;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softdreams.activityhub.dto.request.OrderRequest;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderCustomRepository {

    JdbcTemplate jdbcTemplate;
    ObjectMapper objectMapper;

    public String createOrder(OrderRequest request, String userId) {
        try {
            String itemsJson = objectMapper.writeValueAsString(request.getItems());

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_CreateOrder")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("userId", Types.VARCHAR),
                            new SqlParameter("shippingAddress", Types.NVARCHAR),
                            new SqlParameter("note", Types.NVARCHAR),
                            new SqlParameter("paymentMethod", Types.VARCHAR),
                            new SqlParameter("itemsJson", Types.NVARCHAR),

                            new SqlOutParameter("orderId", Types.VARCHAR),
                            new SqlOutParameter("totalAmount", Types.DECIMAL),
                            new SqlOutParameter("errorCode", Types.INTEGER),
                            new SqlOutParameter("errorMessage", Types.NVARCHAR)
                    );

            SqlParameterSource input = new MapSqlParameterSource()
                    .addValue("userId", userId)
                    .addValue("shippingAddress", request.getShippingAddress())
                    .addValue("note", request.getNote())
                    .addValue(
                            "paymentMethod",
                            request.getPaymentMethod() != null
                                    ? request.getPaymentMethod().name()
                                    : "BANK"
                    )
                    .addValue("itemsJson", itemsJson);

            Map<String, Object> output = jdbcCall.execute(input);

            Number errorCodeValue = (Number) output.get("errorCode");

            int errorCode = errorCodeValue != null
                    ? errorCodeValue.intValue()
                    : 0;

            String errorMessage = output.get("errorMessage") != null
                    ? output.get("errorMessage").toString()
                    : null;

            if (errorCode != 0) {
                log.error(
                        "Create order failed. errorCode={}, errorMessage={}",
                        errorCode,
                        errorMessage
                );

                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            Object orderIdValue = output.get("orderId");

            if (orderIdValue == null) {
                log.error("Stored Procedure không trả về orderId. Output: {}", output);

                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }

            return orderIdValue.toString();

        } catch (JsonProcessingException e) {
            log.error("Không thể chuyển items thành JSON", e);
            throw new RuntimeException("Lỗi chuyển đổi JSON sản phẩm", e);

        } catch (DataAccessException e) {
            log.error("Lỗi khi gọi Stored Procedure sp_CreateOrder", e);
            throw e;
        }
    }

    public void cancelOrder(String orderId, String cancelledBy) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_CancelOrder")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("orderId", Types.VARCHAR),
                            new SqlParameter("cancelledBy", Types.VARCHAR),
                            new SqlOutParameter("errorCode", Types.INTEGER),
                            new SqlOutParameter("errorMessage", Types.NVARCHAR)
                    );

            SqlParameterSource input = new MapSqlParameterSource()
                    .addValue("orderId", orderId)
                    .addValue("cancelledBy", cancelledBy);

            Map<String, Object> output = jdbcCall.execute(input);

            Number errorCodeValue = (Number) output.get("errorCode");
            int errorCode = errorCodeValue != null ? errorCodeValue.intValue() : 0;
            String errorMessage = output.get("errorMessage") != null ? output.get("errorMessage").toString() : null;

            if (errorCode != 0) {
                log.error("Cancel order failed. errorCode={}, errorMessage={}", errorCode, errorMessage);
                if (errorCode == 404) {
                    throw new AppException(ErrorCode.ORDER_NOT_EXISTED);
                }
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, errorMessage);
            }

            log.info("Cancel order success for orderId={}", orderId);

        } catch (AppException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("Lỗi khi gọi Stored Procedure sp_CancelOrder", e);
            throw e;
        }
    }
}
