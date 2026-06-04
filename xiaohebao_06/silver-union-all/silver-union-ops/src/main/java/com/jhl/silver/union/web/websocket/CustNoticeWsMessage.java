package com.jhl.silver.union.web.websocket;

import com.jhl.silver.union.web.data.customer.CustomerNoticeItemDTO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CustNoticeWsMessage {
    private String type;
    private CustomerNoticeItemDTO notice;
}
