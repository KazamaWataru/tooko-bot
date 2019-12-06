package tooko.td.http.passport;

import tooko.td.http.request.BaseRequest;
import tooko.td.http.response.BaseResponse;

/**
 * Stas Parshin
 * 30 July 2018
 */
public class SetPassportDataErrors extends BaseRequest<SetPassportDataErrors, BaseResponse> {

    public SetPassportDataErrors(int userId, PassportElementError... errors) {
        super(BaseResponse.class);
        add("user_id", userId).add("errors", serialize(errors));
    }
}
