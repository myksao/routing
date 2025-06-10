package africa.payaza.routing.security.service;

import africa.payaza.routing.core.domain.PlatformTenant;

import java.util.List;


public interface TenantDetail {
 PlatformTenant fetchTenant(String identifier);
 List<PlatformTenant> fetchAllTenants();
}
