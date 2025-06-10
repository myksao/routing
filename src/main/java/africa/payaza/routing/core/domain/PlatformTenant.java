package africa.payaza.routing.core.domain;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class PlatformTenant {
    private final String id;
    private final String tenantIdentifier;
    private final String name;
    private final String timezone;
    private final PlatformTenantConnection connection;
    private final PlatformOrganization organization;
}
