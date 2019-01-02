package org.pan;

import lombok.Data;
import org.springframework.boot.configurationprocessor.json.JSONObject;

/**
 * @author panmingzhi
 */
@Data
public class JSONResult {
    private Integer resultCode;
    private String resultMsg;
    private JSONObject data;
}
