package africa.payaza.routing.core.service;

import africa.payaza.routing.core.domain.PlatformTenant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public final class ThreadLocalContextUtil {


    public static final String CONTEXT_TENANTS = "tenants";


    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    private static final ThreadLocal<PlatformTenant> tenant_context = new ThreadLocal<>();

    public static void setTenant(final PlatformTenant tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("tenant cannot be null");
        }
        tenant_context.set(tenant);
    }

    public static PlatformTenant getTenant() {
        return tenant_context.get();
    }

    public static void clearTenant() {
        tenant_context.remove();
    }

    public static String getDataSourceContext() {
        return contextHolder.get();
    }

    public static void setDataSourceContext(final String dataSourceContext) {
        contextHolder.set(dataSourceContext);
    }

    public static void clearDataSourceContext() {
        contextHolder.remove();
    }

}
