package uz.pdp.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TgUser {
    private long chatId;
    private String name;
    private String phoneNumber;
    private Float lon;
    private Float lat;
    private String address;
    private String lang;
    private String botState;
    private Integer selectedProductId;

}
