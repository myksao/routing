package africa.payaza.routing.core.domain;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PlatformOrganization {
    /**
     * Note: You can add more table properties
     */
    private final String id;
    private final String bankCode;
    private final String name;
    private final String type;
    private final String countryCode;
    private final String logoUrl;

}
