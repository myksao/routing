package africa.payaza.routing.security.filter;

import africa.payaza.routing.core.domain.PlatformTenant;
import africa.payaza.routing.core.service.ThreadLocalContextUtil;
import africa.payaza.routing.security.exception.InvalidTenantIdentifierException;
import africa.payaza.routing.security.service.TenantDetailImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Order(1)
@Service("tenantIdentifierProcessingFilter")
public class TenantAwareTenantIdentifierFilter extends GenericFilterBean {

    private final TenantDetailImpl tenantDetail;

    private final String header = "X-Tenant-ID";

    @Autowired
    public TenantAwareTenantIdentifierFilter(TenantDetailImpl tenantDetail) {
        this.tenantDetail = tenantDetail;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        final StopWatch task = new StopWatch();
        task.start();

        try {
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
            }

            // Extract tenant identifier from the request header
            String tenantId = request.getHeader(header);
            if (tenantId == null || tenantId.isEmpty()) {
                throw new InvalidTenantIdentifierException("Tenant identifier is missing or empty");
            }

            // Set the tenant identifier in the context (e.g., ThreadLocal)
            PlatformTenant tenant = this.tenantDetail.fetchTenant(tenantId);
            if (tenant == null) {
                throw new InvalidTenantIdentifierException("Invalid tenant identifier: " + tenantId);
            }
            ThreadLocalContextUtil.setTenant(tenant);

            chain.doFilter(request, response);
        } catch (final InvalidTenantIdentifierException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } finally {
            ThreadLocalContextUtil.clearTenant();
        }

    }
}
